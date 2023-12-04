# Description: Tests for espresso web

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

axt_android_library_test(
    name = "ModelCodecTest",
    srcs = ["ModelCodecTest.java"],
    deps = [
        "//core",
        "//espresso/core",
        "//espresso/web",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

axt_android_library_test(
    name = "SimpleAtomTest",
    srcs = ["SimpleAtomTest.java"],
    deps = [
        "//core",
        "//espresso/core",
        "//espresso/web",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:junit_junit",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)
