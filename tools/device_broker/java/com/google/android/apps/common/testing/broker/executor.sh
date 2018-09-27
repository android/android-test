#!/bin/bash
# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOGFILE=$1
shift
EXECUTABLE=$1
shift
echo EXECUTING: $EXECUTABLE $@ >> $LOGFILE
echo START TIME: $(date +"%Y-%m-%d %H:%M:%S %z") >> $LOGFILE
echo START TIME-NANO: $(date +"%Y-%m-%d %H:%M:%S.%N %z") >> $LOGFILE
echo ENVIRONMENT: >> $LOGFILE
printenv >> $LOGFILE
echo STDOUT/STDERR BELOW >> $LOGFILE
echo =================== >> $LOGFILE

exec > >(tee -a ${LOGFILE})
exec 2> >(tee -a ${LOGFILE} >&2)

export DALVIK_WARNING='WARNING: linker: libdvm.so has text relocations. This is wasting memory and \(is a security risk\|prevents security hardening\). Please fix.'
set -o pipefail
trap 'kill $(jobs -p); exit' SIGINT SIGTERM

$EXECUTABLE "$@" | sed "/${DALVIK_WARNING}/d"
EXIT_CODE=$?

echo >> $LOGFILE
echo ======END LOG====== >> $LOGFILE;
echo END TIME: $(date +"%Y-%m-%d %H:%M:%S %z") >> $LOGFILE;
echo END TIME-NANO: $(date +"%Y-%m-%d %H:%M:%S.%N %z") >> $LOGFILE;
echo EXIT CODE: $EXIT_CODE >> $LOGFILE;
exit $EXIT_CODE
