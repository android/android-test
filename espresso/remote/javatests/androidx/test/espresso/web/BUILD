# Description: Tests for espresso web

load(
    "//build_extensions:android_app_instrumentation_tests.bzl",
    "android_app_instrumentation_tests",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    testonly = 1,
    srcs = glob(
        ["**/*.java"],
    ),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/javatests/androidx/test/espresso/remote:util",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_hamcrest_v1_3_core_matchers",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "//espresso/remote/java/androidx/test/espresso/web/action:remote_web_actions",
        "//espresso/remote/java/androidx/test/espresso/web/assertion:remote_web_assertions",
        "//espresso/remote/java/androidx/test/espresso/web/matcher:remote_web_matchers",
        "//espresso/remote/java/androidx/test/espresso/web/model:remote_web_model_atoms",
        "//espresso/remote/java/androidx/test/espresso/web/proto/action:web_actions_proto_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/web/proto/assertion:web_assertions_proto_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/web/proto/matcher:remote_web_matchers_proto_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/web/proto/model:remote_web_model_atoms_proto_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/web/proto/sugar:web_sugar_proto_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/web/proto/webdriver:web_driver_atoms_proto_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/web/sugar:remote_sugar",
        "//espresso/remote/java/androidx/test/espresso/web/webdriver:remote_webdriver_atoms",
        "//espresso/remote/javatests/androidx/test/espresso/proto:test_protos_android_pb3",
        "//espresso/web/java/androidx/test/espresso/web/action",
        "//espresso/web/java/androidx/test/espresso/web/assertion",
        "//espresso/web/java/androidx/test/espresso/web/matcher",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "//espresso/web/java/androidx/test/espresso/web/sugar",
        "//espresso/web/java/androidx/test/espresso/web/webdriver",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)
