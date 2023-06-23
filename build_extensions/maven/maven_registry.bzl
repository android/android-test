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
    "//runner/rules:rules": "androidx.test:rules:%s" % RULES_VERSION,
    "//runner/monitor/java/": "androidx.test:monitor:%s" % MONITOR_VERSION,
    "//runner/monitor:monitor": "androidx.test:monitor:%s" % MONITOR_VERSION,
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
    "//services:test_services": "androidx.test.services:test-services:%s" % SERVICES_VERSION,
    "//runner/android_test_orchestrator/stubapp:stubapp": "androidx.test:orchestrator:%s" % ORCHESTRATOR_VERSION,
}

_SHADED_TARGETS = [
    "@com_google_protobuf//:protobuf_javalite",
    "//opensource/proto:any_java_proto_lite",
    "@com_google_protobuf//:any_proto",
    "//opensource/dagger:dagger",
    "@com_google_protobuf_protobuf_javalite//:com_google_protobuf_protobuf_javalite",
    # emulator controller proto for bazel gets embedded inside espresso-device
    "//opensource/emulator/proto:emulator_controller_java_grpc",
    "//opensource/emulator/proto:emulator_controller_java_proto_lite",
]

# maven apk definitions
SERVICES_APK_ARTIFACT = "androidx.test.services:test-services:%s" % SERVICES_VERSION
ORCHESTRATOR_ARTIFACT = "androidx.test:orchestrator:%s" % ORCHESTRATOR_VERSION

def get_artifact_from_label(label):
    """Retrieve the maven artifact (if known) from the build label."""
    label_string = str(label)
    result = None
    for path, artifact in _TARGET_TO_MAVEN_ARTIFACT.items():
        if path in label_string:
            if result:
                fail("Found multiple maven artifacts for %s path." % label_string)
            result = artifact

    return result

def is_axt_label(label):
    """Determine if given target label is from androidx_test.

    Args:
      label: the target label

    Returns:
      True if the label is in a recognized androidx_test maven artifact
    """
    label_string = str(label)
    for path in _TARGET_TO_MAVEN_ARTIFACT.keys():
        if path in label_string:
            return True

    # special case the apk source dirs
    if "//services" in label_string:
        return True
    if "//runner/android_test_orchestrator" in label_string:
        return True
    return False

def get_maven_apk_deps(artifact):
    # TODO: don't hardcode this, instead try to obtain from build rule
    if artifact == ORCHESTRATOR_ARTIFACT:
        return [SERVICES_APK_ARTIFACT]
    else:
        return []

def is_shaded_from_label(label):
    """Returns true if given target should be shaded.

    A shaded target is one whose classes should be embedded in resulting aar and
    renamed via jarjar.
    """

    # bazel 6.0.0 mysteriously prefixes a '@' onto //opensource/dagger, so just remove it
    string_label = str(label).replace("@//", "//")
    return string_label in _SHADED_TARGETS
