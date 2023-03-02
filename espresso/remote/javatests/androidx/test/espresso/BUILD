load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(default_applicable_licenses = ["//:license"])

axt_android_library_test(
    name = "DisplayDataMatcherRemoteMsgTest",
    srcs = ["DisplayDataMatcherRemoteMsgTest.java"],
    deps = [
        "//espresso/core",
        "//espresso/remote/java/androidx/test/espresso:data-interaction-remote",
        "//espresso/remote/java/androidx/test/espresso/action:remote_view_actions",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_hamcrest_v1_3_core_matchers",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_view_matchers",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:ui_interaction_view_matchers_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
    ],
)
