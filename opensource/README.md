entry_point_deploy.jar is used in Bazel's android_instrumentation_test.

To update :

cd <project root>
bazel build tools/device_broker/java/com/google/android/apps/common/testing/suite:entry_point_deploy.jar
cp bazel-bin/tools/device_broker/java/com/google/android/apps/common/testing/suite/entry_point_deploy.jar opensource/