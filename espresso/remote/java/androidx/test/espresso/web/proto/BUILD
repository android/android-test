# Protocol buffer definitions for web remote UI interactions

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/remote:__subpackages__"],
)

# gather up the package-infos which hide the proto generated code from public API
java_library(
    name = "package-infos",
    exports = [
        "//espresso/remote/java/androidx/test/espresso/web/proto/action:package-info",
        "//espresso/remote/java/androidx/test/espresso/web/proto/assertion:package-info",
        "//espresso/remote/java/androidx/test/espresso/web/proto/matcher:package-info",
        "//espresso/remote/java/androidx/test/espresso/web/proto/model:package-info",
        "//espresso/remote/java/androidx/test/espresso/web/proto/sugar:package-info",
        "//espresso/remote/java/androidx/test/espresso/web/proto/webdriver:package-info",
    ],
)
