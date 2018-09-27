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

"""Utility functions for managing subprocesses.

 Provides support for spawning processes, waiting for them, and capturing
 their output.
"""



import os
import signal
import subprocess
import threading

from absl import flags
from absl import logging

FLAGS = flags.FLAGS
flags.DEFINE_string('subprocess_log_dir',
                    os.environ.get('TEST_UNDECLARED_OUTPUTS_DIR'),
                    'The directory to log commands to.')


def _GetLogCommand(logfile, is_stderr=False):
  """Return suitable command line to write log to logfile."""
  if logfile.startswith('/cns/') or logfile.startswith('/namespace/'):
    # CNS doesn't like concurrent writers. We just skip logging stderr here
    # since the current use case doesn't need stderr content on CNS.
    if is_stderr:
      return ['/usr/bin/tee', '-a', '/dev/null']
    else:
      subprocess.check_call(['fileutil', '-version'])
      # The default buffer for fileutil is 1M. We set it to 1k here.
      # That means there could be lagging with 1k of log.
      return ['fileutil', '--fileutil_internal_buffer_size', '1',
              'tee', '-a', logfile]
  return ['/usr/bin/tee', '-a', logfile]


def _ResetSigPipeHandling():
  # http://bugs.python.org/issue1652
  signal.signal(signal.SIGPIPE, signal.SIG_DFL)


def CommandLogFile(args, exec_dir, extra_env):
  """Creates a logfile to store the output/error of a subprocess.

  This function also stores useful debugging info in the logfile including:
  args used to launch the command, the directory the command was executed from
  and the environment that the command was executed with.

  Args:
    args: the args used to launch the subprocess
    exec_dir: the directory the process launches from.
    extra_env: the environment used to launch the process.

  Returns:
    A path to the logfile or None if logging is disabled.
  """
  if 'subprocess_log_dir' in extra_env:
    subprocess_log_dir = extra_env['subprocess_log_dir']
  else:
    subprocess_log_dir = FLAGS.subprocess_log_dir
  if not subprocess_log_dir:
    return None
  CommandLogFile.count += 1
  command_name = args[0]
  if command_name.rfind('/') > -1:
    command_name = command_name[command_name.rindex('/') + 1:]

  logfile_name = '%s/%s-%s.txt' % (subprocess_log_dir, command_name,
                                   CommandLogFile.count)
  logfile = open(logfile_name, 'w')
  logfile.writelines(['Executing: %s\n' % ' '.join(args),
                      'Execdir: %s\n' % exec_dir,
                      'Environment: %s\n' % extra_env,
                      'STDOUT/STDERR Below\n',
                      '===================\n'])
  logfile.close()
  return logfile_name

CommandLogFile.count = 0


def EnsureFileCached(path):
  """Makes sure a file is cached on the local machine.

  Files on objfs / bazel-out cache can be accessed like standard
  files by regular unix tools. However reading from them can cause long
  blocks. Some programs (emulator, adb, etc) do not behave well in this case.

  To circumvent this, we can write the first byte of the file to /dev/null.

  Args:
    path: path to the file
  Raises:
    AssertionError: if the path doesn't exist.
  """
  assert os.path.exists(path), 'Path doesn\'t exist: %s' % path
  with open('/dev/null', mode='w') as dev_null:
    logging.debug('Ensuring we have cache hit (first attempt might take some '
                  'time) for file: %s', path)
    subprocess.check_call(['/usr/bin/head', '-c1', path],
                          stdout=dev_null,
                          stderr=dev_null)


def Spawn(args, proc_input=None, proc_output=None, exec_dir=None,
          exec_env=None, logfile=None, **kwargs):
  """Execs a subprocess using Popen.

  Task output will be logged to file.

  Args:
    args: A list of arguments to execute
    proc_input: takes true or a file descriptor. If true the stdin is a
      readable pipe. If file descriptor the file is piped into stdin
    proc_output: true for stdout/stderr to be pipes, file descriptor to pipe to
      file.
    exec_dir: the directory the subprocess will run from.
    exec_env: the environment the subprocess will use.
    logfile: an optional filename to log stdout/stderr.
    **kwargs: passed to subprocess.Popen as is.

  Returns:
    An object supporting Popen.wait(), Popen.stdout and Popen.stdin
  """
  proc_err = proc_output
  if not exec_dir:
    exec_dir = os.getcwd()
  if not exec_env:
    exec_env = dict(os.environ)

  if not logfile:
    logfile = CommandLogFile(args, exec_dir, exec_env)
  if not logfile:
    logfile = '/dev/null'

  logfile_handle = None

  if proc_input == True:  # could be a file or True or none
    proc_input = subprocess.PIPE

  logged_to_file = False
  if proc_output == True:  # could be a file or True or None
    proc_output = subprocess.PIPE
    proc_err = subprocess.PIPE
  elif not proc_output:
    # caller not doing anything with outputs, dont pass them on.
    # just write output straight to log.
    logged_to_file = True
    logfile_handle = open(logfile, 'a')
    proc_output = logfile_handle
    proc_err = logfile_handle

  logging.debug('Launching %s. input: %s, output: %s, execdir: %s',
                ' '.join(args), proc_input, proc_output, exec_dir)
  task = None

  if not logged_to_file:
    # launch the task and tee tasks to write the stdout/stderr to file and then
    # pass it along to the caller's pipes or files.
    task = subprocess.Popen(args, stdout=subprocess.PIPE, stdin=proc_input,
                            env=exec_env, cwd=exec_dir, stderr=subprocess.PIPE,
                            close_fds=True,
                            **kwargs)
    log_out_task = subprocess.Popen(_GetLogCommand(logfile),
                                    stdin=task.stdout, stdout=proc_output,
                                    stderr=open('/dev/null'),
                                    close_fds=True)
    log_err_task = subprocess.Popen(_GetLogCommand(logfile, is_stderr=True),
                                    stdin=task.stderr, stdout=proc_err,
                                    stderr=open('/dev/null'),
                                    close_fds=True)

    task.stdout = log_out_task.stdout
    task.stderr = log_err_task.stdout
    task.tee_stdout_task = log_out_task
    task.tee_stderr_task = log_err_task
  else:
    # proc_output and proc_error are already pointing to the logfiles.
    task = subprocess.Popen(args, stdout=proc_output, stdin=proc_input,
                            env=exec_env, cwd=exec_dir, stderr=proc_err,
                            close_fds=True)
    task.logfile_handle = logfile_handle

  task.logged_stdout = ''
  task.logged_stderr = ''
  task.spawn_env = exec_env

  def LoggingCommunicate(task_in=None):
    """Used to capture the results of a call to communicate.

    Args:
      task_in: optional input to communicate
    Returns:
      A pair of (stdout, stderr) from the process.
    """
    task.logged_stdout, task.logged_stderr = task.RawCommunicate(input=task_in)
    return (task.logged_stdout, task.logged_stderr)

  task.RawCommunicate = task.communicate
  task.communicate = LoggingCommunicate

  return task


def _DefaultTimeoutFunction(task):
  if task.poll() is None:
    # still running!
    task.kill()


def SpawnAndWaitWithRetry(args, retries=1, timeout_seconds=None,
                          timeout_fn=_DefaultTimeoutFunction, retry_fn=None,
                          on_success=None, on_error=None, **kwds):
  attempts = 0
  while True:
    logging.debug('Attempt #%s to %s', attempts, args)
    timer = None
    try:
      task = Spawn(args, **kwds)
      if timeout_seconds:
        timer = threading.Timer(timeout_seconds, timeout_fn, [task])
        timer.daemon = True
        timer.start()
      task.borg_out, task.borg_err = task.communicate()
      WaitProcess(args, task, on_success=on_success, on_error=on_error)
      if timer:
        timer.cancel()
      return task
    except SpawnError as b:
      if timer:
        timer.cancel()
      attempts += 1
      if attempts > retries:
        logging.error('Command %s failed %s times. Giving up.', args, attempts)
        raise b
      if retry_fn:
        retry_fn(task)


def WaitProcess(context, task, on_error=None, on_success=None):
  """The single process case of WaitProcesses.

  Args:
    context: The context object given to on_success or on_error handlers..
    task: An object supporting wait().
    on_error: an error handling function f(context, task) when the process has
      non-zero error codes. Default error handler throws an exception and logs
      the command.
    on_success: a handing function f(context, task) that is called when the
      process has a normal return code. Default handler does nothing.

  Returns:
    A pair (success, failure) with the result of running the handler function.
  """
  successes, failures = WaitProcesses([(context, task)], on_error=on_error,
                                      on_success=on_success)
  if failures:
    return (None, failures[0])
  else:
    return (successes[0], None)


def WaitProcesses(context_and_tasks, on_error=None, on_success=None):
  """Waits for a group of processes to complete.

  Args:
    context_and_tasks: A sequence of pairs of context and a task object. Each
      task will have wait() called and based on return code the success/error
      handler will be called with context, task.
    on_error: An error handler when task returns a non zero return code.
      Handler should accept context, task. The default handler logs the
      command and throws an exception. The return of this handler will be
      returned back to the caller of WaitProcesses.
    on_success: A success handler function f(context, task). On every
      successful wait() the handler is invoked and the return is returned
      back to the caller. The default handler is a no-op.

  Returns:
    A pair of sequences of success and failure results.
  """

  if not on_error:
    on_error = DefaultOnError
  if not on_success:
    on_success = DefaultOnSuccess

  success_results = []
  error_results = []
  for context, task in context_and_tasks:
    wait_result = task.wait()
    if hasattr(task, 'tee_stdout_task'):
      task.tee_stdout_task.wait()
    if hasattr(task, 'tee_stderr_task'):
      task.tee_stderr_task.wait()
    if hasattr(task, 'logfile_handle'):
      task.logfile_handle.flush()
      task.logfile_handle.close()

    if wait_result:
      # note task.wait() returns 0 on success, non-zero on error.
      error_result = on_error(context, task)
      error_results.append(error_result)
    else:
      success_result = on_success(context, task)
      success_results.append(success_result)
  return (success_results, error_results)


def DefaultOnError(context, task):
  """Default error handling for failed processes.

  This function raises an exception when a process has a non-zero return code.
  The exception will contain the context and the return code. Also if the task
  was launched by Spawn() the exception will contain the command that was
  executed.

  If the process had opened pipes to stdout/stderr, the remaining content of
  the pipes is logged out.

  Args:
    context: the context of the task
    task: the task itself.

  Returns:
    Nothing. Always throws exception.

  Raises:
      SpawnError: Containing as much context about the failed task as possible.
  """
  extra_details = ''
  if hasattr(task, 'logfile_handle'):
    extra_details += 'logfile: ' + task.logfile_handle.name
  output = []

  if task.logged_stdout:
    output.append(task.logged_stdout)
  elif task.stdout and not task.stdout.closed:
    output += task.stdout.readlines()

  if task.logged_stderr:
    output.append(task.logged_stderr)
  elif task.stderr and not task.stderr.closed:
    output += task.stderr.readlines()

  if output:
    logging.error('Task failed: output: %s', '\n'.join(output))

  raise SpawnError('Task failed. Context: %s, retcode: %s %s'
                   % (context, task.wait(), extra_details))


def DefaultOnSuccess(unused_context, unused_task):
  """Default success handler.

  No op function.

  Args:
    unused_context: ignored
    unused_task: ignored

  Returns:
    None.
  """
  pass


class SpawnError(Exception):
  pass
