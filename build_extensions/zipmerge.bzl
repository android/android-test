"""Combines multiple zips into one zip."""

def zipmerge(name, srcs):
    """Macro wrapper for zipmerge

    Args:
      name: Name to be used for this rule. It produces name.zip
      srcs: List of zips to be combined.
    """

    native.genrule(
        name = name,
        srcs = srcs,
        outs = ["%s.zip" % name],
        tools = ["//third_party/libzip:zipmerge"],
        message = "Combining following zips: %s" % ",".join(srcs),
        cmd = (
            "$(location //third_party/libzip:zipmerge) -s $@ %s" % (" ".join(srcs))
        ),
    )
