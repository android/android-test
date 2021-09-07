"""Skylark rule for generation of docs and API TXT for an android_library."""

def _transitive_deps(java):
    """Returns a list of the  transitive java dependencies."""
    return java.transitive_deps.to_list()

def _basename(filename):
    return filename.split("/")[-1]

def _dirname(filename):
    return filename.rsplit("/", 1)[0] if "/" in filename else "."

def _droiddoc_impl(ctx):
    """Generates javadoc and API txt for android_library."""
    src_jars = []
    dep_jars = []

    for src_jar in ctx.attr.src_jars:
        src_jars.extend(src_jar.files.to_list())

    for dep in ctx.attr.deps:
        dep_jars.extend(_transitive_deps(dep.java))

    dep_jars += ctx.files._android_jar

    src_jar_paths = [src_jar.path for src_jar in src_jars]

    extra_flags = []
    extra_inputs = []
    if ctx.attr.packages:
        extra_flags.append("--packages=%s" % " ".join(ctx.attr.packages))

    if ctx.attr.federation_project:
        if not ctx.attr.federation_url:
            fail("federation_url must be set when federation_project set")
        if not ctx.attr.federation_api_txt:
            fail("federation_api_txt must be set when federation_project set")

        extra_flags.append("--federation_project=%s" % ctx.attr.federation_project)
        extra_flags.append("--federation_url=%s" % ctx.attr.federation_url)
        extra_flags.append(
            "--federation_api_txt=%s" % ctx.file.federation_api_txt.path,
        )
        extra_inputs.append(ctx.file.federation_api_txt)
    else:
        if ctx.attr.federation_url:
            fail("federation_project must be set when federation_url set")
        if not ctx.attr.federation_api_txt:
            fail("federation_project must be set when federation_api_txt set")

    if ctx.attr.devsite:
        extra_flags.append("--devsite=true")
        extra_flags.append("--yamlV2=true")

    ctx.actions.run(
        inputs = src_jars + dep_jars + extra_inputs + [ctx.executable._droiddoc],
        outputs = [ctx.outputs.docs, ctx.outputs.api],
        arguments = [
            "--classpath=%s" % cmd_helper.join_paths(":", depset(dep_jars)),
            "--output=%s" % ctx.outputs.docs.path,
            "--api_output=%s" % ctx.outputs.api.path,
            "--dirname=%s" % ctx.label.name,
        ] + extra_flags + src_jar_paths,
        executable = ctx.executable._droiddoc,
        progress_message = "Generating javadoc: %s" % ctx.outputs.docs.short_path,
    )

droiddoc = rule(
    implementation = _droiddoc_impl,
    attrs = {
        # List of source jars to document.
        "src_jars": attr.label_list(
            mandatory = True,
            allow_empty = False,
            allow_files = [".jar"],
        ),
        # List of targets to add to classpath when generating javadoc
        "deps": attr.label_list(
            mandatory = True,
            allow_empty = False,
            allow_rules = ["android_library", "java_library"],
        ),
        # List of packages to document. If not specified then all packages
        # which have classes in "srcs" will be included.
        "packages": attr.string_list(),
        # Name of project for documentation federation.
        "federation_project": attr.string(
            default = "Android",
        ),
        # URL for federated documentation.
        "federation_url": attr.string(
            default = "https://developer.android.com",
        ),
        # TXT file containing definition of API we are federated with.
        "federation_api_txt": attr.label(
            default = Label("//third_party/android/sdk:api/24.txt"),
            allow_single_file = [".txt"],
        ),
        "devsite": attr.bool(
            default = False,
        ),
        "yamlV2": attr.bool(
            default = False,
        ),
        "_droiddoc": attr.label(
            default = Label("//third_party/android/androidx_test/build_extensions:droiddoc"),
            executable = True,
            allow_files = True,
            cfg = "host",
        ),
        "_android_jar": attr.label(
            default = Label("//third_party/java/android/android_sdk_linux:android"),
            allow_files = True,
        ),
    },
    outputs = {
        "docs": "%{name}.zip",
        "api": "%{name}_api.txt",
    },
)
