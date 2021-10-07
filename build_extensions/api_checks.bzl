"""A macro for generating androidx.test api definitions and checks."""

load(
    "//third_party/android/androidx_test/build_extensions:generate_api.bzl",
    "generate_api",
)
load("//tools/build_defs/golden_test:def.bzl", "golden_test")

def api_checks(
        name,
        deploy_jar,
        src_jar):
    """Generates api definitions and checks for a macro.

    This macro will generate two api definition files, one for public api, one for internal RestrictTo(Scope.LIBRARY_GROUP) apis,
    and generate diff checks to compare them against api/current-public.txt and api/current-internal.txt respectively.

    Args:
      name: name of rule
      deploy_jar: runtime jar that contains the compiled source classea and all their dependencies
      src_jar: contains the source to generate api for
   """

    generate_api(
        name = "%s_public_api" % name,
        jar = deploy_jar,
        src_jar = src_jar,
    )

    golden_test(
        name = "%s_public_api_diff_test" % name,
        size = "small",
        error_message = "\nEither update current_public.txt (run command above) or remove the API diff detected.\n",
        golden = "api/current_public.txt",
        subject = ":%s_public_api.txt" % name,
        verbose = True,
    )

    generate_api(
        name = "%s_internal_api" % name,
        jar = deploy_jar,
        src_jar = src_jar,
        internal = True,
    )

    golden_test(
        name = "%s_internal_api_diff_test" % name,
        size = "small",
        error_message = "\nEither update current_internal.txt (run command above) or remove the API diff detected.\n",
        golden = "api/current_internal.txt",
        subject = ":%s_internal_api.txt" % name,
        verbose = True,
    )
