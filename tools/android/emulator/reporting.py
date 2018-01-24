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

"""An interface to report the status of emulator launches."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import json
import logging
import os
import uuid



class NoOpReporter(object):
  """Captures all device and failure data and throws it away."""

  def __init__(self):
    pass

  def ReportDeviceProperties(self, unused_emu_type, unused_props):
    pass

  def ReportFailure(self, unused_component, unused_details):
    pass

  def ReportToolsUsage(self, unused_namespace, unused_tool_name,
                       unused_runtime_ms, unused_success,
                       unused_total_runtime):
    pass

  def Emit(self):
    pass


def MakeReporter():
  """Creates a reporter instance."""
  return NoOpReporter()


