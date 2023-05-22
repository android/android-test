load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

axt_android_local_test(
    name = "BundleSubjectTest",
    srcs = ["BundleSubjectTest.java"],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "//runner/rules",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "BundleSubjectTest_android",
    srcs = ["BundleSubjectTest.java"],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
    ],
)

axt_android_local_test(
    name = "ParcelableSubjectTest",
    srcs = ["ParcelableSubjectTest.java"],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "//runner/rules",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "ParcelableSubjectTest_android",
    srcs = ["ParcelableSubjectTest.java"],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
