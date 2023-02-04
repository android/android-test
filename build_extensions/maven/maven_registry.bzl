"""Defines maven artifact definitions"""

load(
    "//build_extensions:axt_versions.bzl",
    "ANDROIDX_JUNIT_VERSION",
    "ANDROIDX_TRUTH_VERSION",
    "ANNOTATION_VERSION",
    "CORE_VERSION",
    "ESPRESSO_DEVICE_VERSION",
    "ESPRESSO_VERSION",
    "MONITOR_VERSION",
    "ORCHESTRATOR_VERSION",
    "RULES_VERSION",
    "RUNNER_VERSION",
    "SERVICES_VERSION",
)

# map of target path prefixes to maven artifact.
# This map is based on the androidx.test architecture principle that there is one and only one maven artifact for all targets
# under a given directory. Or in other words, that code in this repo is organized according to which
# maven artifact it belongs to.
_TARGET_TO_MAVEN_ARTIFACT = {
    "//annotation/java/": "androidx.test:annotation:%s" % ANNOTATION_VERSION,
    "//runner/android_junit_runner/java/": "androidx.test:runner:%s" % RUNNER_VERSION,
    "//runner/rules/java/": "androidx.test:rules:%s" % RULES_VERSION,
    "//runner/monitor/java/": "androidx.test:monitor:%s" % MONITOR_VERSION,
    "//core/java/": "androidx.test:core:%s" % CORE_VERSION,
    "//ktx/core/java/": "androidx.test:core-ktx:%s" % CORE_VERSION,
    "//espresso/accessibility/java/": "androidx.test.espresso:espresso-accessibility:%s" % ESPRESSO_VERSION,
    "//espresso/contrib/java/": "androidx.test.espresso:espresso-contrib:%s" % ESPRESSO_VERSION,
    "//espresso/core/java/": "androidx.test.espresso:espresso-core:%s" % ESPRESSO_VERSION,
    "//espresso/device/java/": "androidx.test.espresso:espresso-device:%s" % ESPRESSO_DEVICE_VERSION,
    "//espresso/idling_resource/java/": "androidx.test.espresso:espresso-idling-resource:%s" % ESPRESSO_VERSION,
    "//espresso/idling_resource/concurrent/java/": "androidx.test.espresso.idling:idling-concurrent:%s" % ESPRESSO_VERSION,
    "//espresso/idling_resource/net/java/": "androidx.test.espresso.idling:idling-net:%s" % ESPRESSO_VERSION,
    "//espresso/intents/java/": "androidx.test.espresso:espresso-intents:%s" % ESPRESSO_VERSION,
    "//espresso/remote/java/": "androidx.test.espresso:espresso-remote:%s" % ESPRESSO_VERSION,
    "//espresso/web/java/": "androidx.test.espresso:espresso-web:%s" % ESPRESSO_VERSION,
    "//ext/junit/java/": "androidx.test.ext:junit:%s" % ANDROIDX_JUNIT_VERSION,
    "//ktx/ext/junit/java/": "androidx.test.ext:junit-ktx:%s" % ANDROIDX_JUNIT_VERSION,
    "//ext/truth/java/": "androidx.test.ext:truth:%s" % ANDROIDX_TRUTH_VERSION,
    "//services/storage/java/": "androidx.test.services:storage:%s" % SERVICES_VERSION,
    # services/events/java gets built into both androidx.test.runner as well as orchestrator v2
    "//services/events/java/": "androidx.test:runner:%s" % RUNNER_VERSION,
}

# maven apk definitions
SERVICES_APK_ARTIFACT = "androidx.test.services:test-services:%s" % SERVICES_VERSION
ORCHESTRATOR_ARTIFACT = "androidx.test:orchestrator:%s" % ORCHESTRATOR_VERSION

def get_artifact_from_label(label):
    label_string = str(label)
    result = None
    for path, artifact in _TARGET_TO_MAVEN_ARTIFACT.items():
        if path in label_string:
            if result:
                fail("Found multiple maven artifacts for %s path." % label_string)
            result = artifact
    return result
