"""Runs jarjar over a jar file."""

load("@rules_java//java:defs.bzl", "java_common")

def jarjar_rule(ctx, rule, src, out):
    """API to run jarjar from a rule.

    See https://github.com/pantsbuild/jarjar.

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

def _jarjar_impl(ctx):
    jarjar_rule(ctx, ctx.file.rule, ctx.file.src, ctx.outputs.jar)

jarjar = rule(
    implementation = _jarjar_impl,
    attrs = {
        "src": attr.label(
            doc = "Jar file to transform",
            allow_single_file = [".jar"],
        ),
        "rule": attr.label(
            doc = "File containing jarjar rules to be applied to the classes.",
            allow_single_file = [".txt"],
        ),
        "_jdk": attr.label(
            default = Label("@local_jdk//:bin/java"),
            providers = [java_common.JavaRuntimeInfo],
        ),
        "_jarjar": attr.label(
            default = Label("//build_extensions/maven:jarjar_bin"),
            executable = True,
            cfg = "exec",
        ),
    },
    outputs = {
        "jar": "%{name}.jar",
    },
)
