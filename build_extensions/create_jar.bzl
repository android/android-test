"""Build rule to create a single jar from given files of any type."""

def _create_jar_impl(ctx):
    """
    Construct a single jar from given files of any type.
    """

    input_paths = []
    for target in ctx.attr.srcs:
        input_paths.extend(target.files.to_list())

    args = ctx.actions.args()
    args.add(ctx.outputs.output)
    args.add_all(input_paths)

    ctx.actions.run(
        inputs = input_paths,
        outputs = [ctx.outputs.output],
        executable = ctx.executable._create_jar_java,
        arguments = [args],
        mnemonic = "CreateJAR",
    )

create_jar = rule(
    attrs = {
        "srcs": attr.label_list(allow_files = True),
        "_create_jar_java": attr.label(
            executable = True,
            cfg = "exec",
            allow_files = True,
            default = Label("//build_extensions/jar_creator/java/androidx/test/tools/jarcreator:jarcreator"),
        ),
    },
    outputs = {
        "output": "%{name}.jar",
    },
    implementation = _create_jar_impl,
)
