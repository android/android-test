# Description: Tests for androidx.test.core

load("@build_bazel_rules_android//android:rules.bzl", "android_binary")
load("//build_extensions:axt_android_application_test.bzl", "axt_android_application_test")
load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")
load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load("//build_extensions:phone_devices.bzl", "apis", "devices")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

# ActivityScenarioTest is unique, in that we want to define the activity under test in a
# separate target binary, since that is how most users tests will be setup
android_binary(
    name = "ActivityScenarioTest_target",
    manifest = "AndroidManifest_target.xml",
    multidex = "legacy",
    deps = [
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "@maven//:androidx_multidex_multidex",
    ],
)

axt_android_application_test(
    name = "ActivityScenarioTest_instrumentation",
    srcs = ["ActivityScenarioTest.java"],
    args = [
        "--instrumentation_options=waitForActivitiesToComplete=false",
        # turn off video recording. Video recording won't work anyway on the upcoming slim device with skipDrawing enabled,
        # and turning off video recording saves significant runtime and flakiness
        "--record_test_video=NEVER",
    ],
    instruments = ":ActivityScenarioTest_target",
    manifest = "AndroidManifest_test.xml",
    shard_count = 8,
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//ext/truth",
        "//runner/android_junit_runner",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_application_test(
    name = "ActivityScenarioSharedTest_instrumentation",
    srcs = ["ActivityScenarioSharedTest.java"],
    args = [
        "--instrumentation_options=waitForActivitiesToComplete=false",
        # turn off video recording. Video recording won't work anyway on the upcoming slim device with skipDrawing enabled,
        # and turning off video recording saves significant runtime and flakiness
        "--record_test_video=NEVER",
    ],
    instruments = ":ActivityScenarioTest_target",
    manifest = "AndroidManifest_test.xml",
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//ext/junit",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_local_test(
    name = "ActivityScenarioTest",
    srcs = ["ActivityScenarioTest.java"],
    manifest_values = {
        "applicationId": "androidx.test.core.app",
    },
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//ext/truth",
        "//runner/android_junit_runner",
        "//runner/monitor/java/androidx/test:monitor",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "ApplicationProviderTest",
    srcs = ["ApplicationProviderTest.java"],
    manifest_values = {
        "applicationId": "androidx.test.core",
    },
    deps = [
        "//core",
        "//ext/junit",
        "@maven//:com_google_truth_truth",
    ],
)

# TakeScreenshotTest needs its own rule due to higher min sdk
axt_android_library_test(
    name = "TakeScreenShotTest",
    srcs = ["TakeScreenShotTest.kt"],
    device_list = devices(
        apis(min_api = 18),
    ),
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ktx/core",
        "//services/storage/java/androidx/test/services/storage",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)
