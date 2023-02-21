load("@build_bazel_rules_android//android:rules.bzl", "android_library")

# ** Generate the release artifacts **
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_ANNOTATION_VERSION", "ESPRESSO_VERSION")
load("//build_extensions:combine_jars.bzl", "combine_jars")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

android_library(
    name = "espresso_web_release_lib",
    exports = [
        "//espresso/web/java/androidx/test/espresso/web/action",
        "//espresso/web/java/androidx/test/espresso/web/assertion",
        "//espresso/web/java/androidx/test/espresso/web/matcher",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "//espresso/web/java/androidx/test/espresso/web/sugar",
        "//espresso/web/java/androidx/test/espresso/web/webdriver",
    ],
)

axt_release_lib(
    name = "espresso_web_release",
    jarjar_rules = ["release_jarjar_rules.txt"],
    keep_spec = "androidx.test.espresso.web",
    proguard_specs = [
        "//:proguard_binary.cfg",
        "//espresso:proguard_binary.cfg",
    ],
    remove_spec = "androidx/test/espresso/web/R[$$\\.]",
    deps = [
        ":espresso_web_release_lib",
    ],
)

filegroup(
    name = "web_src",
    srcs = [
        "//espresso/web/java/androidx/test/espresso/web/action:libaction-src.jar",
        "//espresso/web/java/androidx/test/espresso/web/assertion:libassertion-src.jar",
        "//espresso/web/java/androidx/test/espresso/web/bridge:libbridge-src.jar",
        "//espresso/web/java/androidx/test/espresso/web/matcher:libmatcher-src.jar",
        "//espresso/web/java/androidx/test/espresso/web/model:libmodel-src.jar",
        "//espresso/web/java/androidx/test/espresso/web/sugar:libsugar-src.jar",
        "//espresso/web/java/androidx/test/espresso/web/webdriver:libwebdriver-src.jar",
    ],
)

combine_jars(
    name = "web_src_jars",
    srcs = [
        ":web_src",
    ],
)

dackka_test(
    name = "espresso_web_doc",
    jar = ":espresso_web_release_all_deploy.jar",
    src_jar = ":web_src_jars.jar",
)

maven_artifact(
    name = "espresso_web_maven_artifact",
    src = ":espresso_web_release.aar",
    artifact_deps = [
        "androidx.test.espresso:espresso-core:%s" % ESPRESSO_VERSION,
        "androidx.annotation:annotation:%s" % ANDROIDX_ANNOTATION_VERSION,
        "org.ccil.cowan.tagsoup:tagsoup:1.2.1",
    ],
    artifact_id = "espresso-web",
    group_id = "androidx.test.espresso",
    last_updated = "20170622000000",
    src_jar = ":web_src_jars.jar",
    version = "%s" % ESPRESSO_VERSION,
)

api_checks(
    name = "espresso_web_api",
    deploy_jar = ":espresso_web_release_all_deploy.jar",
    src_jar = ":web_src_jars.jar",
)
