load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")
load("//build_extensions:phone_devices.bzl", "apis", "devices")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

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

axt_android_local_test(
    name = "PersistableBundleSubjectTest",
    srcs = ["PersistableBundleSubjectTest.java"],
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
    ],
)

axt_android_library_test(
    name = "PersistableBundleSubjectTest_android",
    srcs = ["PersistableBundleSubjectTest.java"],
    device_list = devices(apis()),
    deps = [
        "//core",
        "//core/javatests/androidx/test/core/app/testing",
        "//core/javatests/androidx/test/core/app/testing:manifest",
        "//ext/junit",
        "//ext/truth",
        "@maven//:junit_junit",
    ],
)
