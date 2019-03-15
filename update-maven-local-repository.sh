#!/bin/bash -e

# Build androidx.test artifacts and deploy them in maven local repository
# (~/.m2/repository) for testing.
#
# Make sure mavenLocal() is the primary repository in your build.gradle file
# when testing the new artifact.
#
# allprojects {
#   repositories {
#     mavenLocal()  // The ordering is important and this should be at top.
#     google()
#     jcenter()
#   }
# }

readonly TMP_WORK_DIR='/tmp/androidx_test'
readonly MAVEN_LOCAL_REPOSITORY="${HOME}/.m2/repository"

cd $(g4 g4d)

blaze build //third_party/android/androidx_test:axt_m2repository

rm -rf "${TMP_WORK_DIR}"
mkdir "${TMP_WORK_DIR}"
unzip "blaze-bin/third_party/android/androidx_test/axt_m2repository.zip" -d "${TMP_WORK_DIR}"

cp -Rf "${TMP_WORK_DIR}/m2repository/androidx/test" "${MAVEN_LOCAL_REPOSITORY}/androidx/"
