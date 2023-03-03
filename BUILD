load("//build_extensions/maven:maven_repo.bzl", "maven_repository")
load("@rules_jvm_external//:defs.bzl", "artifact")
load("@rules_license//rules:license.bzl", "license")

package(default_visibility = ["//:__subpackages__"])

exports_files([
    "proguard_binary.cfg",
    "LICENSE",
    "repo.bzl",
])

# Creates maven release repository
maven_repository(
    name = "axt_m2repository",
    testonly = 1,
    srcs = [
        "//annotation/java/androidx/test/annotation:annotation_maven_artifact",
        "//core/java/androidx/test/core:core_maven_artifact",
        "//espresso/accessibility/java/androidx/test/espresso/accessibility:accessibility_checks_maven_artifact",
        "//espresso/contrib/java/androidx/test/espresso/contrib:espresso_contrib_maven_artifact",
        "//espresso/core/java/androidx/test/espresso:espresso_core_maven_artifact",
        "//espresso/device/java/androidx/test/espresso/device:device_maven_artifact",
        "//espresso/idling_resource/concurrent/java/androidx/test/espresso/idling/concurrent:idling_concurrent_maven_artifact",
        "//espresso/idling_resource/java/androidx/test/espresso:espresso_idling_resource_maven_artifact",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:idling_net_maven_artifact",
        "//espresso/intents/java/androidx/test/espresso/intent:espresso_intents_maven_artifact",
        "//espresso/remote/java/androidx/test/espresso/remote:espresso_remote_maven_artifact",
        "//espresso/web/java/androidx/test/espresso/web:espresso_web_maven_artifact",
        "//ext/junit/java/androidx/test/ext/junit:junit_maven_artifact",
        "//ext/truth/java/androidx/test/ext/truth:truth_maven_artifact",
        "//ktx/core/java/androidx/test/core:core_maven_artifact",
        "//ktx/ext/junit/java/androidx/test/ext/junit:junit_maven_artifact",
        "//runner/android_junit_runner/java/androidx/test:runner_maven_artifact",
        "//runner/android_test_orchestrator/stubapp:orchestrator_release_maven_artifact",
        "//runner/monitor/java/androidx/test:monitor_maven_artifact",
        "//runner/rules/java/androidx/test:rules_maven_artifact",
        "//services:test_services_maven_artifact",
        "//services/storage/java/androidx/test/services/storage:test_storage_maven_artifact",
    ],
)

java_test(
    name = "instrumentation_test_runner",
    testonly = 1,
    tags = ["manual"],
    test_class = "com.google.android.apps.common.testing.suite.AndroidDeviceTestSuite",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//opensource:entry_point_import",
    ],
)

license(name = "license")
