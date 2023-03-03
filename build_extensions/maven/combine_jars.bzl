"""Combines multiple jars into one jar."""

def combine_jars(ctx, input_jars_deps, output):
    """Combine several jars into a single jar.

    Bazel wrapper for build_extensions/jar_combiner.

    Args:
      ctx: the rule context
      input_jars_deps: depset of input jars
      output: the output file path to use
    """
    if not input_jars_deps:
        fail("must provide at least one input_jar")
    if not output:
        fail("must provide output file")

    args = ctx.actions.args()
    args.add(output)
    args.add_all(input_jars_deps)

    ctx.actions.run(
        executable = ctx.executable._combine_jars_java,
        inputs = input_jars_deps,
        arguments = [args],
        outputs = [output],
    )
