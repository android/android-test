"""Generate AXT android archive (aar)."""

load("//build_extensions/maven:add_or_update_file_in_zip.bzl", "add_or_update_file_in_zip")
load("//build_extensions/maven:combine_jars.bzl", "combine_jars")
load("//build_extensions/maven:maven_info.bzl", "MavenFilesInfo", "MavenInfo", "collect_maven_info")
load("//build_extensions/maven:jarjar.bzl", "jarjar")

def _android_aar_impl(ctx):
    # current_aar will include almost everything needed: an AndroidManifest.xml, compiled resources,
    # and a proguard.txt. However, its classes.jar will only include direct compiled srcs. Missing
    # will be any sources from transitive dependencies that also need to be bundled in the aar
    current_aar = ctx.attr.included_dep[AndroidLibraryAarInfo].aar
    if not current_aar:
        fail("included_dep %s does not produce an aar. Is it an android_library rule with an AndroidManifest.xml?" % ctx.attr.included_dep.label)

    if not ctx.attr.included_dep[MavenInfo].artifact:
        fail("Could not find maven artifact for included_dep %s. Is it listed in maven_registry?" % ctx.attr.included_dep.label)

    # build a combined classes jar from all dependencies that are part of this maven artifact
    classes_jar = ctx.actions.declare_file(ctx.attr.name + "_combined_classes.jar")
    combine_jars(
        ctx = ctx,
        input_jars_deps = ctx.attr.included_dep[MavenInfo].transitive_included_runtime_jars,
        output = classes_jar,
    )

    # optionally use jarjar to rename shaded dependencies
    if (ctx.attr.jarjar_rule):
        jarjar_classes_jar = ctx.actions.declare_file(ctx.attr.name + "_jarjar_classes.jar")
        jarjar(ctx, rule = ctx.file.jarjar_rule, src = classes_jar, out = jarjar_classes_jar)
        classes_jar = jarjar_classes_jar

    # TODO: tying validation to an output was the only way to get this to run, but
    # according to docs it shouldn't be necessary
    #validation_output = ctx.actions.declare_file(ctx.attr.name + ".validation")
    validation_output = ctx.outputs.validation
    ctx.actions.run(
        inputs = [classes_jar],
        outputs = [validation_output],
        executable = ctx.executable._validate_jar_java,
        arguments = [validation_output.path, classes_jar.path] + ctx.attr.expected_class_prefixes,
    )

    # update the aar with the new classes.jar
    add_or_update_file_in_zip(
        ctx,
        ctx.attr.name + "_classes",
        src = current_aar,
        out = ctx.outputs.aar,
        update_src = classes_jar,
        update_path = "classes.jar",
    )

    # produce src jar
    combine_jars(
        ctx = ctx,
        input_jars_deps = ctx.attr.included_dep[MavenInfo].transitive_included_src_jars,
        output = ctx.outputs.src_jar,
    )

    return [
        ctx.attr.included_dep[MavenInfo],
        MavenFilesInfo(runtime = ctx.outputs.aar, src_jar = ctx.outputs.src_jar, validation = validation_output),
        OutputGroupInfo(_validation = depset([validation_output])),
    ]

axt_android_aar = rule(
    implementation = _android_aar_impl,
    attrs = {
        "included_dep": attr.label(
            doc = "The android_library target to use as a basis for the android archive. " +
                  "This must include an AndroidManifest.xml, and optionally resources, proguard_specs",
            mandatory = True,
            providers = [JavaInfo, AndroidLibraryAarInfo],
            aspects = [collect_maven_info],
        ),
        "expected_class_prefixes": attr.string_list(
            doc = "The list of class prefixes expected to be containing in resulting .aar. All classes in aar must match at least one of the given prefixes.",
            mandatory = True,
        ),
        "jarjar_rule": attr.label(
            doc = "Optional file containing jarjar rules to be applied to the classes.",
            mandatory = False,
            allow_single_file = [".txt"],
        ),
        "_jdk": attr.label(
            default = Label("@bazel_tools//tools/jdk"),
            providers = [java_common.JavaRuntimeInfo],
        ),
        "_combine_jars_java": attr.label(
            executable = True,
            cfg = "exec",
            allow_files = True,
            default = Label("//build_extensions/jar_combiner/java/androidx/test/tools/jarcombiner"),
        ),
        "_validate_jar_java": attr.label(
            executable = True,
            cfg = "exec",
            allow_files = True,
            default = Label("//build_extensions/jar_validator/java/androidx/test/tools/jarvalidator"),
        ),
        "_jarjar": attr.label(
            default = Label("//build_extensions/maven:jarjar_bin"),
            executable = True,
            cfg = "exec",
        ),
    },
    outputs = {
        "aar": "%{name}.aar",
        "src_jar": "%{name}-src.jar",
        # TODO: remove, this shouldn't be necessary
        "validation": "%{name}.validation",
    },
)
