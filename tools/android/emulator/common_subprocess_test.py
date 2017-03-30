# Copyright 2012 Google Inc. All Rights Reserved.
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

"""Mediumish tests for subprocess based methods in common."""



import threading

from google.apputils import basetest as googletest
from tools.android.emulator import common


def _CheckExecution(fn):
  def _WrappedExecution(*args, **kwargs):
    _WrappedExecution.called = True
    return fn(*args, **kwargs)
  _WrappedExecution.called = False
  return _WrappedExecution


class CommonSubprocessTest(googletest.TestCase):
  def setUp(self):
    super(CommonSubprocessTest, self).setUp()
    self.custom_timeout_called = False
    self.timers = []
    self.timer_factory = threading.Timer
    threading.Timer = self._MonitorTimerCreation()

  def tearDown(self):
    super(CommonSubprocessTest, self).tearDown()
    threading.Timer = self.timer_factory
    self.timers = None

  def _MonitorTimerCreation(self):
    def TimerFactory(interval, function, *args, **kwargs):
      checked_function = _CheckExecution(function)
      timer = self.timer_factory(interval, checked_function, *args, **kwargs)
      timer._ActualCancel = timer.cancel
      timer._was_cancel_called = False  # cancel should always be called
      timer._checked_function = checked_function

      def _LogCancel():
        timer._was_cancel_called = True
        return timer._ActualCancel()

      timer.cancel = _LogCancel
      self.timers.append(timer)
      return timer
    return TimerFactory

  def testTimeout(self):
    try:
      common.SpawnAndWaitWithRetry(
          ['/bin/sleep',
           '10'],
          timeout_seconds=5,
          retries=2)
      self.fail('Should have timed out.')
    except common.SpawnError as unused_expected:
      pass
    self._AssertTimersExecuted()

  def testTimeout_successful(self):
    try:
      common.SpawnAndWaitWithRetry(
          ['/bin/sleep',
           '10'],
          timeout_seconds=30,
          retries=2)
    except common.SpawnError as unexpected:
      self.fail('Should have successfully executed: %s' % unexpected)
    self._AssertTimersNotExecuted()

  def testTimeout_cancelOnError(self):
    try:
      common.SpawnAndWaitWithRetry(
          ['/bin/sh',
           '-c',
           '/bin/sleep 5 && /bin/false'],
          timeout_seconds=30,
          retries=1)
    except common.SpawnError as unused_expected:
      pass
    self._AssertTimersNotExecuted()

  def testTimeout_customFunction(self):
    def CustomFn(task):
      self.custom_timeout_called = True
      if task.poll() is None:
        task.kill()
    try:
      common.SpawnAndWaitWithRetry(
          ['/bin/sleep',
           '10'],
          timeout_fn=CustomFn,
          timeout_seconds=1)
      self.fail('Should have timed out.')
    except common.SpawnError as unused_expected:
      self.assertTrue(self.custom_timeout_called, 'Custom callback not called.')
    self._AssertTimersExecuted()

  def _AssertTimersExecuted(self):
    for timer in self.timers:
      self.assertTrue(timer._checked_function.called, 'function never called.')
      self.assertTrue(timer._was_cancel_called, 'even executed timers should '
                      'be cancelled.')

  def _AssertTimersNotExecuted(self):
    for timer in self.timers:
      self.assertTrue(timer._was_cancel_called, 'cancel not attempted')
      self.assertFalse(timer._checked_function.called, 'the timer function was'
                       'executed')

if __name__ == '__main__':
  googletest.main()
