#!/bin/bash
# Copyright 2018 The Android Open Source Project. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Shell script to invoke javadoc, used by droiddoc.bzl.
#
# Usage: droiddoc.sh [arguments] source.jar ...

#source ${BASH_SOURCE[0]}.runfiles/google3/util/gbash.sh || exit

set -o errexit
set -o pipefail

# When running from a skylark action a simple "source gbash.sh" will not work.
# However once we manage to source gbash we can safely rely on the $RUNFILES
# variable it sets for resolving all other dependencies.
source "${BASH_SOURCE[0]}.runfiles/google3/util/shell/gbash/gbash.sh" || exit 1

DEFINE_string classpath --required "" "Classpath to supply to javadoc command"
DEFINE_string output --required "" "Path to output zip file"
DEFINE_string api_output --required "" "Path to output API TXT"
DEFINE_string dirname --required "" \
    "Name of top level directory inside zip file"
DEFINE_string packages "" \
    "Space separated list of top level packages to restrict docs to"
DEFINE_string federation_project "" "Project to federate docs with"
DEFINE_string federation_url "" "URL of docs to federate with"
DEFINE_string federation_api_txt "" "Path to API TXT of federated docs"
DEFINE_string devsite "" "Generate docs for devsite"
DEFINE_string yamlV2 "" "Generate docs for devsite"


readonly JAVADOC="$RUNFILES/google3/third_party/java/jdk/jdk-64/bin/javadoc"
readonly ZIP="$RUNFILES/google3/third_party/zip/zip"
readonly UNZIP="$RUNFILES/google3/third_party/unzip/unzip"
readonly DOCLAVA="$RUNFILES/google3/third_party/java/doclava/current/doclava.jar"
readonly JSILVER="$RUNFILES/google3/third_party/java/jsilver/v1_0_0/jsilver.jar"

# Reorganize directory of java files by their java packages.
# javadoc -sourcepath only works if source files are in paths matching
# their java package.
function organize_srcs {
  local -r src_dir="$1"; shift || gbash::die "Missing argument: src_dir"
  (( $# == 0 )) || gbash::die "Too many arguments"

  for f in $(find "$src_dir" -name '*.java'); do
    pkg="$(sed -En 's/^package ([a-zA-Z0-9\.]+)\;/\1/p' "$f" | sed 's!\.!/!g')"
    mkdir -p "$src_dir/$pkg"
    if [[ $(dirname "$f") != $src_dir/$pkg ]]; then
      mv "$f" "$src_dir/$pkg"
    fi
  done
}

# Outputs the list of packages containing classes for a well-organized
# (post organize_srcs) directory of .java files.
function all_src_packages {
  local -r src_dir="$1"; shift || gbash::die "Missing argument: src_dir"
  (( $# == 0 )) || gbash::die "Too many arguments"

  ( cd "$src_dir" ; find . -name '*.java' -exec dirname \{\} \; |
      sed -e 's!^\./!!' -e 's!/!.!g' | sort | uniq )
}

# Joins paramaters $2 onwards using $1 as separator.
# Usage: join ":" "${my_array[@]"
function join {
  local delim=$1; shift
  echo -n "$1"; shift
  printf "%s" "${@/#/$delim}"
}

function main {
  local -r src_tmp="$(mktemp -d --suffix=_sources)"
  for srcjar in "$@"; do
    echo "extracting $srcjar"
    "$UNZIP" -qo "$srcjar" -d "$src_tmp"
  done
  organize_srcs "$src_tmp"

  if [[ -z $FLAGS_packages ]] ; then
    # No package list specified, document all packages.
    packages=($(all_src_packages "$src_tmp"))
  else
    packages=($FLAGS_packages)
  fi

  local extra_args=()

  if [[ -n $FLAGS_federation_project ]] ; then
    [[ -n $FLAGS_federation_url ]] || gbash::die "federation_url not set"
    [[ -n $FLAGS_federation_api_txt ]] ||
        gbash::die "federation_api_txt not set"

    extra_args+=(
        "-federate" "$FLAGS_federation_project" "$FLAGS_federation_url"
        "-federationapi" "$FLAGS_federation_project" "$FLAGS_federation_api_txt"
    )
  fi
  if [[ -n $FLAGS_devsite ]] ; then
    extra_args+=("-devsite")
  fi

  if [[ -n $FLAGS_yamlV2 ]] ; then
    extra_args+=("-yamlV2")
  fi

  local -r docs_tmp="$(mktemp -d --suffix=_sources)"
  mkdir -p "$docs_tmp/$FLAGS_dirname"
  "$JAVADOC" \
      -quiet \
      -encoding "UTF-8" \
      -XDignore.symbol.file \
      -classpath "$FLAGS_classpath" \
      -doclet "com.google.doclava.Doclava" \
      -docletpath "$DOCLAVA:$JSILVER" \
      -protected \
      -yaml "_book.yaml" \
      -hdf dac true \
      -dac_libraryroot "androidx/test" \
      -dac_dataname "SUPPORT_TEST_DATA" \
      -toroot "/" \
      -stubpackages "$(join ":" "${packages[@]}")" \
      -api "$FLAGS_api_output" \
      -d "$docs_tmp/$FLAGS_dirname" \
      -sourcepath "$src_tmp" \
      "${extra_args[@]}" \
      "${packages[@]}"
  (
    root_dir="$(pwd)"
    cd "$docs_tmp/$FLAGS_dirname"
    # Rare zip options:
    #  -jt sets the timestamp of all entries to zero
    #  -X  no extra file attributes
    "$ZIP" -jt -X -q -r "$root_dir/$FLAGS_output" .
  )
}

gbash::main "$@"
