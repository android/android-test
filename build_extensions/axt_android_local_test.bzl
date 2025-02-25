"""A rule wrapper for generating android_local_test ."""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

_CONFIG_JAR_COMMAND = """
set -e
JAR="$(location @local_jdk//:jar)"
SRC="$<"
[[ "$$(basename "$${SRC}")" = 'robolectric.properties' ]] || {
  echo 'Must be named: robolectric.properties';
  exit 1;
}
$${JAR} -cf "$@" -C "$$(dirname "$${SRC}")" "$$(basename "$${SRC}")"
"""

def axt_android_local_test(name, srcs = [], deps = [], manifest = "//build_extensions:AndroidManifest_robolectric.xml", tags = ["robolectric"], **kwargs):
    """A wrapper around android_local_test that provides sensible defaults for androidx.test.


    Args:
      name: the name to use for the generated android_local_test rule.
      srcs: the test sources to generate rules for
      deps: the build dependencies to use for the generated local test
      manifest: the android manifest. Default: AndroidManifest_robolectric.xml
      tags: the tags to pass to android_local_test. Default ["robolectric"].
        If overridden, it is recommended to pass the "robolectric" tag so
        the test gets executed on github CI
      **kwargs: arguments to pass to generated android_local_test rules
    """

    _robolectric_config(
        name = "%s_config" % name,
        src = "//build_extensions:robolectric.properties",
    )
    deps = depset(deps + [
        "%s_config" % name,
        # the blaze-robolectric target exports these by default, so export them here too for consistency
        "@robolectric//bazel:android-all",
        "@maven//:org_robolectric_robolectric",
        "@maven//:org_robolectric_shadows_framework",
        "@maven//:org_robolectric_shadowapi",
        "@maven//:org_robolectric_annotations",
        "//ext/junit",
        "//core",
    ]).to_list()

    if _is_kotlin(srcs):
        kt_android_library(
            name = "%s_kt_lib" % name,
            srcs = srcs,
            exports_manifest = True,
            manifest = manifest,
            deps = deps,
            testonly = 1,
        )
        deps = [":%s_kt_lib" % name]
        srcs = []

    native.android_local_test(
        name = name,
        srcs = srcs,
        tags = tags,
        manifest = manifest,
        deps = deps,
        **kwargs
    )

def _robolectric_config(name, src):
    """Creates a JAR file containing the given Robolectric properties file at the top level.

    Args:
      name: a string, the name of the rule
      src: a label, the properties file to package
    """
    native.genrule(
        name = name + "_gen",
        srcs = [src],
        outs = ["%s.jar" % name],
        message = "Generating Robolectric config...",
        cmd = _CONFIG_JAR_COMMAND,
        tools = [
            "@local_jdk//:jar",
        ],
        visibility = ["//visibility:private"],
    )
    native.java_import(
        name = name,
        constraints = ["android"],
        jars = [name + "_gen"],
    )

def _is_kotlin(srcs):
    for s in srcs:
        if s.endswith(".kt"):
            return True
    return False
