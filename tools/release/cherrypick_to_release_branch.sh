#!/bin/bash

# Script to cherry pick a commit to a 'in progress' release branch
#
#
# Usage:
# export RELEASE_BRANCH=axt_XXX_release_branch # if not already defined
# bash tools/release/cherrypick_to_release_branch.sh commit_hash

set -e

if [[ -n $(git status -s) ]]; then
    echo "Error: git directory is not clean; check 'git status'"
    exit 1
fi

if [ "$#" -ne 1 ]; then
    echo "Error: Unexpected number of parameters: Usage: cherrypick_to_release_branch.sh commit_hash"
    exit 1
fi

if [[ ! "$RELEASE_BRANCH" =~ "release_branch" ]]; then
    echo "Error: RELEASE_BRANCH env var is undefined or does not contain 'release_branch'"
    exit 1
fi

echo "Syncing main branch"
git remote update
git checkout main
git pull

echo "Checking out ${RELEASE_BRANCH}_in_progress and syncing"
git checkout ${RELEASE_BRANCH}_in_progress
git pull

echo "Cherrypicking $1 and pushing upstream"
git cherry-pick $1
git push --set-upstream origin ${RELEASE_BRANCH}_in_progress

echo "Navigate to https://github.com/android/android-test/compare/${RELEASE_BRANCH}...${RELEASE_BRANCH}_in_progress to create a PR"

