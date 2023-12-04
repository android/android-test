# Description:
#   Tests for espresso contrib classes

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load("//build_extensions:jetify.bzl", "jetify_android_library")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])
## Android tests

jetify_android_library(
    name = "recycler_view_test_baseclass",
    testonly = 1,
    srcs = ["RecyclerViewIntegrationTest.java"],
    deps = [
        "//core",
        "//espresso/contrib/java/androidx/test/espresso/contrib:recycler_view",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib",
        "//testapps/ui_testapp/javatests/androidx/test/ui/app:test_resources",
        "@maven//:androidx_recyclerview_recyclerview",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "AccessibilityChecksIntegrationTest",
    srcs =
        ["AccessibilityChecksIntegrationTest.java"],
    deps = [
        "//core",
        "//espresso/contrib/java/androidx/test/espresso/contrib:accessibility_checks",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "ActivityResultMatchersTest",
    srcs =
        ["ActivityResultMatchersTest.java"],
    deps = [
        "//espresso/contrib/java/androidx/test/espresso/contrib:activity_result_matchers",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "DrawerActionsIntegrationTest",
    srcs =
        ["DrawerActionsIntegrationTest.java"],
    deps = [
        "//core",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "NavigationViewActionsIntegrationTest",
    srcs =
        ["NavigationViewActionsIntegrationTest.java"],
    deps = [
        "//core",
        "//espresso/contrib/java/androidx/test/espresso/contrib:design_lib_actions",
        "//espresso/contrib/java/androidx/test/espresso/contrib:drawer_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:androidx_core_core",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "RecyclerViewGridLayoutManagerTest",
    srcs =
        ["RecyclerViewGridLayoutManagerTest.java"],
    deps = [
        ":recycler_view_test_baseclass",
        "//espresso/contrib/java/androidx/test/espresso/contrib:recycler_view",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "RecyclerViewLinearLayoutManagerTest",
    srcs =
        ["RecyclerViewLinearLayoutManagerTest.java"],
    deps = [
        ":recycler_view_test_baseclass",
        "//espresso/contrib/java/androidx/test/espresso/contrib:recycler_view",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:androidx_recyclerview_recyclerview",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "RecyclerViewStaggeredGridLayoutManagerTest",
    srcs =
        ["RecyclerViewStaggeredGridLayoutManagerTest.java"],
    deps = [
        ":recycler_view_test_baseclass",
        "//espresso/contrib/java/androidx/test/espresso/contrib:recycler_view",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "ViewPagerActionsIntegrationTest",
    srcs =
        ["ViewPagerActionsIntegrationTest.java"],
    deps = [
        "//espresso/contrib/java/androidx/test/espresso/contrib:view_pager_actions",
        "//espresso/core/java/androidx/test/espresso",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
