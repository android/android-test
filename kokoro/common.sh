#!/bin/bash

#------------------------------------------------------------------------------
# Install bazelisk
#------------------------------------------------------------------------------
function install_bazelisk() {
  bazelisk_version="v1.16.0"
  download_url="https://github.com/bazelbuild/bazelisk/releases/download/${bazelisk_version}/bazelisk-linux-amd64"
  mkdir -p "${TMPDIR}/bazelisk-release"
  wget -nv ${download_url} -O "${TMPDIR}/bazelisk-release/bazelisk"
  chmod +x "${TMPDIR}/bazelisk-release/bazelisk"
  export PATH="${TMPDIR}/bazelisk-release:${PATH}"
}

