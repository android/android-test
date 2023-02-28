# Description:
#   Espresso - the new UI Test API for Android.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_ANNOTATION_VERSION", "ANNOTATION_VERSION", "CORE_VERSION", "ESPRESSO_VERSION", "HAMCREST_VERSION", "KOTLIN_VERSION", "RUNNER_VERSION")
load("//build_extensions:combine_jars.bzl", "combine_jars")
load("//build_extensions:remove_from_jar.bzl", "remove_from_jar")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

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
        "//opensource/dagger",
        "//runner/android_junit_runner",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_concurrent_concurrent_futures",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
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
        "//opensource/dagger",
        "//runner/android_junit_runner",
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
        "//espresso/core/java/androidx/test/espresso/screenshot",
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
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
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
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
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

axt_release_lib(
    name = "espresso_core_release",
    custom_package = "androidx.test.espresso.core",
    jarjar_rules = [
        "release_jarjar_rules.txt",
    ],
    # ensure to include kotlin_module since those define extension functions
    keep_spec = "androidx/test/espresso|META-INF/androidx.test.espresso.*.kotlin_module",
    overlapping_jars = [
        "//espresso/idling_resource/java/androidx/test/espresso:espresso_idling_resource_release_no_deps.jar",
    ],
    proguard_library = ":proguard_library.cfg",
    proguard_specs = [
        "//:proguard_binary.cfg",
        "//espresso:proguard_binary.cfg",
    ],
    remove_spec = "androidx/test/espresso/core/R[$$\\.]",
    deps = [
        ":espresso",
    ],
)

filegroup(
    name = "espresso_src",
    srcs = [
        ":libespresso-src.jar",
        ":libframework-src.jar",
        ":libinterface-src.jar",
        ":libview-interaction-src.jar",
        "//espresso/core/java/androidx/test/espresso/action:libaction-src.jar",
        "//espresso/core/java/androidx/test/espresso/action:libadapter_view_protocol-src.jar",
        "//espresso/core/java/androidx/test/espresso/assertion:libassertion-src.jar",
        "//espresso/core/java/androidx/test/espresso/base:espresso_base_src",
        "//espresso/core/java/androidx/test/espresso/matcher:libmatcher-src.jar",
        "//espresso/core/java/androidx/test/espresso/remote:espresso_remote_src",
        "//espresso/core/java/androidx/test/espresso/screenshot:libscreenshot-src.jar",
        "//espresso/core/java/androidx/test/espresso/util:libutil-src.jar",
        "//espresso/core/java/androidx/test/espresso/util/concurrent:libconcurrent-src.jar",
    ],
)

combine_jars(
    name = "espresso_src_all_jars",
    srcs = [
        ":espresso_src",
    ],
)

remove_from_jar(
    name = "espresso_src_jars",
    jar = ":espresso_src_all_jars.jar",
    keep_spec = "androidx/test/espresso",
    # Remove generated dagger classes from source jar
    remove_spec = ".*_Provide.*|.*_Factory.*",
)

dackka_test(
    name = "espresso_core_doc",
    runtime_dep = ":espresso",
    src_jar = ":espresso_src_jars.jar",
)

maven_artifact(
    name = "espresso_core_maven_artifact",
    src = ":espresso_core_release.aar",
    artifact_deps = [
        "androidx.annotation:annotation:%s" % ANDROIDX_ANNOTATION_VERSION,
        "androidx.test:core:%s" % CORE_VERSION,
        "androidx.test:runner:%s" % RUNNER_VERSION,
        "androidx.test.espresso:espresso-idling-resource:%s" % ESPRESSO_VERSION,
        "com.squareup:javawriter:2.1.1",
        "javax.inject:javax.inject:1",
        "org.hamcrest:hamcrest-library:%s" % HAMCREST_VERSION,
        "org.hamcrest:hamcrest-integration:%s" % HAMCREST_VERSION,
        "com.google.code.findbugs:jsr305:2.0.2",
        "org.jetbrains.kotlin:kotlin-stdlib:%s" % KOTLIN_VERSION,
        "androidx.test:annotation:%s" % ANNOTATION_VERSION,
    ],
    artifact_id = "espresso-core",
    group_id = "androidx.test.espresso",
    last_updated = "20170622000000",
    src_jar = ":espresso_src_jars.jar",
    version = "%s" % ESPRESSO_VERSION,
)

api_checks(
    name = "espresso_core_api",
    runtime_dep = ":espresso",
    src_jar = ":espresso_src_jars.jar",
)
