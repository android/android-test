# Description:
#   Tests for espresso matchers

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
    "gmscore_channel",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_library(
    name = "utils",
    srcs = ["MatcherTestUtils.java"],
    visibility = ["//visibility:private"],
    deps = ["@maven//:org_hamcrest_hamcrest_core"],
)

axt_android_library_test(
    name = "BoundedDiagnosingMatcherTest",
    srcs = ["BoundedDiagnosingMatcherTest.java"],
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@androidsdk//:legacy_test-34",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "CursorMatchersTest",
    srcs = ["CursorMatchersTest.java"],
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@androidsdk//:legacy_test-34",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "HasBackgroundMatcherTest",
    srcs = ["HasBackgroundMatcherTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@androidsdk//:legacy_test-34",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "LayoutMatchersTest",
    srcs = ["LayoutMatchersTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "PreferenceMatchersTest",
    srcs = ["PreferenceMatchersTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "ViewMatchers1Test",
    srcs = ["ViewMatchers1Test.java"],
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@androidsdk//:legacy_test-34",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

# TODO(b/232962936): AOSP API 26 image does not include webview
axt_android_library_test(
    name = "ViewMatchers2Test",
    size = "large",
    srcs = ["ViewMatchers2Test.java"],
    device_list = devices(apis(exclude_apis = [26])) + devices(
        [26],
        gms_channel = gmscore_channel.PREBUILT,
    ),
    shard_count = 4,
    deps = [
        ":utils",
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@androidsdk//:legacy_test-34",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)
