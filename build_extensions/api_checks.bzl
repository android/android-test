"""A macro for generating androidx.test api definitions and checks."""

# TODO: implement me

def api_checks(
        name,
        runtime_dep,
        src_jar,
        testonly = 1):
    """Generates api definitions and checks for a macro.

    This macro will generate two api definition files, one for public api, one for internal RestrictTo(Scope.LIBRARY_GROUP) apis,
    and generate diff checks to compare them against api/current-public.txt and api/current-internal.txt respectively.

    Args:
      name: name of rule
      runtime_dep: runtime java dependency of the srcs
      src_jar: contains the source to generate api for
      testonly: the testonly restriction. Default 1
   """
