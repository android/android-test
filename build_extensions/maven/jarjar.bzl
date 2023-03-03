"""Runs jarjar over a jar file."""

def jarjar(ctx, rule, src, out):
    """Runs jarjar See https://github.com/pantsbuild/jarjar.

    Args:
      ctx: the context
      rule: a text file containing the list of jarjar transforms to apply
      src: The input jar
      out: The output jar.

    """
    args = ctx.actions.args()
    args.add("process")
    args.add(rule)
    args.add(src)
    args.add(out)

    ctx.actions.run(
        executable = ctx.executable._jarjar,
        inputs = [rule, src],
        outputs = [out],
        arguments = [args],
    )
