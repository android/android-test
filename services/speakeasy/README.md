# SpeakEasy

Android lacks a `sudo` command. SpeakEasy is a workaround that facilitates this
for applications that need to run commands with root privileges:

1.  `androidx.test.services` runs a service that can accept connections that
    pass IBinder information
1.  a top level `ShellMain` run with root privileges starts a server that
    publishes an IBinder to `androidx.test.services`
1.  other programs can connect to `androidx.test.services` and get the IBinder

The SpeakEasy way to run an
[Instrumentation](http://go/android-dev/reference/android/app/Instrumentation)
test is to use `app_process` to start `ShellMain`, which then runs the `am
instrument` command.

A standard launch looks like `CLASSPATH=$(pm path androidx.test.services) app_process /
androidx.test.services.shellexecutor.ShellMain am instrument -w
--no-window-animation -e targetInstrumentation
com.google.test.test/androidx.test.runner.AndroidJUnitRunner -e serverPort 64676
-e orchestratorCollectDiagnostics false -e testDataDir /data -e appPackage
com.google.test
com.google.testing.platform.android.core/.AndroidTestOrchestrator`:

1.  [`app_process`](https://cs.android.com/android/platform/superproject/+/master:frameworks/base/cmds/app_process/app_main.cpp)
    launches ShellMain in the root directory. It is not an application and it
    has no [Context](http://go/android-dev/reference/android/content/Context).
    It figures out where to get ShellMain from the `CLASSPATH`. (ShellMain is
    not present in `androidx.test.services`' manifest.)
1.  [ShellMain](http://google3/third_party/android/androidx_test/services/shellexecutor/java/androidx/test/services/shellexecutor/ShellMain.java)
    starts the ShellCommandExecutorServer.
1.  The
    [ShellCommandExecutorServer](http://google3/third_party/android/androidx_test/services/shellexecutor/java/androidx/test/services/shellexecutor/ShellCommandExecutorServer.java):
    1.  generates a *`binderKey`*.
    1.  publishes an
        [IBinder](http://go/android-dev/reference/android/os/IBinder) to the
        SpeakEasy service running in `androidx.test.services` using a
        [ToolConnection](http://google3/third_party/android/androidx_test/services/speakeasy/java/androidx/test/services/speakeasy/client/ToolConnection.java),
        which has a variety of hacks for getting at the
        [SpeakEasyContentProvider](http://google3/third_party/android/androidx_test/services/speakeasy/java/androidx/test/services/speakeasy/server/SpeakEasyContentProvider.java)
        without a Context. (Different API levels use different hacks to get at
        the [IActivityManager](https://cs.android.com/android/platform/superproject/+/refs/heads/master:frameworks/base/core/java/android/app/IActivityManager.aidl).)
    1.  spawns a thread that waits for incoming
        [Command](http://google3/third_party/android/androidx_test/services/shellexecutor/java/androidx/test/services/shellexecutor/Command.aidl)s
        and sends them to the
        [ShellCommandExecutor](http://google3/third_party/android/androidx_test/services/shellexecutor/java/androidx/test/services/shellexecutor/ShellCommandExecutor.java).
    1.  returns the *`binderKey`* to ShellMain.
1.  ShellMain passes `-e shellExecKey`*`binderKey`* to `am instrument` when it
    spawns the AndroidTestOrchestrator.
1.  The
    [AndroidTestOrchestrator](http://google3/third_party/utp/android/java/com/google/testing/platform/android/core/AndroidTestOrchestrator.kt)
    1.  passes its arguments to the
        [InstrumentationRegistry](http://google3/third_party/android/androidx_test/runner/monitor/java/androidx/test/platform/app/InstrumentationRegistry.java)
    1.  uses a
        [ShellExecutor](http://google3/third_party/android/androidx_test/services/shellexecutor/java/androidx/test/services/shellexecutor/ShellExecutor.java)
        to send commands to the server.
        1.  The
            [ShellExecutorImpl](http://google3/third_party/android/androidx_test/services/shellexecutor/java/androidx/test/services/shellexecutor/ShellExecutorImpl.java)
            constructor takes the *`binderKey`* retrieved from the
            InstrumentationRegistry.
        1.  It uses the
            [ShellCommandClient](http://google3/third_party/android/androidx_test/services/shellexecutor/java/androidx/test/services/shellexecutor/ShellCommandClient.java)
            to deliver the Command.
            1.  This involves creating an
                [Intent](http://go/android-dev/reference/android/content/Intent)
                for package `androidx.test.services` and class
                `androidx.test.services.speakeasy.server.SpeakEasyService`
            1.  and passing it to
                [Context.startForegroundService](http://go/android-dev/reference/android/content/Context#startForegroundService\(android.content.Intent\))
                (on API >= 26) or
                [Context.startService()](http://go/android-dev/reference/android/content/Context#startService\(android.content.Intent\)).
            1.  It then blocks on a
                [FindResultReceiver](http://google3/third_party/android/androidx_test/services/speakeasy/java/androidx/test/services/speakeasy/client/FindResultReceiver.java),
                which extends
                [ResultReceiver](http://go/android-dev/reference/android/os/ResultReceiver).
1.  In
    [`androidx.test.services`](http://google3/third_party/android/androidx_test/services/AndroidManifest.xml),
    the
    [SpeakEasyService](http://google3/third_party/android/androidx_test/services/speakeasy/java/androidx/test/services/speakeasy/server/SpeakEasyService.java)
    receives the Intent in its `onStart` and sends it to a background thread,
    where it's handed to SpeakEasy.
    1.  [SpeakEasy](http://google3/third_party/android/androidx_test/services/speakeasy/java/androidx/test/services/speakeasy/server/SpeakEasy.java)
        contains the core logic of pairing binders with identifiers. It uses a
        [ResultReceiver](http://go/android-dev/reference/android/os/ResultReceiver)
        to pass the message along.

## Challenges

IBinders cannot be persisted, so if `androidx.test.services` is killed by the
lowmemorykiller, it cannot restore its state. When tests run under low memory
conditions, dex2oat can gobble tons of memory while the test is getting set up.
