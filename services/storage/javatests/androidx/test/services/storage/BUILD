# Description:
#   Tests for the test storage.

load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load("//build_extensions:phone_devices.bzl", "apis", "devices")

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "TestStorageTest",
    size = "large",
    srcs = [
        "TestStorageTest.java",
    ],
    args = [
        "--install_test_services=true",
        "--test_args=arg1=value1,arg2=value2,arg3=value3",
    ],
    data = [
        ":testinput.txt",
    ],
    device_list = devices(),
    support_apps = [
        "//services:test_services",
    ],
    deps = [
        "//core/java/androidx/test/core",
        "//services/storage/java/androidx/test/services/storage",
        "//services/storage/java/androidx/test/services/storage/file",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

[
    axt_android_library_test(
        name = "TestStorageHsumTest_{user}".format(user = user),
        size = "large",
        srcs = [
            "TestStorageTest.java",
        ],
        args = [
            "--install_test_services=true",
            "--test_args=arg1=value1,arg2=value2,arg3=value3",
            "--instrumentation_user={user}".format(user = user),
        ],
        data = [
            ":testinput.txt",
        ],
        device_list =
            custom_devices(
                apis(min_api = 36),
                "hsum_phone",
                "google_hsum",
                "x86_64",
            ),
        support_apps = [
            "//services:test_services",
        ],
        tags = ["requires-mem:20g"],
        test_class = "androidx.test.services.storage.TestStorageTest",
        deps = [
            "//core/java/androidx/test/core",
            "//runner/monitor/java/androidx/test:monitor",
            "//services/storage/java/androidx/test/services/storage",
            "//services/storage/java/androidx/test/services/storage/file",
            "@maven//:com_google_truth_truth",
            "@maven//:junit_junit",
        ],
    )
    for user in [
        "0",
        "10",
    ]
]
