# Description:
#   Espresso - the new UI Test API for Android.

load("//build_extensions:api_checks.bzl", "api_checks")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

# Add only espresso packages here.
package_group(
    name = "espresso_pkg",
    packages = [
        "//espresso/...",
    ],
)

package_group(
    name = "espresso_first_party",
    packages = [
    ],
)

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

exports_files(["AndroidManifest.xml"])

PUBLIC_FRAMEWORK_LAYER = [
    "ViewAction.java",
    "ViewAssertion.java",
]

BASE_LAYER_INTERFACES = [
    "AmbiguousViewMatcherException.java",
    "AppNotIdleException.java",
    "NoActivityResumedException.java",
    "NoMatchingRootException.java",
    "NoMatchingViewException.java",
    "EspressoException.java",
    "FailureHandler.java",
    "IdlingPolicies.java",
    "IdlingPolicy.java",
    "IdlingResourceTimeoutException.java",
    "InjectEventSecurityException.java",
    "PerformException.java",
    "Root.java",
    "RootViewException.java",
    "ViewFinder.java",
    "UiController.java",
]

VIEW_INTERACTION = [
    "ViewInteraction.java",
    "ViewInteractionComponent.java",
    "ViewInteractionModule.java",
    "InteractionResultsHandler.java",
]

android_library(
    name = "view-interaction",
    srcs = VIEW_INTERACTION,
    visibility = [":espresso_pkg"],
    deps = [
        ":framework",
        ":interface",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/internal/data",
        "//espresso/core/java/androidx/test/espresso/internal/data/model",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote:aidls",
        "//espresso/core/java/androidx/test/espresso/remote:bindable",
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/core/java/androidx/test/espresso/util/concurrent",
        "//opensource/androidx:annotation",
        "//opensource/dagger",
        "//runner/android_junit_runner",
        "@maven//:androidx_concurrent_concurrent_futures",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)

ESPRESSO_GRAPH = [
    "GraphHolder.java",
    "BaseLayerComponent.java",
]

android_library(
    name = "espresso-graph",
    srcs = ESPRESSO_GRAPH,
    visibility = [
        ":espresso_first_party",
    ],
    deps = [
        ":interface",
        ":view-interaction",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:default_failure_handler",
        "//espresso/core/java/androidx/test/espresso/base:idling_resource_registry",
        "//espresso/core/java/androidx/test/espresso/internal/data",
        "//espresso/core/java/androidx/test/espresso/util/concurrent",
        "//opensource/androidx:annotation",
        "//opensource/dagger",
        "//runner/android_junit_runner",
        "@maven//:javax_inject_javax_inject",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

android_library(
    name = "espresso",
    srcs = glob(
        ["*.java"],
        exclude = BASE_LAYER_INTERFACES + PUBLIC_FRAMEWORK_LAYER + ESPRESSO_GRAPH + VIEW_INTERACTION,
    ),
    exports = [
        # Anything listed here is instantly available to targets that
        # depend on :espresso.
        #
        # Only list items here which are required to have a 'useful'
        # cut of espresso.
        #
        # For example: //j/c/g/a/a/c/testing/ui/espresso/action
        # sure you can use espresso without this dependency, but 98% of users
        # will include it because its such a key piece of functionality.
        ":framework",
        ":interface",
        ":view-interaction",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/action:adapter_view_protocol",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
    ],
    deps = [
        ":espresso-graph",
        ":framework",
        ":interface",
        ":view-interaction",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/action:adapter_view_protocol",
        "//espresso/core/java/androidx/test/espresso/base:idling_resource_registry",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote:reflectionUtils",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/core/java/androidx/test/espresso/util/concurrent",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

android_library(
    name = "framework",
    srcs = PUBLIC_FRAMEWORK_LAYER,
    deps = [
        ":interface",
        "//espresso/core/java/androidx/test/espresso/util",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

android_library(
    name = "interface",
    srcs = BASE_LAYER_INTERFACES,
    manifest = "AndroidManifest.xml",
    deps = [
        "//annotation",
        "//espresso/core/java/androidx/test/espresso/util",
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

# Compile-time library is used by tests, to avoid including the project code into test apk.
# By doing this we significantly reduce test apk filesize.
# Test code classpath is overlayed with project classpath on Android.
android_library(
    name = "espresso_compiletime",
    testonly = 1,
    neverlink = 1,
    exports = [":espresso"],
)

android_library(
    name = "espresso_aar_lib",
    custom_package = "androidx.test.espresso.core",
    manifest = "AndroidManifest.xml",
    proguard_specs = [":proguard_library.cfg"],
    visibility = ["//visibility:private"],
    exports = [
        ":espresso",
        "//espresso/core/java/androidx/test/espresso/core/internal/deps/dagger",
    ],
)

axt_android_aar(
    name = "espresso_core_aar",
    expected_class_prefixes = [
        "androidx.test.espresso",
    ],
    included_dep = ":espresso_aar_lib",
    jarjar_rule =
        "release_jarjar_rules.txt",
)

maven_artifact(
    name = "espresso_core_maven_artifact",
    last_updated = "20170622000000",
    target = ":espresso_core_aar",
)

dackka_test(
    name = "espresso_core_doc",
    runtime_dep = ":espresso_aar_lib",
    src_jar = ":espresso_core_aar-src.jar",
)

api_checks(
    name = "espresso_core_api",
    runtime_dep = ":espresso_aar_lib",
    src_jar = ":espresso_core_aar-src.jar",
)
