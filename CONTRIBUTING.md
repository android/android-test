# How to Contribute

We'd love to accept your patches and contributions to this project. There are
just a few small guidelines you need to follow.

## Contributor License Agreement

Contributions to this project must be accompanied by a Contributor License
Agreement. You (or your employer) retain the copyright to your contribution;
this simply gives us permission to use and redistribute your contributions as
part of the project. Head over to <https://cla.developers.google.com/> to see
your current agreements on file or to sign a new one.

You generally only need to submit a CLA once, so if you've already submitted one
(even if it was for a different project), you probably don't need to do it
again.

## Building and Testing

AndroidX Test uses the [Bazel](https://bazel.build) build system.

Currently only Linux is fully supported. Mac may work but is not regularly tested

### One time setup

*   [Fork](https://help.github.com/articles/fork-a-repo/) and
    [clone](https://help.github.com/articles/cloning-a-repository/) the
    [AndroidX Test repo](https://github.com/android/android-test)
*   Install [Bazelisk](https://github.com/bazelbuild/bazelisk/blob/master/README.md)
    Note that instrumentation test execution support is currently not setup
    for androidx test libraries.
*   Install [maven](http://maven.apache.org/install.html) and make it available
    on PATH.
*   Install the [Android SDK](https://developer.android.com/studio/install) and
    run the following command to ensure you have the necessary components:
    `./tools/bin/sdkmanager --install 'build-tools;33.0.2'
    'platforms;android-33' 'emulator' 'platform-tools'
*   Set the `ANDROID_HOME` environment variable to point to the SDK install
    location. For example
    *   On Linux: `export ANDROID_HOME=/home/$USER/Android/Sdk`
    *   On Mac: `export ANDROID_HOME=/Users/$USER/Library/Android/sdk`
    You can also add this command to your ~/.bashrc, ~/.zshrc, or ~/.profile file to make it
    permanent.

### IDE setup

Android Studio is recommended.

*   Install the [Bazel Android Studio plugin](https://docs.bazel.build/versions/master/ide.html).
*   Setup Bazel Android Studio plugin:
    *   Navigate to `Settings > Other Settings > Bazel Settings`
    *   Update `Bazel binary location` to `/path/to/bazel/binary` (on Mac it's usually
      `/usr/local/bin/bazel`)
*   Select 'Import Bazel project' and set workspace location to android-test
    github repo
*   Select 'Import project view' and select <github repo>/.bazelproject

Check [Troubleshooting](#troubleshooting) for tips on resolving common build issues.

### Building

```
bazelisk build <target path>
```

For example, to build the AndroidX Test maven repository:
```
bazelisk build :axt_m2repository
```

### Testing

```
bazelisk test <target path> 
```

e.g. to run the androidx-test-core tests:
```
bazelisk test //core/javatests/... 
```

To run all the robolectric local tests (and thus replicate the GitHub CI):
`bazelisk test ... --test_tag_filters=robolectric --build_tag_filters=robolectric`

To run the gradle integration tests:
```
bazelisk build :axt_m2repository
unzip bazel-bin/axt_m2repository.zip -d ~/.m2/
cd gradle-tests
./gradlew nexusOneApi30DebugAndroidTest
```

### Troubleshooting

#### Unresolved imports after build

If your project fails to build because of unresolved imports two things might be wrong.

1. Missing Android SDK components.
   Open SDK Manager in Android Studio and check that the platform and build tools versions specified in the `WORKSPACE` file are installed.
   Look at the following variables:

    ```bazel
    android_sdk_repository(
        ...
        api_level = 33,
        build_tools_version = "33.0.2",
        ...
    )
    ```

2. Something might be wrong with `ANDROID_HOME` environment variable setup. Try adding the path of
the android-sdk to the `WORKSPACE` file:

    ```bazel
    android_sdk_repository(
        ...
        path = "/Users/$USER/Library/Android/sdk",
        ...
    )
    ```

## Code reviews

All submissions, including submissions by project members, require review. We
use GitHub pull requests for this purpose. Consult
[GitHub Help](https://help.github.com/articles/about-pull-requests/) for more
information on using pull requests.

AndroidX Test follows the [Google Java Style Guide](http://google.github.io/styleguide/javaguide.html)

## Community Guidelines

This project follows [Google's Open Source Community
Guidelines](https://opensource.google.com/conduct/).
