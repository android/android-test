#!/bin/bash

# Create AndroidDeviceTestSuite deploy jar for use with Bazel's
# android_instrumentation_test.

set -ex

p4 g4d

# Setup
G3_ROOT="$(pwd)"

# Use /tmp/axt_$username for a temporary directory
TMP_DIR="/tmp/axt_$(whoami)"

# Run copybara to a local directory
/google/data/ro/teams/copybara/copybara --ignore-noop third_party/android/androidx_test/copy.bara.sky piper_to_local --folder-dir="$TMP_DIR"

cd "$TMP_DIR"
bazel build tools/device_broker/java/com/google/android/apps/common/testing/suite:entry_point_deploy.jar
cp bazel-bin/tools/device_broker/java/com/google/android/apps/common/testing/suite/entry_point_deploy.jar $G3_ROOT/third_party/android/androidx_test/opensource
cp bazel-bin/tools/device_broker/java/com/google/android/apps/common/testing/suite/entry_point_deploy.jar $TMP_DIR/opensource/

cd "$G3_ROOT"
