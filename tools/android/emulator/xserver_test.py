# Copyright 2017 Google Inc. All Rights Reserved.
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

"""Tests for tools.android.emulator.xserver."""

import os
import subprocess
import tempfile

from tools.android.emulator import resources
from google.apputils import basetest as googletest
from tools.android.emulator import xserver


class X11ServerTest(googletest.TestCase):

  def setUp(self):
    self.x11 = None

  def tearDown(self):
    if self.x11 is not None:
      self.x11.Kill()

  def testXServerDisplayResolution(self):
    self.x11 = xserver.X11Server(
        resources.GetRunfilesDir(), tempfile.mkdtemp(), 225, 300)
    self.x11.Start()
    self.assertEquals('225x300', self._GetXRes(self.x11.environment))

  def testXServerKill(self):
    self.x11 = xserver.X11Server(
        resources.GetRunfilesDir(), tempfile.mkdtemp(), 200, 400)
    self.x11.Start()
    # should not throw.
    self._GetXRes(self.x11.environment)
    env = self.x11.environment
    self.x11.Kill()
    try:
      self._GetXRes(env)
      self.fail('X not being killed')
    except subprocess.CalledProcessError as unused_expected:
      pass

  def testXDiesAtStart(self):
    self.x11 = xserver.X11Server(
        resources.GetRunfilesDir(), tempfile.mkdtemp(), 200, 400)
    self.x11._xvfb_bin = '/bin/false'
    try:
      self.x11.Start()
      self.fail('should crash')
    except xserver.ProcessCrashedError as unused_expected:
      pass

  def testDoubleStartNoOp(self):
    self.x11 = xserver.X11Server(
        resources.GetRunfilesDir(), tempfile.mkdtemp(), 200, 400)
    self.x11.Start()
    env = self.x11.environment
    self.x11.Start()
    env_2 = self.x11.environment
    self.assertEquals(env, env_2)

  def testIsRunningBeforeStart(self):
    self.x11 = xserver.X11Server(
        resources.GetRunfilesDir(), tempfile.mkdtemp(), 225, 300)
    self.assertFalse(self.x11.IsRunning())
    self.assertFalse(self.x11.IsRunning())  # Doesn't change after invocation.

  def testIsRunningAfterKill(self):
    self.x11 = xserver.X11Server(
        resources.GetRunfilesDir(), tempfile.mkdtemp(), 225, 300)
    self.x11.Start()
    self.x11.Kill()
    self.assertFalse(self.x11.IsRunning())
    self.assertFalse(self.x11.IsRunning())  # Doesn't change after invocation.

  def testIsRunningWhenRunning(self):
    self.x11 = xserver.X11Server(
        resources.GetRunfilesDir(), tempfile.mkdtemp(), 225, 300)
    self.x11.Start()
    self.assertTrue(self.x11.IsRunning())
    self.assertTrue(self.x11.IsRunning())  # Doesn't change after invocation.

  def testStartWithZeroTimeoutTimesOut(self):
    self.x11 = xserver.X11Server(
        resources.GetRunfilesDir(), tempfile.mkdtemp(), 225, 300)
    try:
      self.x11.Start(wait_until_up_sec=0)
      self.fail('should timeout')
    except xserver.TimeoutError as unused_expected:
      pass

# Uncomment this test to test performance of xvfb.
# The 100 cycles take 211 seconds as of cl/98458300, 2015/07/16.
#  def testStart100times(self):
#    for _ in range(0, 100):
#      self.x11 = xserver.X11Server(
#          resources.GetRunfilesDir(), tempfile.mkdtemp(), 225, 300)
#      self.x11.Start()
#      self.x11.Kill()

  def _GetXRes(self, x11env):
    env = dict(os.environ)
    env.update(x11env)
    return subprocess.check_output(
        [
            os.path.join(resources.GetRunfilesDir(),
                         'android_test_support/'
                         'tools/android/emulator/xres')
        ],
        env=env).strip()

if __name__ == '__main__':
  googletest.main()
