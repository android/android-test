package androidx.test.tools.releaseupdater

import java.io.InputStream
import java.util.Scanner
import kotlin.text.CharCategory

/** Entry point for running the release updating logic. Run via
 *
 * bazelisk run //tools/release/java/androidx/test/tools/releaseupdater:releaseupdater | xargs buildozer
 *
 * to validate axt_versions.bzl/axt_released_versions.bzl and update //:axt_m2repository
 */
class Main {

  companion object {
    fun parseVersionsToMap(stream: InputStream): MutableMap<String, String> {
      val map = mutableMapOf<String, String>()
      Scanner(stream).use {
        while (it.hasNextLine()) {
          val line = it.nextLine()
          if (line.isNotBlank()) {
            val firstChar = line[0]
            if (firstChar.category == CharCategory.UPPERCASE_LETTER) {
              val parsedLine = line.split(" ")
              // line is of the form 'KEY_VERSION = "value" # comment'
              if (parsedLine[0].endsWith("VERSION")) {
                map[parsedLine[0]] = parsedLine[2].drop(1).dropLast(1)
              }
            }
          }
        }
      }

      return map
    }

    @JvmStatic
    fun main(args: Array<String>) {
      val versionsToUpdate =
        parseVersionsToMap(
          Main::class.java.getClassLoader().getResourceAsStream("build_extensions/axt_versions.bzl")
        )

      val releasedVersions =
        parseVersionsToMap(
          Main::class
            .java
            .getClassLoader()
            .getResourceAsStream("build_extensions/axt_released_versions.bzl")
        )

      println(ReleaseUpdater().validateAndMakeRemoveSrcsCommand(releasedVersions, versionsToUpdate))
    }
  }
}
