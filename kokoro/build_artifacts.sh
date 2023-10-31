#!/bin/bash

# Fail on any error.
set -e

# this directory must exist for artifacts to be uploaded
mkdir -p "${KOKORO_ARTIFACTS_DIR}/artifacts"
cd "${KOKORO_ARTIFACTS_DIR}/github/android-test-releases"

source kokoro/common.sh
install_bazelisk
# runner has @androidsdk//:legacy_test-30 as a dependency
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk "platforms;android-30" "build-tools;30.0.0"
# building :axt_m2_repository uses @androidsdk//:build-tools/33.0.2/aapt2
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk "build-tools;33.0.2"
/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk "platforms;android-34" "build-tools;34.0.0"

bazelisk build :axt_m2repository

# copy the zip here so that we don't follow symlinks to get the files to upload
cp bazel-bin/axt_m2repository.zip .
