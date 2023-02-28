# Description:
#   Sample UI tests.

load("//build_extensions:android_app_instrumentation_tests.bzl", "android_app_instrumentation_tests")
load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
    "gmscore_channel",
)

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//opensource/visibility:testing_pkg"],
)

android_library(
    name = "test_resources",
    assets = glob(["assets/**"]),
    assets_dir = "assets",
    manifest = "AndroidManifest.xml",
)

android_library(
    name = "local_manifest",
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
)

# Test classes that are supposed to run with only a subset of the sdks in DEVICES
# Don't forget to add targets for these!
SUPPRESSED_RANGE_TEST_CLASSES = [
    "DrawerActionsTest.java",
    "DuplicateViewActivityTest.java",
    "LongListMatchers.java",
    "MultipleWindowTest.java",
    "TransitionActivityMainTest.java",
    "TruncatedViewHierarchyActivityTest.java",
    "provider/FlightsContentProviderByRuleTest.java",
    "WebViewTest.java",
    "MainActivityTest.java",
]

FLAKY_TESTS = ["VerticalSwipeTest.java"]

android_library(
    name = "LongListMatchers",
    testonly = 1,
    srcs = ["LongListMatchers.java"],
    deps = [
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_neverlink",
        "@maven//:com_google_guava_guava",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_app_instrumentation_tests(
    name = "samples",
    size = "large",
    srcs = glob(
        include = ["**/*.java"],
        exclude = SUPPRESSED_RANGE_TEST_CLASSES + FLAKY_TESTS,
    ),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    tags = [
        "stable",
    ],
    deps = [
        ":LongListMatchers",
        ":local_manifest",
        "//core",
        "//espresso/accessibility/java/androidx/test/espresso/accessibility:accessibility_checks",
        "//espresso/contrib/java/androidx/test/espresso/contrib:activity_result_matchers",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:picker_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/idling_resource/java/androidx/test/espresso:counting_idling_resource",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:uri_idling_resource",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
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
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_core_core",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_app_instrumentation_tests(
    name = "DrawerActionsTest",
    size = "large",
    srcs = ["DrawerActionsTest.java"],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    device_list = devices(
        apis(min_api = 21),
    ),
    tags = [
        "stable",
    ],
    deps = [
        "//core",
        "//espresso/contrib/java/androidx/test/espresso/contrib:activity_result_matchers",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:picker_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/idling_resource/java/androidx/test/espresso:counting_idling_resource",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:uri_idling_resource",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
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
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_core_core",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_app_instrumentation_tests(
    name = "MultipleWindowTest",
    size = "large",
    srcs = ["MultipleWindowTest.java"],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    device_list = devices(
        apis(min_api = 24),
    ),
    tags = [
        "stable",
    ],
    deps = [
        "//core",
        "//espresso/accessibility/java/androidx/test/espresso/accessibility:accessibility_checks",
        "//espresso/contrib/java/androidx/test/espresso/contrib:activity_result_matchers",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:picker_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/idling_resource/java/androidx/test/espresso:counting_idling_resource",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:uri_idling_resource",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
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
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_app_instrumentation_tests(
    name = "TransitionActivityMainTest",
    size = "large",
    srcs = ["TransitionActivityMainTest.java"],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    device_list = devices(
        apis(min_api = 21),
    ),
    tags = [
        "stable",
    ],
    deps = [
        "//core",
        "//espresso/accessibility/java/androidx/test/espresso/accessibility:accessibility_checks",
        "//espresso/contrib/java/androidx/test/espresso/contrib:activity_result_matchers",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:picker_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/idling_resource/java/androidx/test/espresso:counting_idling_resource",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:uri_idling_resource",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
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
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_app_instrumentation_tests(
    name = "webview_tests",
    size = "large",
    srcs = [
        "MainActivityTest.java",
        "WebViewTest.java",
    ],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    device_list = devices(
        apis(
            # TODO(b/232962936) aosp api 26 does not include webview
            exclude_apis = [26],
        ),
    ) + devices(
        [26],
        gms_channel = gmscore_channel.PREBUILT,
    ),
    tags = [
        "stable",
    ],
    deps = [
        "//core",
        "//espresso/accessibility/java/androidx/test/espresso/accessibility:accessibility_checks",
        "//espresso/contrib/java/androidx/test/espresso/contrib:activity_result_matchers",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:picker_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/idling_resource/java/androidx/test/espresso:counting_idling_resource",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:uri_idling_resource",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
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
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

# TODO(b/150875830) Broken on API 27+. Add back to "samples" when fixed
# TODO(b/150644182) Additional failures on simulated device sponge/f40d8989-703a-4765-8bbc-78b9e3247303
android_app_instrumentation_tests(
    name = "provider/FlightsContentProviderByRuleTest",
    size = "large",
    srcs = ["provider/FlightsContentProviderByRuleTest.java"],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    device_list = devices(
        apis(max_api = 26),
    ),
    tags = [
        "stable",
    ],
    deps = [
        ":local_manifest",
        ":test_resources",
        "//core",
        "//espresso/accessibility/java/androidx/test/espresso/accessibility:accessibility_checks",
        "//espresso/contrib/java/androidx/test/espresso/contrib:activity_result_matchers",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:picker_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/idling_resource/java/androidx/test/espresso:counting_idling_resource",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:uri_idling_resource",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
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
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_core_core",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
    ],
)

test_suite(
    name = "sample_tests",
    tags = ["stable"],
)

android_app_instrumentation_tests(
    name = "VerticalSwipeTest",
    srcs = ["VerticalSwipeTest.java"],
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    tags = [
        # TODO(b/202177238): flaky
        "notap",
    ],
    deps = [
        ":LongListMatchers",
        ":local_manifest",
        "//core",
        "//espresso/accessibility/java/androidx/test/espresso/accessibility:accessibility_checks",
        "//espresso/contrib/java/androidx/test/espresso/contrib:activity_result_matchers",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:picker_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/idling_resource/java/androidx/test/espresso:counting_idling_resource",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:uri_idling_resource",
        "//espresso/intents/java/androidx/test/espresso/intent:intents",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
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
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_core_core",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
    ],
)
