# Description:
#   Tests for Espresso Intents lib.

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
    name = "BundleMatchersTest",
    srcs = ["BundleMatchersTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "ComponentNameMatchersTest",
    srcs = ["ComponentNameMatchersTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "IntentMatchersTest",
    srcs = ["IntentMatchersTest.java"],
    shard_count = 3,
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent:resolved_intent_interface",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "RuntimePermissionMatchersTest",
    srcs = ["RuntimePermissionMatchersTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "UriMatchersTest",
    srcs = ["UriMatchersTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
