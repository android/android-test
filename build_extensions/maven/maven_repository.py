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

"""Script to package up multiple maven artifacts into a repository."""

import zipfile

from absl import app
from absl import flags

FLAGS = flags.FLAGS

flags.DEFINE_list('sources', None, 'List of source m2repository files')
flags.mark_flag_as_required('sources')

flags.DEFINE_string('output', None, 'Output zip file')
flags.mark_flag_as_required('output')


def main(_):
  output_zip = zipfile.ZipFile(FLAGS.output, 'w', zipfile.ZIP_STORED)
  for source in FLAGS.sources:
    source_zip = zipfile.ZipFile(source, 'r')
    for zip_info in source_zip.infolist():
      output_zip.writestr(zip_info, source_zip.read(zip_info))
    source_zip.close()
  output_zip.close()


if __name__ == '__main__':
  app.run(main)()
