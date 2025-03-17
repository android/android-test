# Description:
#   Tests for espresso actions

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
    name = "AdapterDataLoaderActionRemoteMsgTest",
    srcs =
        ["AdapterDataLoaderActionRemoteMsgTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/remote/java/androidx/test/espresso/action:remote_view_actions",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_hamcrest_v1_3_core_matchers",
        "//espresso/remote/java/androidx/test/espresso/proto/action:ui_interaction_view_actions_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "RemoteViewActionsTest",
    srcs =
        ["RemoteViewActionsTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/remote/java/androidx/test/espresso/action:remote_view_actions",
        "//espresso/remote/java/androidx/test/espresso/proto/action:ui_interaction_view_actions_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
