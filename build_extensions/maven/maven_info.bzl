# Copyright 2023 The Android Open Source Project. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""Starlark rules to collect Maven artifacts information.
"""

load("//build_extensions/maven:kotlin_info.bzl", "is_kotlin")
load("//build_extensions:axt_versions.bzl", "KOTLIN_VERSION")
load("//build_extensions/maven:maven_registry.bzl", "get_artifact_from_label", "get_maven_apk_deps", "is_axt_label", "is_shaded_from_label")

# logic here largely inspired from https://github.com/google/dagger/blob/master/tools/maven_info.bzl

# provider for the built maven files to be published
MavenFilesInfo = provider(
    fields = {
        "runtime": """
        The runtime artifact. Either a .jar, .aar or .apk
        """,
        "src_jar": """
        The jar of source files
        """,
        "validation": """
        The validation output file
        """,
    },
)

# provider for info on building the maven artifacts and its related dependency info
MavenInfo = provider(
    fields = {
        "artifact": """
        The Maven coordinate for the artifact that is exported by this target. Can only be null if is_compileonly or is_shaded are True.
        """,
        "is_compileOnly": """
        True if this target is a compile only dependency, and should not be included in final combined artifact.
        """,
        "is_shaded": """
        True if this is a shaded dependency and it should be directly bundled inside the final artifact.
        """,
        "transitive_included_runtime_jars": """
        depset of jars to bundle into same artifact.
        """,
        "transitive_included_src_jars": """
        depset of src jars to bundle into same artifact.
        """,
        "transitive_maven_direct_deps": """
        depset of external maven artifacts that are a direct dependency of this artifact.
        """,
    },
)

_MAVEN_COORDINATES_PREFIX = "maven_coordinates="

def _collect_maven_info_impl(target, ctx):
    tags = getattr(ctx.rule.attr, "tags", [])
    neverlink = getattr(ctx.rule.attr, "neverlink", False)
    deps = getattr(ctx.rule.attr, "deps", [])
    exports = getattr(ctx.rule.attr, "exports", [])

    # ignore non-runtime or non-java dependencies eg proto_library
    if neverlink or JavaInfo not in target:
        return MavenInfo(
            artifact = None,
            is_compileOnly = True,
            is_shaded = False,
            transitive_included_runtime_jars = depset(),
            transitive_included_src_jars = depset(),
            transitive_maven_direct_deps = depset(),
        )

    artifact = get_artifact_from_label(target.label)
    is_shaded = is_shaded_from_label(target.label)

    if not artifact and not is_shaded:
        for tag in tags:
            if tag.startswith(_MAVEN_COORDINATES_PREFIX):
                if artifact:
                    fail("%s: Each target must belong to one and only one maven artifact: Does target have multiple '%s' tags?" % (target.label, _MAVEN_COORDINATES_PREFIX))
                artifact = tag[len(_MAVEN_COORDINATES_PREFIX):]

    included_runtime_jars = []
    included_runtime_jars += target[JavaInfo].runtime_output_jars

    # shaded source won't be correct, so just exclude it
    included_src_jars = [] if is_shaded else target[JavaInfo].source_jars
    transitive_included_runtime_jars = []
    transitive_included_src_jars = []
    maven_direct_deps = []
    transitive_maven_direct_deps = []

    # add implicit runtime dependencies needed by certain rules
    if is_kotlin(target):
        maven_direct_deps.append("org.jetbrains.kotlin:kotlin-stdlib:%s" % KOTLIN_VERSION)

    grpc_protobuf_jar = _findGrpcJavaProtoJar(target)
    if grpc_protobuf_jar:
        # java_grpc_library rule's generated code implicitly depends on this artifact
        # which needs to be shaded
        included_runtime_jars.append(grpc_protobuf_jar)

    # this is a bit of a hack, but java_lite_proto_library have empty runtime_output_jars!
    # So add in all transitive_runtime_jars instead, because we know for protos all dependencies must
    # be embedded in resulting aar
    if _isJavaProtoTarget((deps + exports)):
        included_runtime_jars = target[JavaInfo].transitive_runtime_jars.to_list()

    for dep in (deps + exports):
        if is_shaded:
            transitive_included_runtime_jars.append(dep[MavenInfo].transitive_included_runtime_jars)
        if dep[MavenInfo].artifact == artifact:
            transitive_included_runtime_jars.append(dep[MavenInfo].transitive_included_runtime_jars)
            transitive_included_src_jars.append(dep[MavenInfo].transitive_included_src_jars)

            # also need to pick up any external maven deps from this included dep
            transitive_maven_direct_deps.append(dep[MavenInfo].transitive_maven_direct_deps)
        elif dep[MavenInfo].is_shaded:
            transitive_included_runtime_jars.append(dep[MavenInfo].transitive_included_runtime_jars)
        elif dep[MavenInfo].artifact:
            # this dependency is an external maven artifact, just need to add it to direct deps
            maven_direct_deps.append(dep[MavenInfo].artifact)
        elif not dep[MavenInfo].is_compileOnly and not dep[MavenInfo].is_shaded:
            fail("%s: Each target must belong to one and only one maven artifact: Did not find maven info for dep %s" % (target.label, dep.label))

    return [MavenInfo(
        artifact = artifact,
        is_compileOnly = False,
        is_shaded = is_shaded,
        transitive_included_runtime_jars = depset(included_runtime_jars, transitive = transitive_included_runtime_jars),
        transitive_included_src_jars = depset(included_src_jars, transitive = transitive_included_src_jars),
        transitive_maven_direct_deps = depset(maven_direct_deps, transitive = transitive_maven_direct_deps),
    )]

def _isJavaProtoTarget(all_deps):
    for dep in all_deps:
        if ProtoInfo in dep:
            return True
    return False

def _findGrpcJavaProtoJar(target):
    for jar in target[JavaInfo].transitive_runtime_jars.to_list():
        if "io_grpc_grpc_java/protobuf-lite" in jar.path:
            return jar
    return None

collect_maven_info = aspect(
    attr_aspects = [
        "deps",
        "exports",
    ],
    doc = """
    Collects the Maven information for targets, their dependencies, and their transitive exports.
    """,
    implementation = _collect_maven_info_impl,
)

def _collect_maven_apk_info_impl(target, ctx):
    neverlink = getattr(ctx.rule.attr, "neverlink", False)
    deps = getattr(ctx.rule.attr, "deps", [])
    exports = getattr(ctx.rule.attr, "exports", [])

    # ignore non-runtime or non-java dependencies eg proto_library or non-axt-labels
    if neverlink or JavaInfo not in target or not is_axt_label(target.label):
        return MavenInfo(
            artifact = None,
            is_compileOnly = True,
            is_shaded = False,
            transitive_included_runtime_jars = depset(),
            transitive_included_src_jars = depset(),
            transitive_maven_direct_deps = depset(),
        )

    artifact = get_artifact_from_label(target.label)
    included_src_jars = target[JavaInfo].source_jars
    transitive_included_src_jars = []

    for dep in (deps + exports):
        transitive_included_src_jars.append(dep[MavenInfo].transitive_included_src_jars)

    maven_deps = get_maven_apk_deps(artifact)
    return [MavenInfo(
        artifact = artifact,
        is_compileOnly = False,
        is_shaded = False,
        transitive_included_runtime_jars = depset(),
        transitive_included_src_jars = depset(included_src_jars, transitive = transitive_included_src_jars),
        transitive_maven_direct_deps = depset(maven_deps),
    )]

collect_maven_apk_info = aspect(
    attr_aspects = [
        "deps",
        "exports",
    ],
    doc = """
    Collects the Maven apk information for targets, their dependencies, and their transitive exports.
    """,
    implementation = _collect_maven_apk_info_impl,
)
