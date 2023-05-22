# Description:
#   Tests for espresso concurrent idling resrouces

load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

axt_android_local_test(
    name = "IdlingScheduledThreadPoolExecutorTest",
    size = "small",
    srcs = [
        "IdlingScheduledThreadPoolExecutorTest.java",
    ],
    deps = [
        "//espresso/idling_resource/concurrent/java/androidx/test/espresso/idling/concurrent:scheduled_thread_pool_idling_resource",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "//ext/junit",
        "@maven//:com_google_truth_truth",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_local_test(
    name = "IdlingThreadPoolExecutorTest",
    size = "small",
    srcs = [
        "IdlingThreadPoolExecutorTest.java",
    ],
    deps = [
        "//espresso/idling_resource/concurrent/java/androidx/test/espresso/idling/concurrent:thread_pool_idling_resource",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "//ext/junit",
        "@maven//:com_google_truth_truth",
        "@maven//:org_mockito_mockito_core",
    ],
)
