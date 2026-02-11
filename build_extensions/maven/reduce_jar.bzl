"""Remove entries from a jar that exist in another jar."""

def reduce_jar(ctx, input_jar, overlapping_jar, output_jar):
    """Remove entries from jar that exist in another jar.

    Bazel wrapper for build_extensions/jar_reducer.

    Args:
      ctx: the rule context
      input_jar: the input jar to reduce
      overlapping_jar: the baseline jar that contains entries to remove
      output_jar: the produced output
    """
    if not input_jar:
        fail("must provide input_jar")
    if not output_jar:
        fail("must provide output file")
    if not reduce_jar:
        fail("must provide reduce jar file")

    args = ctx.actions.args()
    args.add(input_jar)
    args.add(overlapping_jar)
    args.add(output_jar)

    ctx.actions.run(
        executable = ctx.executable._reduce_jar_java,
        inputs = [input_jar, overlapping_jar],
        arguments = [args],
        outputs = [output_jar],
        mnemonic = "ReduceJar",
    )
