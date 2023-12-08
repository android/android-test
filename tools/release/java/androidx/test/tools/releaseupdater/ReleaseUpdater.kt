package androidx.test.tools.releaseupdater

/** Utility class to use during releases of AndroidX Test maven artifacts.
 *
 * This utility does two things:
 * 1) Reads the old and new version numbers of each artifact, making sure that the versions of any
 * to-be-released artifacts are bumped correctly
 * 2) Takes the version mappings and outputs a buildozer command to update //:axt_m2repository so
 * that we only bundle artifacts that are getting a version bump
 */
class ReleaseUpdater {

  /**
   * Validates that the version is correctly incremented.
   *
   * "Correct" increments fall under two categories:
   * 1) X.X.X (stable) -> Y.Y.Y-alpha01 where validateVersionNumberIncrement('X.X.X', 'Y.Y.Y')
   *    passes
   * 2) X.X.X-suffix1 -> X.X.X-suffix2 where validateSuffixIncrement(suffix1, suffix2) passes
   *
   * @param oldVersion The original version
   * @param newVersion The incremented "+1" version
   * @throws IllegalArgumentException If the version was incorrectly incremented
   */
  fun validateVersions(oldVersion: String, newVersion: String): Unit {
    if (newVersion.indexOf("-") == -1) {
      // if newVersion doesn't have a -, it means it is a stable version, which
      // means 'oldVersion' must be an rc with the same version number
      if (oldVersion.indexOf("-") == -1) {
        // we don't currently support stable -> stable
        throw invalidVersionNumberException(newVersion, oldVersion)
      }
      val oldVersionNum = oldVersion.split("-")[0]
      val oldVersionSuffix = oldVersion.split("-")[1]
      if (newVersion != oldVersionNum || !oldVersionSuffix.startsWith("rc")) {
        throw invalidVersionException(newVersion, oldVersion)
      }
    } else if (oldVersion.indexOf("-") == -1) {
      // if oldVersion doesn't have a -, it means it is a stable version, which
      // means 'newVersion' must be an alpha01 of the next version number
      val newVersionNum = newVersion.split("-")[0]
      val newVersionSuffix = newVersion.split("-")[1]
      if (newVersionSuffix != "alpha01") {
        throw invalidVersionException(newVersion, oldVersion)
      }

      validateVersionNumberIncrement(oldVersion, newVersionNum)
    } else {
      val oldVersionNum = oldVersion.split("-")[0]
      val oldVersionSuffix = oldVersion.split("-")[1]
      val newVersionNum = newVersion.split("-")[0]
      val newVersionSuffix = newVersion.split("-")[1]

      if (oldVersionNum == newVersionNum) {
        // same number, so check the alpha/beta/rc part
        validateSuffixIncrement(oldVersionSuffix, newVersionSuffix)
      } else {
        // we've already explicitly checked for the two cases where the numbers are different, so
        // we know this one is invalid
        throw invalidVersionException(newVersion, oldVersion)
      }
    }
  }

  /**
   * Validates that the suffix is correctly incremented.
   *
   * "Correct" increments fall under two categories:
   * 1) alphaXX -> beta01, betaXX -> rc01
   * 2) alphaXX -> alphaYY, betaXX -> betaYY, rcXX -> rcYY, where XX + 1 = YY
   *
   * Used in alpha/beta/rc releases. Stable releases do not have a suffix and thus this method does
   * not handle them.
   *
   * @param oldSuffix The original suffix in (alpha/beta/rc)XX format
   * @param newSuffix The "incremented" suffix in (alpha/beta/rc)XX format
   * @throws IllegalArgumentException If the suffix was incorrectly incremented
   */
  private fun validateSuffixIncrement(oldSuffix: String, newSuffix: String): Unit {
    val ordering = listOf("alpha", "beta", "rc")
    val oldAlphaBetaOrRc = oldSuffix.dropLast(2)
    val oldSuffixNum = oldSuffix.takeLast(2)
    val newAlphaBetaOrRc = newSuffix.dropLast(2)
    val newSuffixNum = newSuffix.takeLast(2)

    if (!ordering.contains(oldAlphaBetaOrRc) || !ordering.contains(newAlphaBetaOrRc)) {
      throw invalidSuffixException(newSuffix, oldSuffix)
    }

    if (newSuffixNum == "01") {
      // ensure alpha -> beta01 or beta -> rc01
      if (ordering.indexOf(oldAlphaBetaOrRc) + 1 != ordering.indexOf(newAlphaBetaOrRc)) {
        throw invalidSuffixException(newSuffix, oldSuffix)
      }
    } else if (oldAlphaBetaOrRc != newAlphaBetaOrRc) {
      // already checked alpha -> beta or beta -> rc, so if they're still different, it's some
      // invalid combination
      throw invalidSuffixException(newSuffix, oldSuffix)
    } else {
      // both alpha or both beta or both rc, so just need to make sure that the number at the end
      // is getting incremented by 1
      if (oldSuffixNum.toInt(10) + 1 != newSuffixNum.toInt(10)) {
        throw invalidSuffixException(newSuffix, oldSuffix)
      }
    }
  }

  /**
   * Validates that the version number is correctly incremented.
   *
   * @param oldNumber The original version number in major.minor.bugfix format
   * @param newNumber The "incremented" version number in major.minor.bugfix format
   * @throws IllegalArgumentException If the version number was incorrectly incremented
   */
  private fun validateVersionNumberIncrement(oldNumber: String, newNumber: String): Unit {
    val oldMajor = oldNumber.split(".")[0].toInt()
    val oldMinor = oldNumber.split(".")[1].toInt()
    val newMajor = newNumber.split(".")[0].toInt()
    val newMinor = newNumber.split(".")[1].toInt()
    val newBugfix = newNumber.split(".")[2].toInt()

    if (oldMajor + 1 == newMajor) {
      if (newMinor != 0 || newBugfix != 0) {
        throw invalidVersionNumberException(newNumber, oldNumber)
      }
    } else if (oldMajor != newMajor) {
      throw invalidVersionNumberException(newNumber, oldNumber)
    } else if (oldMinor + 1 == newMinor) {
      if (newBugfix != 0) {
        throw invalidVersionNumberException(newNumber, oldNumber)
      }
    } else if (oldMinor != newMinor) {
      throw invalidVersionNumberException(newNumber, oldNumber)
    } else {
      // major and minor are the same. But we don't currently want to support "bugfix-only" updates
      // via this library, (or the version didn't get incremented at all) so we throw
      throw invalidVersionNumberException(newNumber, oldNumber)
    }
  }

  /** Given an "old" and a "new" version mapping, output the arguments to buildozer to use to remove
   * the maven artifacts *not* being released (i.e. the artifacts for which the version number 
   * hasn't changed between the two mappings.)
   *
   * The keys are variable names from build_extensions/axt_*.bzl, and the values are the maven
   * artifacts whose versions are controlled by that variable. See
   * build_extensions/maven/maven_registry.bzl for the corresponding logic.
   *
   * For more information about buildozer, see
   * https://github.com/bazelbuild/buildtools/blob/master/buildozer/README.md
   *
   * @param oldVersions The "old" version mapping
   * @param newVersions The "new" version mapping
   *
   * @return The buildozer command to run, as a String
   */
  fun validateAndMakeRemoveSrcsCommand(
    oldVersions: Map<String, String>,
    newVersions: Map<String, String>
  ): String {
    val depsToRemove = mutableListOf<String>()

    for (key in artifactMap.keys) {
      if (oldVersions[key] == newVersions[key]) {
        depsToRemove.addAll(artifactMap[key] as Array<String>)
      }
    }

    return if (depsToRemove.isEmpty()) {
      ""
    } else {
      "'remove srcs ${depsToRemove.joinToString(" ")}' //:axt_m2repository"
    }
  }

  companion object {
    private fun invalidSuffixException(newSuffix: String, oldSuffix: String) =
      IllegalArgumentException(String.format("Invalid suffix %s after %s", newSuffix, oldSuffix))

    private fun invalidVersionException(newVersion: String, oldVersion: String) =
      IllegalArgumentException(
        String.format("Invalid version %s after %s", newVersion, oldVersion)
      )

    private fun invalidVersionNumberException(newNumber: String, oldNumber: String) =
      IllegalArgumentException(
        String.format("Invalid version number %s after %s", newNumber, oldNumber)
      )

    private val artifactMap: Map<String, Array<String>> =
      mapOf(
        "ANNOTATION_VERSION" to
          arrayOf("//annotation/java/androidx/test/annotation:annotation_maven_artifact"),
        "CORE_VERSION" to
          arrayOf(
            "//core/java/androidx/test/core:core_maven_artifact",
            "//ktx/core/java/androidx/test/core:core_maven_artifact",
          ),
        "ESPRESSO_VERSION" to
          arrayOf(
            "//espresso/accessibility/java/androidx/test/espresso/accessibility:accessibility_checks_maven_artifact",
            "//espresso/contrib/java/androidx/test/espresso/contrib:espresso_contrib_maven_artifact",
            "//espresso/core/java/androidx/test/espresso:espresso_core_maven_artifact",
            "//espresso/idling_resource/concurrent/java/androidx/test/espresso/idling/concurrent:idling_concurrent_maven_artifact",
            "//espresso/idling_resource/java/androidx/test/espresso:espresso_idling_resource_maven_artifact",
            "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:idling_net_maven_artifact",
            "//espresso/intents/java/androidx/test/espresso/intent:espresso_intents_maven_artifact",
            "//espresso/remote/java/androidx/test/espresso/remote:espresso_remote_maven_artifact",
            "//espresso/web/java/androidx/test/espresso/web:espresso_web_maven_artifact",
          ),
        "ESPRESSO_DEVICE_VERSION" to
          arrayOf(
            "//espresso/device/java/androidx/test/espresso/device:device_maven_artifact",
          ),
        "ANDROIDX_JUNIT_VERSION" to
          arrayOf(
            "//ext/junit/java/androidx/test/ext/junit:junit_maven_artifact",
            "//ktx/ext/junit/java/androidx/test/ext/junit:junit_maven_artifact",
          ),
        "ANDROIDX_TRUTH_VERSION" to
          arrayOf(
            "//ext/truth/java/androidx/test/ext/truth:truth_maven_artifact",
          ),
        "MONITOR_VERSION" to
          arrayOf(
            "//runner/monitor/java/androidx/test:monitor_maven_artifact",
          ),
        "ORCHESTRATOR_VERSION" to
          arrayOf(
            "//runner/android_test_orchestrator/stubapp:orchestrator_release_maven_artifact",
          ),
        "RUNNER_VERSION" to
          arrayOf(
            "//runner/android_junit_runner/java/androidx/test:runner_maven_artifact",
          ),
        "RULES_VERSION" to
          arrayOf(
            "//runner/rules/java/androidx/test:rules_maven_artifact",
          ),
        "SERVICES_VERSION" to
          arrayOf(
            "//services:test_services_maven_artifact",
            "//services/storage/java/androidx/test/services/storage:test_storage_maven_artifact",
          ),
      )
  }
}
