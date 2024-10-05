load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

package(
    default_visibility = [
        "//:__subpackages__",
    ],
)

kt_jvm_test(
    name = "ReleaseUpdaterTest",
    srcs = ["ReleaseUpdaterTest.kt"],
    deps = [
        "//tools/release/java/androidx/test/tools/releaseupdater:release_updater_lib",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
