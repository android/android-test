# Description:
#   Sample multi-process tests.

load("//build_extensions:axt_android_application_test.bzl", "axt_android_application_test")
load(
    "//build_extensions:phone_devices.bzl",
    "devices",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

# Tests
# =========================================================

DEVICES = devices([23])

axt_android_application_test(
    name = "DefaultProcessActivityTest",
    srcs =
        ["DefaultProcessActivityTest.java"],
    device_list = DEVICES,
    instruments = "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:testapp",
    manifest = "AndroidManifest.xml",
    tags = ["integration"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/remote/java/androidx/test/espresso/remote",
        "//ext/junit",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:lib",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_application_test(
    name = "GlobalProcessActivityTest",
    srcs =
        ["GlobalProcessActivityTest.java"],
    device_list = DEVICES,
    instruments = "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:testapp",
    manifest = "AndroidManifest_global.xml",
    tags = ["integration"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/remote/java/androidx/test/espresso/remote",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:lib",
        "@maven//:junit_junit",
    ],
)

axt_android_application_test(
    name = "IdlingActivityTest",
    srcs =
        ["IdlingActivityTest.java"],
    device_list = DEVICES,
    instruments = "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:testapp",
    manifest = "AndroidManifest_idling.xml",
    tags = ["integration"],
    deps = [
        "//espresso/core",
        "//espresso/remote/java/androidx/test/espresso/remote",
        "//ext/junit",
        "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:lib",
        "@maven//:junit_junit",
    ],
)

axt_android_application_test(
    name = "MainActivityTest",
    srcs =
        ["MainActivityTest.java"],
    device_list = DEVICES,
    instruments = "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:testapp",
    manifest = "AndroidManifest.xml",
    tags = ["integration"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/remote/java/androidx/test/espresso/remote",
        "//espresso/web/java/androidx/test/espresso/web/action",
        "//espresso/web/java/androidx/test/espresso/web/assertion",
        "//espresso/web/java/androidx/test/espresso/web/matcher",
        "//espresso/web/java/androidx/test/espresso/web/sugar",
        "//ext/junit",
        "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:lib",
        "@maven//:junit_junit",
    ],
)

axt_android_application_test(
    name = "PrivateProcessActivityTest",
    srcs =
        ["PrivateProcessActivityTest.java"],
    device_list = DEVICES,
    instruments = "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:testapp",
    manifest = "AndroidManifest_private.xml",
    tags = ["integration"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/remote/java/androidx/test/espresso/remote",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:lib",
        "@maven//:junit_junit",
    ],
)

axt_android_application_test(
    name = "WebViewActivityTest",
    srcs =
        ["WebViewActivityTest.java"],
    device_list = DEVICES,
    instruments = "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:testapp",
    manifest = "AndroidManifest.xml",
    tags = ["integration"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/remote/java/androidx/test/espresso/remote",
        "//espresso/web/java/androidx/test/espresso/web/action",
        "//espresso/web/java/androidx/test/espresso/web/assertion",
        "//espresso/web/java/androidx/test/espresso/web/matcher",
        "//espresso/web/java/androidx/test/espresso/web/sugar",
        "//espresso/web/java/androidx/test/espresso/web/webdriver",
        "//ext/junit",
        "//testapps/multiprocess_testapp/java/androidx/test/multiprocess/app:lib",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
