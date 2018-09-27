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

"""Util library for tests to build AndroidPlatform."""

import os


from absl import flags
from tools.android.emulator import resources
from tools.android.emulator import emulated_device


FLAGS = flags.FLAGS
flags.DEFINE_string('system_image_dir',
                    'third_party/java/android/system_images/android_10/armeabi',
                    'Workspace Relative System image location.')
flags.DEFINE_string('android_sdk_path',
                    'third_party/java/android/android_sdk_linux/',
                    'Path of Android SDK.')


root_dir = os.path.abspath(os.path.join(resources.GetRunfilesDir(),
                                        'android_test_support'))


def BuildAndroidPlatform():
  """Builds a Fake Android Platform that sets all the parameters for tests."""

  platform = emulated_device.AndroidPlatform()
  sdk_root = GetAndroidSdkPath()
  platform.adb = os.path.join(root_dir,
                              ''
                              'tools/android/emulator/support/adb.turbo')
  platform.emulator_x86 = os.path.join(sdk_root, 'tools/emulator64-x86')
  platform.emulator_arm = os.path.join(sdk_root, 'tools/emulator64-arm')
  platform.emulator_wrapper_launcher = os.path.join(sdk_root, 'tools/emulator')
  platform.real_adb = os.path.join(sdk_root, 'platform-tools/adb')
  platform.mksdcard = os.path.join(sdk_root, 'tools/mksdcard')
  platform.empty_snapshot_fs = os.path.join(
      sdk_root, 'tools/lib/emulator/snapshots.img')
  platform.emulator_support_lib_path = os.path.join(
      root_dir, 'third_party/browser_automation/lib/')
  platform.base_emulator_path = os.path.join(sdk_root, 'tools')
  return platform


def GetSystemImageDir(sys_dir=None):
  """Gets the path of the System Image Directory."""
  if sys_dir:
    return _GetFilePath(sys_dir)
  return _GetFilePath(FLAGS.system_image_dir)


def GetAndroidSdkPath():
  """Gets the path of the Android SDK Directory."""
  return _GetFilePath(FLAGS.android_sdk_path)


def _GetFilePath(path):
  if os.path.isabs(path) and os.path.exists(path):
    return path
  else:
    return os.path.join(root_dir, path)
