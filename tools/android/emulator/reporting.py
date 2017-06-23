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
                       unused_runtime_ms, unused_success):
    pass

  def Emit(self):
    pass


def MakeReporter():
  """Creates a reporter instance."""
  return NoOpReporter()


