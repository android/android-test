load("@build_bazel_rules_android//android:rules.bzl", "android_local_test")
load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load("//build_extensions:phone_devices.bzl", "devices")

java_library(
    name = "robolectric_config",
    resources = ["robolectric.properties"],
)

android_local_test(
    name = "EventtoTest",
    srcs = ["EventtoTest.java"],
    jvm_flags = ["-Drobolectric.looperMode=INSTRUMENTATION_TEST"],
    deps = [
        ":robolectric_config",
        "//espresso/core/java/androidx/test/espresso",
        "//eventto",
        "//eventto/javatests/androidx/test/eventto/fixtures",
        "//ext/junit",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "EventtoTest_android",
    srcs = ["EventtoTest.java"],
    device_list = devices([34]),
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//eventto",
        "//eventto/javatests/androidx/test/eventto/fixtures",
        "//ext/junit",
        "@maven//:junit_junit",
    ],
)
