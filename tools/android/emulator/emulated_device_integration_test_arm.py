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

"""Large tests which actually starts an emulator."""



import os

from tools.android.emulator import resources
from google.apputils import basetest as googletest
from tools.android.emulator import emulated_device
from tools.android.emulator import fake_android_platform_util

root_dir = os.path.abspath(os.path.join(resources.GetRunfilesDir(),
                                        'android_test_support'))
SYSTEM_IMG_DIR = 'third_party/java/android/system_images/android_19/armeabi'


class EmulatedDeviceIntegrationTest(googletest.TestCase):

  def testStartEmulator_arm_source_properties(self):
    attempts = 0
    last_err = None
    while attempts < 4:
      attempts += 1
      try:
        device = emulated_device.EmulatedDevice(
            android_platform=fake_android_platform_util.BuildAndroidPlatform())
        device.Configure(
            fake_android_platform_util.GetSystemImageDir(SYSTEM_IMG_DIR),
            '800x480',
            '512',
            233,
            36,
            source_properties={'systemimage.abi': 'armeabi',
                               'androidversion.apilevel': '19'})
        device.StartDevice(False, 0)
        device.KillEmulator(politely=True)
        return
      except emulated_device.TransientEmulatorFailure as e:
        last_err = e
    self.fail(last_err)


if __name__ == '__main__':
  googletest.main()
