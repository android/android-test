load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")
load("//build_extensions:android_library_test.bzl", "axt_android_library_test")

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

axt_android_local_test(
    name = "ActivityScenarioRuleTest",
    srcs = ["ActivityScenarioRuleTest.java"],
    deps = [
        "//core/java/androidx/test/core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "//runner/monitor",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "ActivityScenarioRuleTest_android",
    srcs = ["ActivityScenarioRuleTest.java"],
    deps = [
        "//core/java/androidx/test/core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//runner/monitor",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "ActivityScenarioRuleWithCustomIntentTest_android",
    srcs = ["ActivityScenarioRuleWithCustomIntentTest.java"],
    deps = [
        "//core/java/androidx/test/core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "//runner/monitor",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_local_test(
    name = "DeleteFilesRuleTest",
    srcs = ["DeleteFilesRuleTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "DeleteFilesRuleTest_android",
    srcs = ["DeleteFilesRuleTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
