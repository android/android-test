load("@rules_kotlin//kotlin:jvm.bzl", "kt_jvm_binary", "kt_jvm_library")

package(
    default_visibility = [
        "//:__subpackages__",
    ],
)

kt_jvm_library(
    name = "release_updater_lib",
    srcs = glob([
        "ReleaseUpdater.kt",
    ]),
)

kt_jvm_binary(
    name = "releaseupdater",
    srcs = ["Main.kt"],
    main_class = "androidx.test.tools.releaseupdater.Main",
    resources = [
        "//build_extensions:axt_released_versions.bzl",
        "//build_extensions:axt_versions.bzl",
    ],
    deps = [
        ":release_updater_lib",
    ],
)
