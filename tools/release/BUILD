load("@bazel_skylib//rules:expand_template.bzl", "expand_template")
load(
    "//build_extensions:axt_versions.bzl",
    "ANDROIDX_JUNIT_VERSION",
    "ANDROIDX_TRUTH_VERSION",
    "ANNOTATION_VERSION",
    "CORE_VERSION",
    "ESPRESSO_VERSION",
    "MONITOR_VERSION",
    "ORCHESTRATOR_VERSION",
    "RULES_VERSION",
    "RUNNER_VERSION",
    "SERVICES_VERSION",
)

package(
    default_visibility = [
        "//:__subpackages__",
    ],
)

expand_template(
    name = "update_settings_gradle",
    out = "settings.gradle.out",
    substitutions = {
        "{ANDROIDX_JUNIT_VERSION}": ANDROIDX_JUNIT_VERSION,
        "{ANDROIDX_TRUTH_VERSION}": ANDROIDX_TRUTH_VERSION,
        "{ANNOTATION_VERSION}": ANNOTATION_VERSION,
        "{CORE_VERSION}": CORE_VERSION,
        "{ESPRESSO_VERSION}": ESPRESSO_VERSION,
        "{MONITOR_VERSION}": MONITOR_VERSION,
        "{ORCHESTRATOR_VERSION}": ORCHESTRATOR_VERSION,
        "{RULES_VERSION}": RULES_VERSION,
        "{RUNNER_VERSION}": RUNNER_VERSION,
        "{SERVICES_VERSION}": SERVICES_VERSION,
    },
    template = "settings.gradle.template",
)

genrule(
    name = "update_settings_gradle_rule",
    srcs = [":update_settings_gradle"],
    outs = ["settings.gradle"],
    cmd = "cp $(location :update_settings_gradle) \"$@\"",
)
