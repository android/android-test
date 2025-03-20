"""Skylark rule to create a maven repository from a single artifact."""

load("//build_extensions/maven:maven_info.bzl", "MavenFilesInfo", "MavenInfo")

_pom_tmpl = "\n".join([
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"',
    '    xmlns="http://maven.apache.org/POM/4.0.0"',
    '    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">',
    "  <modelVersion>4.0.0</modelVersion>",
    "  <groupId>{group_id}</groupId>",
    "  <artifactId>{artifact_id}</artifactId>",
    "  <version>{version}</version>",
    "  <packaging>{packaging}</packaging>",
    "  <name>AndroidX Test Library</name>",
    "  <description>The AndroidX Test Library provides an extensive framework for testing Android apps</description>",
    "  <url>https://developer.android.com/testing</url>",
    "  <inceptionYear>2015</inceptionYear>",
    "  <licenses>",
    "{licenses}",
    "  </licenses>",
    "  <developers>",
    "    <developer>",
    "      <name>The Android Open Source Project</name>",
    "    </developer>",
    "  </developers>",
    "  <dependencies>",
    "{dependencies}",
    "  </dependencies>",
    "</project>",
    "",
])

_dependency_tmpl = "\n".join([
    "    <dependency>",
    "      <groupId>{group_id}</groupId>",
    "      <artifactId>{artifact_id}</artifactId>",
    "      <version>{version}</version>",
    "      <scope>compile</scope>",
    "{exclusions}",
    "    </dependency>",
])

_exclusions_tmpl = "\n".join([
    "      <exclusions>",
    "{exclusion}",
    "      </exclusions>",
])

_exclusion_tmpl = "\n".join([
    "          <exclusion>",
    "              <groupId>{groupId}</groupId>",
    "              <artifactId>{artifactId}</artifactId>",
    "          </exclusion>",
    "",
])

_metadata_tmpl = "\n".join([
    '<?xml version="1.0" encoding="UTF-8"?>',
    "<metadata>",
    "  <groupId>{group_id}</groupId>",
    "  <artifactId>{artifact_id}</artifactId>",
    "  <version>{version}</version>",
    "  <versioning>",
    "    <release>{version}</release>",
    "    <versions>",
    "      <version>{version}</version>",
    "    </versions>",
    "  <lastUpdated>{last_updated}</lastUpdated>",
    "  </versioning>",
    "</metadata>",
    "",
])

_license_impl = "\n".join([
    "    <license>",
    "      <name>{name}</name>",
    "      <url>{url}</url>",
    "      <distribution>repo</distribution>",
    "    </license>",
])

def _packaging_type(f):
    """Returns the packaging type used by the file f."""
    if f.basename.endswith(".aar"):
        return "aar"
    elif f.basename.endswith(".apk"):
        return "apk"
    elif f.basename.endswith(".jar"):
        return "jar"
    fail("Artifact has unknown packaging type: %s" % f.short_path)

def _create_pom_string(
        ctx,
        group_id,
        artifact_id,
        version,
        packaging_type,
        maven_dependencies,
        maven_dependencies_exclusions = {}):
    """Returns the contents of the pom file as a string."""
    dependencies = []
    for dep in maven_dependencies:
        dep_group_id, dep_artifact_id, dep_version = _parse_artifact_versioning(dep)
        exclusions_string = _create_exclusions_string(dep_group_id, dep_artifact_id, maven_dependencies_exclusions)
        dependencies.append(_dependency_tmpl.format(
            group_id = dep_group_id,
            artifact_id = dep_artifact_id,
            version = dep_version,
            exclusions = exclusions_string,
        ))

    licenses = []
    if ctx.attr.license_name and ctx.attr.license_url:
        licenses.append(_license_impl.format(
            name = ctx.attr.license_name,
            url = ctx.attr.license_url,
        ))
    else:
        licenses.append(_license_impl.format(
            name = "The Apache Software License, Version 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt",
        ))

    return _pom_tmpl.format(
        group_id = group_id,
        artifact_id = artifact_id,
        version = version,
        packaging = packaging_type,
        dependencies = "\n".join(dependencies),
        licenses = "\n".join(licenses),
    )

def _create_metadata_string(ctx, group_id, artifact_id, version):
    """Returns the string contents of maven-metadata.xml for the group."""
    return _metadata_tmpl.format(
        group_id = group_id,
        artifact_id = artifact_id,
        version = version,
        last_updated = ctx.attr.last_updated,
    )

def _create_exclusions_string(group_id, artifact_id, excluded_dependencies_map):
    """Returns the string contents of excluded dependencies for the dependency."""
    excluded_dependencies_csv = excluded_dependencies_map.get("%s:%s" % (group_id, artifact_id))
    if not excluded_dependencies_csv:
        return ""
    excluded_dependencies = excluded_dependencies_csv.split(",")
    excluded_dependencies_string = ""
    for excluded_dependency in excluded_dependencies:
        excluded_group, excluded_artifact = _parse_group_artifact(excluded_dependency)
        excluded_dependencies_string += _exclusion_tmpl.format(
            groupId = excluded_group,
            artifactId = excluded_artifact,
        )
    return _exclusions_tmpl.format(exclusion = excluded_dependencies_string)

def _parse_artifact_versioning(artifact_coordinates):
    """Parse out artifact_id, version and group info from a full coordinate strings.

    Expected format groupId:artifactId:[type:]version 
    """
    segments = artifact_coordinates.split(":")
    if len(segments) == 3:
      return segments
    elif len(segments) == 4: 
      return segments[0],segments[1],segments[3]

    fail("artifact_deps values must be of form: groupId:artifactId:[type:]version. Found %s" % artifact_coordinates)
    return segments

def _parse_group_artifact(artifact_coordinates):
    """Parse out artifact_id, and group info from a  coordinate string"""
    if artifact_coordinates.count(":") != 1:
        fail("artifact_deps values must be of form: groupId:artifactId. Found %s" % artifact_coordinates)

    return artifact_coordinates.split(":")

def _rename_artifact(ctx, tpl_string, src_file, packaging_type, artifact_id, version):
    """Rename the artifact to match maven naming conventions."""
    artifact = ctx.actions.declare_file(tpl_string % (artifact_id, version, packaging_type))
    ctx.actions.run_shell(
        inputs = [src_file],
        outputs = [artifact],
        command = "cp %s %s" % (src_file.path, artifact.path),
    )
    return artifact

def _maven_artifact_impl(ctx):
    """Generates maven repository for a single artifact."""

    group_id, artifact_id, version = _parse_artifact_versioning(ctx.attr.target[MavenInfo].artifact)
    pom = ctx.actions.declare_file(
        "%s-%s.pom" % (artifact_id, version),
    )

    maven_deps = sorted(ctx.attr.target[MavenInfo].transitive_maven_direct_deps.to_list())

    packaging_type = _packaging_type(ctx.attr.target[MavenFilesInfo].runtime)
    pom_content = _create_pom_string(ctx, group_id, artifact_id, version, packaging_type, maven_deps, ctx.attr.excluded_dependencies)
    ctx.actions.write(output = pom, content = pom_content)

    metadata = ctx.actions.declare_file("%s_maven-metadata.xml" % (ctx.label.name))
    ctx.actions.write(output = metadata, content = _create_metadata_string(ctx, group_id, artifact_id, version))

    # Rename binary artifact to artifact_id-version.packaging_type
    artifact = _rename_artifact(ctx, "%s-%s.%s", ctx.attr.target[MavenFilesInfo].runtime, packaging_type, artifact_id, version)

    arguments = [
        "--group_path=%s" % group_id.replace(".", "/"),
        "--artifact_id=%s" % artifact_id,
        "--version=%s" % version,
        "--artifact=%s" % artifact.path,
        "--pom=%s" % pom.path,
        "--metadata=%s" % metadata.path,
        "--output=%s" % ctx.outputs.m2repository.path,
    ]
    inputs = [pom, metadata, artifact]

    if ctx.attr.target[MavenFilesInfo].src_jar:
        # Rename sources jar artifact to artifact_id-version-sources.jar
        source = _rename_artifact(ctx, "%s-%s-sources.%s", ctx.attr.target[MavenFilesInfo].src_jar, "jar", artifact_id, version)
        arguments.append("--source=%s" % source.path)
        inputs.append(source)

    # TODO: remove, shouldn't be necessary
    # add validation to inputs so it runs on rebuild
    if ctx.attr.target[MavenFilesInfo].validation:
        inputs.append(ctx.attr.target[MavenFilesInfo].validation)

    ctx.actions.run(
        inputs = inputs,
        outputs = [ctx.outputs.m2repository],
        arguments = arguments,
        executable = ctx.executable._maven_artifact_sh,
        progress_message = (
            "Packaging repository: %s" % ctx.outputs.m2repository.short_path
        ),
    )

maven_artifact = rule(
    implementation = _maven_artifact_impl,
    attrs = {
        "target": attr.label(
            doc = "The target to be published to maven. Must be a axt_android_aar or axt_android_apk",
            mandatory = True,
            providers = [MavenInfo, MavenFilesInfo],
        ),
        # TODO: derive this?
        "last_updated": attr.string(mandatory = True),
        "license_file": attr.label(
            mandatory = False,
            allow_single_file = ["LICENSE"],
        ),
        "license_name": attr.string(mandatory = False),
        "license_url": attr.string(mandatory = False),
        "excluded_dependencies": attr.string_dict(
            mandatory = False,
            doc = "Map of maven dependency to a csv list of excluded dependencies. eg {\"com.google.foo:foo\":\"com.google.bar:bar,com.google.bar:bar-none\"}",
        ),
        "_maven_artifact_sh": attr.label(
            default = Label("//build_extensions/maven:maven_artifact_sh"),
            executable = True,
            allow_files = True,
            cfg = "exec",
        ),
    },
    outputs = {
        "m2repository": "%{name}.zip",
    },
)
