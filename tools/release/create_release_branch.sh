#!/bin/bash

# Script to create a release branch and associated working 'in progress' branch
# TODO: add support for creating a 'stable' branch

set -e

if [[ -n $(git status -s) ]]; then
    echo "Error: git directory is not clean; check 'git status'"
    exit 1
fi

echo "Syncing main branch"
git remote update
git checkout main
git pull

DATE=$(date -u "+%Y_%m_%d")
RELEASE_BRANCH="axt_${DATE}_release_branch"

echo "Creating $RELEASE_BRANCH"
git checkout -b $RELEASE_BRANCH
git push --set-upstream origin $RELEASE_BRANCH

echo "Creating ${RELEASE_BRANCH}_in_progress"
git checkout -b ${RELEASE_BRANCH}_in_progress

echo "Creating complete. Now do the following steps"
echo "vim build_extensions/axt_versions.bzl"
echo "bash tools/release/validate_and_propagate_versions.sh"
echo "git commit -a -m 'Update artifacts and version numbers for $DATE release'"
echo "git push --set-upstream origin ${RELEASE_BRANCH}_in_progress"
echo "Navigate to https://github.com/android/android-test/compare/${RELEASE_BRANCH}...${RELEASE_BRANCH}_in_progress to create a PR"

