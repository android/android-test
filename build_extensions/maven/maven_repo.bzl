"""Starlark rule to create a single maven repository zip from a set of zips of each artifact."""

def _maven_repository_impl(ctx):
    """Generates maven repository for multiple artifacts."""
    source_files = []
    for src in ctx.attr.srcs:
        source_files.extend(src.files.to_list())
    ctx.actions.run(
        inputs = source_files,
        outputs = [ctx.outputs.m2repository],
        arguments = [
            "--sources=%s" % ",".join([f.path for f in source_files]),
            "--output=%s" % ctx.outputs.m2repository.path,
        ],
        executable = ctx.executable._maven_repository,
        progress_message = (
            "Packaging repository: %s" % ctx.outputs.m2repository.short_path
        ),
    )

maven_repository = rule(
    implementation = _maven_repository_impl,
    attrs = {
        "srcs": attr.label_list(allow_rules = ["maven_artifact"]),
        "_maven_repository": attr.label(
            default = Label("//build_extensions/maven:maven_repository"),
            executable = True,
            allow_files = True,
            cfg = "exec",
        ),
    },
    outputs = {
        "m2repository": "%{name}.zip",
    },
)
