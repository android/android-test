# Protocol buffer definitions for remote web driver atoms

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = [
        "//espresso/core/java/androidx/test/espresso:espresso_pkg",
        "//espresso/web/java/androidx/test/espresso/web/webdriver:__pkg__",
    ],
)

proto_library(
    name = "web_driver_atoms_proto",
    srcs = ["web_webdriver_atoms.proto"],
    deps = [
        "@com_google_protobuf//:any_proto",
    ],
)

java_lite_proto_library(
    name = "web_driver_atoms_proto_android_pb3",
    deps = [":web_driver_atoms_proto"],
)
