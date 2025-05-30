load("@build_bazel_rules_android//android:rules.bzl", "android_binary")
load("//build_extensions:jetify.bzl", "jetify_android_library")

# Description:
#   Common library for testing inter and intra app communication via intents.
licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = ["//:__subpackages__"],
)

# Genrule that creates a Java classes with 32768 methods
[genrule(
    name = "MultiDexTestClass%s" % s,
    outs = ["MultiDexTestClass%s.java" % s],
    cmd = "echo 'package androidx.test.multidex.app;'>$@;" +
          "echo 'public class MultiDexTestClass%s {'>>$@;" % s +
          "for i in `seq 1 32768`;" +
          "do echo 'public static int method'$$i'() {return 42;}'>>$@;" +
          "done;" +
          "echo '}'>>$@",
) for s in [
    "A",
    "B",
    "C",
    "D",
]]

jetify_android_library(
    name = "src",
    testonly = True,
    srcs = [
        "MultiDexActivity.java",
        "MultiDexApplication.java",
        ":MultiDexTestClassA",
        ":MultiDexTestClassB",
        ":MultiDexTestClassC",
        ":MultiDexTestClassD",
    ],
    jetify_sources = True,
    deps = ["@maven//:androidx_multidex_multidex"],
)

android_binary(
    name = "multidex_testapp",
    testonly = True,
    main_dex_list = "main_dex_list.txt",
    manifest = "AndroidManifest.xml",
    multidex = "manual_main_dex",
    resource_files = glob(["res/**"]),
    deps = [":src"],
)
