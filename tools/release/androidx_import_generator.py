"""Script to update the androidx repo with the released axt versions."""

from collections.abc import Sequence
import fileinput
import os

from absl import app

from tools.release import release_versions


def main(argv: Sequence[str]) -> None:
  if len(argv) > 1:
    raise app.UsageError('Too many command-line arguments.')

  _edit_gradle_versions()
  _edit_docs_versions()
  _output_import_command()


def _edit_gradle_versions():
  """Edit the gradle versions in the androidx repo."""
  androidx_home = os.environ['ANDROIDX_HOME']
  gradle_versions_path = os.path.join(
      androidx_home, 'frameworks/support/gradle/libs.versions.toml'
  )
  print('Modifying ' + gradle_versions_path)
  print('')
  with fileinput.input(files=[gradle_versions_path], inplace=True) as file:
    for line in file:
      if line.startswith('espresso = "'):
        print(
            'espresso = "{Espresso}"'.format(
                **release_versions.RELEASED_VERSIONS_DICT
            )
        )
      elif line.startswith('espressoDevice = "'):
        print(
            'espressoDevice = "{Espresso Device}"'.format(
                **release_versions.RELEASED_VERSIONS_DICT
            )
        )
      elif line.startswith('androidxTestRunner = "'):
        print(
            'androidxTestRunner = "{Runner}"'.format(
                **release_versions.RELEASED_VERSIONS_DICT
            )
        )
      elif line.startswith('androidxTestRules = "'):
        print(
            'androidxTestRules = "{Rules}"'.format(
                **release_versions.RELEASED_VERSIONS_DICT
            )
        )
      elif line.startswith('androidxTestMonitor = "'):
        print(
            'androidxTestMonitor = "{Monitor}"'.format(
                **release_versions.RELEASED_VERSIONS_DICT
            )
        )
      elif line.startswith('androidxTestCore = "'):
        print(
            'androidxTestCore = "{Core}"'.format(
                **release_versions.RELEASED_VERSIONS_DICT
            )
        )
      elif line.startswith('androidxTestExtJunit = "'):
        print(
            'androidxTestExtJunit = "{JUnit Extensions}"'.format(
                **release_versions.RELEASED_VERSIONS_DICT
            )
        )
      elif line.startswith('androidxTestExtTruth = "'):
        print(
            'androidxTestExtTruth = "{Truth Extensions}"'.format(
                **release_versions.RELEASED_VERSIONS_DICT
            )
        )
      else:
        print(line.rstrip())


def _edit_docs_versions():
  """Edit the axt versions used in docs-public build in androidx repo."""
  androidx_home = os.environ['ANDROIDX_HOME']
  docs_version_path = os.path.join(
      androidx_home, 'frameworks/support/docs-public/build.gradle'
  )
  print('Modifying ' + docs_version_path)
  print('')

  output = True
  with fileinput.input(files=[docs_version_path], inplace=True) as file:
    for line in file:
      if 'docsWithoutApiSince("androidx.test' in line:
        if output:
          print(
              """    docsWithoutApiSince("androidx.test:core:{Core}")
    docsWithoutApiSince("androidx.test:core-ktx:{Core}")
    docsWithoutApiSince("androidx.test:monitor:{Monitor}")
    docsWithoutApiSince("androidx.test:rules:{Rules}")
    docsWithoutApiSince("androidx.test:runner:{Runner}")
    docsWithoutApiSince("androidx.test.espresso:espresso-accessibility:{Espresso}")
    docsWithoutApiSince("androidx.test.espresso:espresso-contrib:{Espresso}")
    docsWithoutApiSince("androidx.test.espresso:espresso-core:{Espresso}")
    docsWithoutApiSince("androidx.test.espresso:espresso-device:{Espresso Device}")
    docsWithoutApiSince("androidx.test.espresso:espresso-idling-resource:{Espresso}")
    docsWithoutApiSince("androidx.test.espresso:espresso-intents:{Espresso}")
    docsWithoutApiSince("androidx.test.espresso:espresso-remote:{Espresso}")
    docsWithoutApiSince("androidx.test.espresso:espresso-web:{Espresso}")
    docsWithoutApiSince("androidx.test.espresso.idling:idling-concurrent:{Espresso}")
    docsWithoutApiSince("androidx.test.espresso.idling:idling-net:{Espresso}")
    docsWithoutApiSince("androidx.test.ext:junit:{JUnit Extensions}")
    docsWithoutApiSince("androidx.test.ext:junit-ktx:{JUnit Extensions}")
    docsWithoutApiSince("androidx.test.ext:truth:{Truth Extensions}")
    docsWithoutApiSince("androidx.test.services:storage:{Services}")""".format(
                  **release_versions.RELEASED_VERSIONS_DICT
              )
          )
        output = False
      else:
        print(line.rstrip())


def _output_import_command():
  """Output the command to download the maven artifacts in androidx repo."""
  print('Run this command:')
  print(
      """
        development/importMaven/importMaven.sh \\
        androidx.test.espresso:espresso-accessibility:{Espresso} \\
        androidx.test.espresso:espresso-device:{Espresso Device} \\
        androidx.test.espresso:espresso-remote:{Espresso} \\
        androidx.test.espresso.idling:idling-concurrent:{Espresso} \\
        androidx.test.espresso.idling:idling-net:{Espresso} \\
        androidx.test.espresso:espresso-contrib:{Espresso}  \\
        androidx.test.ext:truth:{Truth Extensions} \\
        androidx.test.ext:junit-ktx:{JUnit Extensions} \\
        androidx.test:monitor:{Monitor} \\
        androidx.test:rules:{Rules} \\
        androidx.test:runner:{Runner} \\
        androidx.test.espresso:espresso-intents:{Espresso} \\
        androidx.test:core-ktx:{Core}""".format(
          **release_versions.RELEASED_VERSIONS_DICT
      )
  )


if __name__ == '__main__':
  app.run(main)
