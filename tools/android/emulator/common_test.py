# Copyright 2010 Google Inc. All Rights Reserved.
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

"""Tests for android_test_support.third_party.android.android_test_support.tools.android.emulator.common."""




import subprocess



import mox
import six

from google.apputils import basetest as googletest
from tools.android.emulator import common


class CommonTest(mox.MoxTestBase):

  def testDefaultOnError(self):
    waiter = Waitable(1)
    waiter.stdout = None
    waiter.stderr = None
    waiter.logged_stdout = None
    waiter.logged_stderr = None
    self.assertRaises(common.SpawnError, common.DefaultOnError, 'hello there',
                      waiter)

  def testWaitProcess_success(self):
    received_successes = []

    def OnSuccess(context, task):
      received_successes.append((context, task))
      return (context, task)
    test_task = Waitable()
    test_context = 'Success!'
    result = (test_context, test_task)

    self.mox.ReplayAll()

    expected_results = (result, None)
    actual_results = common.WaitProcess(test_context, test_task,
                                        on_success=OnSuccess)
    self.assertEquals(actual_results, expected_results)
    self.assertEquals(received_successes, [(test_context, test_task)])

  def testWaitProcess_fail(self):
    received_errors = []

    def OnError(context, task):
      received_errors.append((context, task))
      return (context, task)

    def OnSuccess(unused_context, unused_task):
      raise common.SpawnError('Hey I should have failed!')
    test_task = Waitable(retcode=1)
    test_context = 'Fail!'
    result = (test_context, test_task)

    self.mox.ReplayAll()

    expected_results = (None, result)
    actual_results = common.WaitProcess(test_context, test_task,
                                        on_success=OnSuccess, on_error=OnError)
    self.assertEquals(actual_results, expected_results)
    self.assertEquals(received_errors, [(test_context, test_task)])

  def testWaitProcesses_mix(self):
    received_errors = []

    def OnError(context, task):
      received_errors.append((context, task))
      return (context, task)

    received_successes = []

    def OnSuccess(context, task):
      received_successes.append((context, task))
      return (context, task)

    tasks = [('Success-1', Waitable()),
             ('Success-2', Waitable()),
             ('Fail-1', Waitable(retcode=1)),
             ('Success-3', Waitable()),
             ('Fail-2', Waitable(retcode=1))]

    self.mox.ReplayAll()

    expected_results = ([tasks[0], tasks[1], tasks[3]],
                        [tasks[2], tasks[4]])
    actual_results = common.WaitProcesses(tasks, on_error=OnError,
                                          on_success=OnSuccess)
    self.assertEquals(actual_results, expected_results)
    self.assertEquals(received_successes, expected_results[0])
    self.assertEquals(received_errors, expected_results[1])

  def testSpawn_IgnoreOutput(self):
    base_args = ['/some/process', '-a']
    args = base_args
    exec_dir = '/foo'
    exec_env = dict([('foo', 'bar')])
    logfile_name = '/tmp/blah.log'

    self.mox.StubOutWithMock(common, 'CommandLogFile')
    common.CommandLogFile(args, exec_dir, exec_env).AndReturn(logfile_name)

    self.mox.StubOutWithMock(six.moves.builtins, 'open')
    file_handle = 1
    open(logfile_name, 'a').AndReturn(file_handle)

    self.mox.StubOutWithMock(subprocess, 'Popen')
    subprocess.Popen(
        args,
        stdout=file_handle,
        stderr=file_handle,
        env=exec_env,
        close_fds=True,
        stdin=None,
        cwd=exec_dir).AndReturn(Waitable())

    self.mox.ReplayAll()

    task = common.Spawn(base_args, exec_dir=exec_dir, exec_env=exec_env)
    self.assertEquals(task.logfile_handle, file_handle)

  def testSpawn_RedirectStdinStdout(self):
    base_args = ['/some/process', '-a']
    args = base_args
    exec_dir = '/foo'
    exec_env = dict([('foo', 'bar')])
    logfile_name = '/tmp/blah.log'

    self.mox.StubOutWithMock(common, 'CommandLogFile')
    common.CommandLogFile(args, exec_dir, exec_env).AndReturn(logfile_name)

    self.mox.StubOutWithMock(subprocess, 'Popen')
    main_task = Waitable(stdout_text='foo', stderr_text='bar',
                         stdin=True)
    tee_out_task = Waitable(stdout_text='foo')
    tee_err_task = Waitable(stdout_text='bar')

    subprocess.Popen(
        args,
        stdout=subprocess.PIPE,
        stdin=subprocess.PIPE,
        stderr=subprocess.PIPE,
        env=exec_env,
        close_fds=True,
        cwd=exec_dir).AndReturn(main_task)
    subprocess.Popen(
        ['/usr/bin/tee', '-a', logfile_name],
        stdin=main_task.stdout,
        stdout=21,
        stderr=mox.Func(lambda f: f.name == '/dev/null'),
        close_fds=True).AndReturn(tee_out_task)
    subprocess.Popen(
        ['/usr/bin/tee', '-a', logfile_name],
        stdin=main_task.stderr,
        stdout=21,
        stderr=mox.Func(lambda f: f.name == '/dev/null'),
        close_fds=True).AndReturn(tee_err_task)

    self.mox.ReplayAll()

    real_task = common.Spawn(base_args, proc_output=21, proc_input=True,
                             exec_dir=exec_dir, exec_env=exec_env)
    self.assertEquals(real_task.stdout, tee_out_task.stdout)
    self.assertEquals(real_task.stderr, tee_err_task.stdout)
    self.assertEquals(real_task.tee_stdout_task, tee_out_task)
    self.assertEquals(real_task.tee_stderr_task, tee_err_task)
    self.assertEquals(real_task, main_task)
    self.assertTrue(real_task.stdin)


class Waitable(object):
  """Stub of a Popen object.

  Args:
    retcode: the return code from wait.
    stdout_text: the text present on stdout
    stderr_text: the text present on stderr
    stdin: an array of lines that will be read in from stdin.
  """

  def __init__(self, retcode=0, stdout_text=None, stderr_text=None, stdin=None):
    self.retcode = retcode
    self.stdout_text = stdout_text
    self.stderr_text = stderr_text
    self.stdin = stdin
    self.stderr = None
    self.stdout = None
    self.logged_stdout = []
    self.logged_stderr = []
    if stdout_text:
      self.stdout = stdout_text.split('\n')
      self.stderr = []
    if stderr_text:
      self.stderr = stderr_text.split('\n')

  def communicate(self, in_message=None):
    if in_message:
      self.stdin.append(input)
    self.stderr = []
    self.stdout = []

    return (self.stdout_text, self.stderr_text)

  def wait(self):
    return self.retcode

if __name__ == '__main__':
  googletest.main()
