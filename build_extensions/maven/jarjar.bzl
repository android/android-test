def jarjar(ctx, rule, src, out):
    """Runs jarjar See https://github.com/pantsbuild/jarjar.

    Args:
      ctx: the context
      rule: a text file containing the list of jarjar transforms to apply
      src: The input jar
      out: The output jar.

    """
    command = "{jarjar_bin} process {rule} {input} {output}".format(
        jarjar_bin = ctx.executable._jarjar.path,
        rule = rule.path,
        input = src.path,
        output = out.path,
    )

    ctx.actions.run_shell(
        command = command,
        inputs = [rule, src],
        outputs = [out],
        tools = [ctx.executable._jarjar],
    )
