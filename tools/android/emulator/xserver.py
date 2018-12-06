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

"""XVFB Integration.

This module allows us to launch a headless xvfb server - it is modeled on:
  //testing/matrix/nativebrowsers/x/xvfb/launch_xvfb.sh

With some small tweaks to:
  prevent the caller from needing to know an empty DISPLAY
  force 24-bit depth (for emulator)
  force specification of width / height
  minimize copying of binaries.

"""

import datetime
import os
import socket
import subprocess
import tempfile
import time
from absl import logging



class X11Server(object):
  """Represents a headless X server."""

  def __init__(self, runfiles_dir, temp_dir, width, height):
    # To properly set up all the symlinks for xvfb launch,
    # runfiles_dir needs to be an absolute path.
    self._runfiles_dir = os.path.abspath(runfiles_dir)
    self._temp_dir = temp_dir
    self.width = int(width)
    self.height = int(height)
    self._x11_process = None
    self._xvfb_bin = '/usr/bin/Xvfb'
    assert os.path.exists(self._temp_dir)
    assert self.width > 0
    assert self.height > 0

  def Start(self, wait_until_up_sec=30):
    """Launches an Xvfb server. Raises if it didn't successfully start."""
    if not self._x11_process:
      self._LaunchX()
    WaitUntilRunning(self, wait_until_up_sec)

  def IsRunning(self):
    """Returns True if this server is up and running, False otherwise.

    Returns:
      True is this server is up and running, False otherwise.

    Raises:
      ProcessCrashedError: if Xvfb process started and unexpectedly exited.
    """
    if self._x11_process and self._x11_process.poll() is not None:
      return_code = self._x11_process.returncode
      self._Cleanup(self.x11_pid)

      self._x11_process = None
      raise ProcessCrashedError('Xvfb crashed unexpectedly, exit code %s' %
                                return_code)
    if (not self._x11_process
        or self._x11_process.poll() is not None
        or not hasattr(self._x11_process, 'display')):
      return False

    s = None
    try:
      s = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
      s.connect('/tmp/.X11-unix/X%s' % self.x11_pid)
      return True
    except socket.error:
      return False
    finally:
      if s:
        s.close()

  def Kill(self):
    """Kills this server if started (else no-op). Returns process' exit code."""
    old_x_proc = self._x11_process
    self._x11_process = None
    if old_x_proc:
      old_pid = old_x_proc.pid
      if old_x_proc.poll() is None:
        old_x_proc.terminate()
        if old_x_proc.poll() is None:
          time.sleep(2)
          if old_x_proc.poll() is None:
            old_x_proc.kill()
      self._Cleanup(old_pid)
      return old_x_proc.wait()
    else:
      return 0

  @property
  def display(self):
    """Returns display string such as ':12345'. Throws if not started."""
    assert self._x11_process
    assert self._x11_process.poll() is None
    return self._x11_process.display

  @property
  def x11_pid(self):
    """Returns PID of this server or None if it is not started."""
    return (self._x11_process and self._x11_process.pid) or None

  @property
  def environment(self):
    """Returns a dict with env values for process to use this display."""
    return {'DISPLAY': self.display}


  def _LaunchX(self):
    """Launches Xvfb in a separate process. Doesn't wait for it to start."""

    x11_env = {}

    args = [
        self._xvfb_bin,
        '-retro',
        '-nocursor',
        '-noreset',
        '-nolisten', 'tcp',
        '-screen', '0', '%sx%sx24' % (self.width, self.height)]
    def _ExecFn():
      # need a unique / discoverable display value.
      # Lets use the xserver pid.
      # This fn will be executed after fork() but immedately
      # before exec(). Python's done all the heavy lifting for
      # us so all we need to do is slap our pid as the last arg
      # to xvfb and do the execve ourselves.
      args.append(':%s' % os.getpid())
      os.execve(args[0], args, x11_env)

    preexec_fn = _ExecFn

    try:
      self._x11_process = subprocess.Popen(
          args,
          preexec_fn=preexec_fn,
          close_fds=True,
          stdin=open(os.devnull),
          env=x11_env)
    except (ValueError, OSError) as e:
      logging.error('Failed to start process, %s', e)
      raise ProcessCrashedError('Xvfb failed to launch')
    self._x11_process.display = ':%s' % self._x11_process.pid

  # Clean up leftover X server files in the tmp directory
  def _Cleanup(self, pid):
    # Try to remove the socket file if it exists
    x11_tmp_file = '/tmp/.X11-unix/X%s' % pid
    try:
      os.remove(x11_tmp_file)
    except OSError:
      pass

    # Try to remove the /tmp/.X$DISPLAY-lock lockfile if it exists
    lockfile = '/tmp/.X%s-lock' % pid
    try:
      os.remove(lockfile)
    except OSError:
      pass


class TimeoutError(Exception):
  """Server took too long to start."""
  pass


class ProcessCrashedError(Exception):
  """Process crashed unexpectedly."""
  pass


class External(object):
  """X server that's started externally to this process. This reads $DISPLAY."""

  def __init__(self, env=None):
    env = env or os.environ
    self._display = env.get('DISPLAY')
    assert self._display, 'There\'s no external X server'
    self._env = {}
    self._env['DISPLAY'] = self._display
    if 'XAUTHORITY' in env:
      self._env['XAUTHORITY'] = env.get('XAUTHORITY')
    else:
      self._env['XAUTHORITY'] = '~/.Xauthority'
    if 'XDG_SESSION_COOKIE' in env:
      self._env['XDG_SESSION_COOKIE'] = env.get('XDG_SESSION_COOKIE')

  def Start(self, wait_until_up_sec=30):
    pass

  def IsRunning(self):
    return True

  def Kill(self):
    return 0  # We'll let them think they've killed the server...

  @property
  def display(self):
    return self._display

  @property
  def x11_pid(self):
    """Returns None, this is for compatibility with X11Server."""
    return None

  @property
  def environment(self):
    return self._env


def WaitUntilRunning(server, timeout_sec):
  # pylint: disable=g-doc-args
  """Waits for the specified timout until server.IsRunning().

  Attempts to server.Kill() if time is up and not server.IsRunning().

  Raises:
    TimeoutError: if timeout exceeded.
    ProcessCrashedError: if Xvfb process started and unexpectedly exited.
  """
  deadline = datetime.datetime.now() + datetime.timedelta(seconds=timeout_sec)
  while datetime.datetime.now() < deadline:
    if server.IsRunning():
      return
    else:
      time.sleep(0.05)

  try:
    server.Kill()
  except Exception as e:  # pylint: disable=broad-except
    logging.warn('Error killing server after start-up timeout: %s', e)
  raise TimeoutError(
      'Server did not start within %s seconds' % timeout_sec)
