entry_point_deploy.jar is used by bazel to run android_instrumentation_tests.

To rebuild:
bazel build
tools/device_broker/java/com/google/android/apps/common/testing/suite:entry_point_deploy.jar
cp
bazel-bin/tools/device_broker/java/com/google/android/apps/common/testing/suite/entry_point_deploy.jar opensource/
