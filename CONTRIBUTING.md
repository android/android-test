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

AndroidX Test uses the [bazel](https://bazel.build) build system.

### One time setup

*   [Fork](https://help.github.com/articles/fork-a-repo/) and
    [clone](https://help.github.com/articles/cloning-a-repository/) the
    [AndroidX Test repo](https://github.com/android/android-test)
*   Install [bazel](https://docs.bazel.build/versions/master/install.html).
    Version 0.19 is recommended. Ensure your environment meets the following
    [prerequisites](https://docs.bazel.build/versions/master/android-instrumentation-test.html#prerequisites)
*   Install [maven](http://maven.apache.org/install.html) and make it available
    on PATH.
*   Install the [Android SDK](https://developer.android.com/studio/install) and
    run the following command to ensure you have the necessary components:
    `./tools/bin/sdkmanager --install 'build-tools;28.0.3'
    'platforms;android-28' 'emulator' 'platform-tools'
    'system-images;android-19;default;x86'
    'system-images;android-21;default;x86'
    'system-images;android-22;default;x86'
    'system-images;android-23;default;x86'`
*   Set the ANDROID_HOME environment variable to point to the SDK install
    location. eg `export ANDROID_HOME=/home/$USER/Android/Sdk`

### Building

```
bazel build <target path>
```

For example, to build the AndroidX Test maven repository:
```
bazel build :axt_m2repository
```

### Testing

```
bazel test <target path> --spawn_strategy=local
```

eg to run the androidx-test-core tests
```
bazel test //core/javatests/… --spawn_strategy=local
```

To run all the robolectric local tests (and thus replicate the Google Cloud
Build CI) `bazel test ... --test_tag_filters=robolectric
--build_tag_filters=robolectric`

### IDE setup

Android Studio is recommended.

*   Install the bazel Android Studio plugin
    [instructions](https://docs.bazel.build/versions/master/ide.html)
*   Select 'Import Bazel project' and set workspace location to android-test
    github repo
*   Select 'Import project view' and select <github repo>/.bazelproject

## Code reviews

All submissions, including submissions by project members, require review. We
use GitHub pull requests for this purpose. Consult
[GitHub Help](https://help.github.com/articles/about-pull-requests/) for more
information on using pull requests.

AndroidX Test follows the [Google Java Style Guide](http://google.github.io/styleguide/javaguide.html)

## Community Guidelines

This project follows [Google's Open Source Community
Guidelines](https://opensource.google.com/conduct/).
