"""Starlark rule to create a single maven repository zip from a set of zips of each artifact."""

def _maven_repository_impl(ctx):
    """Merges several maven artifacts into combined zip."""

    source_files = []
    for src in ctx.attr.srcs:
        source_files.extend(src.files.to_list())

    args = ctx.actions.args()
    args.add(ctx.outputs.m2repository.path)
    args.add_all([f.path for f in source_files])

    ctx.actions.run(
        inputs = source_files,
        outputs = [ctx.outputs.m2repository],
        arguments = [args],
        executable = ctx.executable._zip_combiner,
        progress_message = (
            "Packaging repository: %s" % ctx.outputs.m2repository.short_path
        ),
    )

maven_repository = rule(
    implementation = _maven_repository_impl,
    attrs = {
        "srcs": attr.label_list(allow_rules = ["maven_artifact"]),
        "_zip_combiner": attr.label(
            default = Label("//build_extensions/zip_combiner/java/androidx/test/tools/zipcombiner"),
            executable = True,
            allow_files = True,
            cfg = "exec",
        ),
    },
    outputs = {
        "m2repository": "%{name}.zip",
    },
)
