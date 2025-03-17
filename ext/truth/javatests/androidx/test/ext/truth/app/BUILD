load("//build_extensions:android_library_test.bzl", "axt_android_library_test")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "NotificationActionSubjectTest",
    srcs = ["NotificationActionSubjectTest.java"],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "NotificationSubjectTest",
    srcs = ["NotificationSubjectTest.java"],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "//runner/rules",
        "@maven//:junit_junit",
    ],
)
