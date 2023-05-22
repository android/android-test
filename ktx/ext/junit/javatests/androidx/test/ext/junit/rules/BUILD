# Description: Tests for androidx.test.ext.junit.rules Kotlin extension

load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")
load("//build_extensions:android_library_test.bzl", "axt_android_library_test")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

axt_android_local_test(
    name = "ActivityScenarioRuleKotlinTest",
    srcs = [
        "ActivityScenarioRuleKotlinTest.kt",
    ],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ktx/core",
        "//ktx/ext/junit",
        "//runner/monitor",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "ActivityScenarioRuleKotlinTest_android",
    srcs = [
        "ActivityScenarioRuleKotlinTest.kt",
    ],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ktx/core",
        "//ktx/ext/junit",
        "//runner/monitor",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_local_test(
    name = "ActivityScenarioRuleWithCustomIntentKotlinTest",
    srcs = [
        "ActivityScenarioRuleWithCustomIntentKotlinTest.kt",
    ],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "//ktx/core",
        "//ktx/ext/junit",
        "//runner/monitor",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "ActivityScenarioRuleWithCustomIntentKotlinTest_android",
    srcs = [
        "ActivityScenarioRuleWithCustomIntentKotlinTest.kt",
    ],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "//ktx/core",
        "//ktx/ext/junit",
        "//runner/monitor",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
