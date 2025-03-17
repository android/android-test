# Description:
#   Tests for espresso matchers

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "RemoteHamcrestCoreMatcher13Test",
    srcs =
        ["RemoteHamcrestCoreMatcher13Test.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_hamcrest_v1_3_core_matchers",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_view_matchers",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:hamcrest_matchers_v1_3_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "RemoteViewMatchersTest",
    srcs = ["RemoteViewMatchersTest.java"],
    shard_count = 5,
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_hamcrest_v1_3_core_matchers",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_view_matchers",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:ui_interaction_view_matchers_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
