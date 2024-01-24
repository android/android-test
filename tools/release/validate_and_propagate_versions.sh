#!/bin/bash
#
# This script will do the following:
#
# 1) Validate that the version numbers in axt_versions.bzl were incremented
# correctly
# 2) Update //:axt_m2repository based off of axt_versions.bzl
# 3) Update gradle-tests/settings.gradle using axt_versions.bzl
#
# To run, execute this script from the repository root:
#
# cd $REPOSITORY_ROOT
# bash tools/release/validate_and_propagate_versions.sh
#

OUTPUT=`bazelisk run //tools/release/java/androidx/test/tools/releaseupdater:releaseupdater`
if [[ ! -z "$OUTPUT" ]]; then
  echo $OUTPUT | xargs buildozer
fi
bazelisk build //tools/release:update_settings_gradle_rule
cp bazel-bin/tools/release/settings.gradle gradle-tests/settings.gradle
