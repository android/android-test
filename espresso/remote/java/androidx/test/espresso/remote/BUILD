# Description:
#  Remote package for espresso.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions:api_checks.bzl", "api_checks")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

COMMON_JAVACOPTS = [
    "-Xep:ObjectToString:OFF",  # See http://go/objecttostring-lsc
]

REMOTE_REGISTRY = [
    "RemoteDescriptorRegistry.java",
    "RemoteDescriptor.java",
    "FieldDescriptor.java",
]

android_library(
    name = "registry",
    srcs = REMOTE_REGISTRY,
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/remote:reflectionUtils",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/remote/java/androidx/test/espresso/proto:ui_interaction_android_pb3",
        "//runner/monitor",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

PROTO_UTILS = [
    "Converter.java",
    "AnyToTypeConverter.java",
    "TypeToAnyConverter.java",
    "ByteStringToTypeConverter.java",
    "TypeToByteStringConverter.java",
    "ParcelableToByteStringConverter.java",
    "ByteStringToParcelableConverter.java",
    "TypeProtoConverters.java",
    "ProtoUtils.java",
    "InteractionRequest.java",
    "GenericRemoteMessage.java",
    "RemoteMessageSerializer.java",
    "RemoteMessageDeserializer.java",
    "BuilderReflector.java",
    "ProtoReflector.java",
]

android_library(
    name = "protoUtils",
    srcs = PROTO_UTILS,
    visibility = ["//visibility:public"],
    deps = [
        ":registry",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/remote:aidls",
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/remote:reflectionUtils",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/remote/java/androidx/test/espresso/proto:ui_interaction_android_pb3",
        "//opensource/proto:any_java_proto_lite",
        "//runner/android_junit_runner",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

android_library(
    name = "remote",
    srcs = glob(
        ["*.java"],
        exclude = PROTO_UTILS + REMOTE_REGISTRY,
    ),
    javacopts = COMMON_JAVACOPTS,
    manifest = "AndroidManifest.xml",
    proguard_specs = [
        ":proguard_library.cfg",
    ],
    visibility = ["//visibility:public"],
    deps = [
        ":protoUtils",
        ":registry",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote:aidls",
        "//espresso/core/java/androidx/test/espresso/remote:bindable",
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/remote:reflectionUtils",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/remote/java/androidx/test/espresso:data-interaction-remote",
        "//espresso/remote/java/androidx/test/espresso/action:remote_view_actions",
        "//espresso/remote/java/androidx/test/espresso/assertion:remote_view_assertions",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_hamcrest_v1_3_core_matchers",
        "//espresso/remote/java/androidx/test/espresso/matcher:remote_view_matchers",
        "//espresso/remote/java/androidx/test/espresso/proto:package-infos",  # unuseddeps: keep
        "//espresso/remote/java/androidx/test/espresso/proto:ui_interaction_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/proto/matcher:ui_interaction_view_matchers_android_pb3",
        "//espresso/remote/java/androidx/test/espresso/web/action:remote_web_actions",
        "//espresso/remote/java/androidx/test/espresso/web/assertion:remote_web_assertions",
        "//espresso/remote/java/androidx/test/espresso/web/matcher:remote_web_matchers",
        "//espresso/remote/java/androidx/test/espresso/web/model:remote_web_model_atoms",
        "//espresso/remote/java/androidx/test/espresso/web/proto:package-infos",  # unuseddeps: keep
        "//espresso/remote/java/androidx/test/espresso/web/sugar:remote_sugar",
        "//espresso/remote/java/androidx/test/espresso/web/webdriver:remote_webdriver_atoms",
        "//runner/android_junit_runner",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

axt_android_aar(
    name = "espresso_remote_aar",
    expected_class_prefixes = [
        "androidx.test.espresso",
    ],
    included_dep = ":remote",
    jarjar_rule = "aar_jarjar_rules.txt",
)

maven_artifact(
    name = "espresso_remote_maven_artifact",
    last_updated = "20170622000000",
    target = ":espresso_remote_aar",
)

dackka_test(
    name = "espresso_remote_doc",
    runtime_dep = ":remote",
    src_jar = ":espresso_remote_aar-src.jar",
)

api_checks(
    name = "espresso_remote_api",
    runtime_dep = ":remote",
    src_jar = ":espresso_remote_aar-src.jar",
)
