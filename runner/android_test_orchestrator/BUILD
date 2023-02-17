# Description:
#   On Device Orchestrator, to run instrumentation tests sequentially from a single adb command.

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

alias(
    name = "android_test_orchestrator",
    actual = "//runner/android_test_orchestrator/java/androidx/test/orchestrator",
    visibility = [":odo"],
)

package_group(
    name = "odo",
    packages = [
        "//runner/android_test_orchestrator/java/androidx/test/orchestrator",
        "//runner/android_test_orchestrator/java/androidx/test/orchestrator/instrumentationlistener",
        "//runner/android_test_orchestrator/java/androidx/test/orchestrator/junit",
        "//runner/android_test_orchestrator/java/androidx/test/orchestrator/listeners",
        "//runner/android_test_orchestrator/javatests/androidx/test/orchestrator",
        "//runner/android_test_orchestrator/javatests/androidx/test/orchestrator/instrumentationlistener",
        "//runner/android_test_orchestrator/javatests/androidx/test/orchestrator/junit",
        "//runner/android_test_orchestrator/javatests/androidx/test/orchestrator/listeners",
        "//runner/android_test_orchestrator/stubapp",
    ],
)
