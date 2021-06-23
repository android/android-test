"""Conditionally rewrites android.support.test references to androidx.test.

It is intended for modifying references to android.support.test
in prebuilt java libraries, to point to their renamed androidx.test equivalent.

This functionality is guarded by a flag, so initially use of this function
will have no effect.

See go/jetpack-test-lsc
"""

def atsl_to_axt_jarjar(name, src_jar, out_jar, **kwargs):
    # this will be swapped to "atslToAxtJarJarRules.txt" in the ATSL -> AXT LSC
    JARJAR_RULES = "//third_party/android/androidx_test/build_extensions:atslToAxtJarJarRules.txt"

    # TODO(b/78906684): use jetifier instead of jarjar
    native.genrule(
        name = name,
        srcs = [src_jar],
        outs = [out_jar],
        cmd = ("$(location //third_party/java/jarjar:jarjar_bin) process " +
               "$(location %s) '$<' '$@'" % JARJAR_RULES),
        tools = [
            JARJAR_RULES,
            "//third_party/java/jarjar:jarjar_bin",
        ],
        **kwargs
    )
