android_library(
    name = "reverser",
    testonly = True,
    srcs = ["Reverser.java"],
    tags = [
        # It is not yet clear which targets to exclude from coverage to suppress the
        # "ClassNotFoundException: org.jacoco.agent.rt.internal.Offline" error.
        "nozapfhahn",
    ],
    deps = [
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
        "//services/speakeasy/java/androidx/test/services/speakeasy/client",
        "//services/speakeasy/java/androidx/test/services/speakeasy/client:tool_connection",
        "//services/speakeasy/java/androidx/test/services/speakeasy/server:speak_easy_service",
    ],
)
