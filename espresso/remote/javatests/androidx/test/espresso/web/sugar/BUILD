# Description: Tests for espresso web

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
    name = "RemoteWebSugarTest",
    srcs = ["RemoteWebSugarTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/javatests/androidx/test/espresso/remote:util",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "//espresso/remote/java/androidx/test/espresso/web/action:remote_web_actions",
        "//espresso/remote/java/androidx/test/espresso/web/assertion:remote_web_assertions",
        "//espresso/remote/java/androidx/test/espresso/web/matcher:remote_web_matchers",
        "//espresso/remote/java/androidx/test/espresso/web/proto/sugar:web_sugar_proto_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/web/sugar:remote_sugar",
        "//espresso/web/java/androidx/test/espresso/web/action",
        "//espresso/web/java/androidx/test/espresso/web/assertion",
        "//espresso/web/java/androidx/test/espresso/web/matcher",
        "//espresso/web/java/androidx/test/espresso/web/sugar",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
