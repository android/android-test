load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# Description:
#   Contributed / non-core functionality of Espresso. Tools which add on to
#   Espresso belong here.
licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

COMMON_JAVACOPTS = [
    "-Xep:ObjectToString:OFF",  # See http://go/objecttostring-lsc
]

android_library(
    name = "resources",
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
)

android_library(
    name = "counting_idling_resource",
    visibility = ["//visibility:public"],
    exports = [
        "//espresso/idling_resource/java/androidx/test/espresso:counting_idling_resource",
    ],
)

android_library(
    name = "scheduled_thread_pool_idling_resource",
    visibility = ["//visibility:public"],
    exports = [
        "//espresso/idling_resource/concurrent/java/androidx/test/espresso/idling/concurrent:scheduled_thread_pool_idling_resource",
    ],
)

android_library(
    name = "thread_pool_idling_resource",
    visibility = ["//visibility:public"],
    exports = [
        "//espresso/idling_resource/concurrent/java/androidx/test/espresso/idling/concurrent:thread_pool_idling_resource",
    ],
)

android_library(
    name = "picker_actions",
    srcs = ["PickerActions.java"],
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

COMMON_JAVACOPTS = [
    "-Xep:ObjectToString:OFF",  # See http://go/objecttostring-lsc
]

android_library(
    name = "recycler_view",
    srcs = ["RecyclerViewActions.java"],
    javacopts = COMMON_JAVACOPTS,
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//runner/monitor",
        "@maven//:androidx_recyclerview_recyclerview",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_library(
    name = "drawer_actions",
    testonly = 1,
    srcs = [
        "DrawerActions.java",
        "DrawerMatchers.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        ":resources",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "@maven//:androidx_core_core",
        "@maven//:androidx_drawerlayout_drawerlayout",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

android_library(
    name = "activity_result_matchers",
    srcs = [
        "ActivityResultMatchers.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

android_library(
    name = "accessibility_checks",
    testonly = 1,
    srcs = [
        "AccessibilityChecks.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

android_library(
    name = "design_lib_actions",
    testonly = 1,
    srcs = ["NavigationViewActions.java"],
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_drawerlayout_drawerlayout",
        "@maven//:com_google_android_material_material",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_library(
    name = "view_pager_actions",
    testonly = 1,
    srcs = ["ViewPagerActions.java"],
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "@maven//:androidx_viewpager_viewpager",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

android_library(
    name = "espresso_contrib_aar_lib",
    testonly = 1,
    manifest = "AndroidManifest.xml",
    resource_files = glob(["res/**"]),
    exports = [
        ":accessibility_checks",
        ":activity_result_matchers",
        ":design_lib_actions",
        ":drawer_actions",
        ":picker_actions",
        ":recycler_view",
        ":view_pager_actions",
    ],
)

axt_android_aar(
    name = "espresso_contrib_aar",
    testonly = 1,
    expected_class_prefixes = [
        "androidx.test.espresso.contrib",
    ],
    included_dep = ":espresso_contrib_aar_lib",
)

maven_artifact(
    name = "espresso_contrib_maven_artifact",
    testonly = 1,
    last_updated = "20170622000000",
    target = ":espresso_contrib_aar",
)

dackka_test(
    name = "espresso_contrib_doc",
    runtime_dep = ":espresso_contrib_aar_lib",
    src_jar = ":espresso_contrib_aar-src.jar",
)

api_checks(
    name = "espresso_contrib_api",
    runtime_dep = ":espresso_contrib_aar_lib",
    src_jar = ":espresso_contrib_aar-src.jar",
)
