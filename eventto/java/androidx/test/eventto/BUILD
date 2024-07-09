load("@build_bazel_rules_android//android:rules.bzl", "android_library")

android_library(
    name = "eventto",
    testonly = 1,
    srcs = glob(["*.java"]),
    visibility = [
        "//eventto:__pkg__",
    ],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso:espresso_compiletime",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/util",
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:javax_inject_javax_inject",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)
