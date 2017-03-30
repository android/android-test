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

"""Tests for android_test_support.third_party.android.android_test_support.tools.android.emulator.resources.

"""

import os
import tempfile

from google.apputils import basetest as googletest
from tools.android.emulator import resources


class ResourcesTest(googletest.TestCase):

  def testGetResourceFilenameWithAbsoluteFileName(self):
    temp_file = tempfile.NamedTemporaryFile().name
    resource_file = resources.GetResourceFilename(temp_file)
    self.assertEquals(temp_file, resource_file)

  def testFindRunfilesDir(self):
    # Create a set of temp directories in the format that we know bazel would
    # create.
    root_dir = tempfile.mkdtemp()
    emu_dir = os.path.join(root_dir, (
        'android_test_support/bazel-bin/tools/android/emulator'))
    os.makedirs(emu_dir)
    runfiles_dir = os.path.join(emu_dir, 'resources_test.runfiles')
    os.makedirs(runfiles_dir)
    output = resources.FindRunfilesDir(os.path.join(emu_dir, 'resources_test'))
    self.assertEquals(runfiles_dir, output)

  def testGetResourceAsFile(self):
    _, temp_file = tempfile.mkstemp()
    resource_file = resources.GetResourceAsFile(temp_file)
    self.assertFalse(resource_file.closed)


if __name__ == '__main__':
  googletest.main()
