py_library(
    name = "gflags",
    srcs = glob(["gflags/*.py"]),
    srcs_version = "PY2AND3",
    deps = [
        ":pep257",
        "@six_archive//:six",
    ],
    visibility = ["//third_party/android/androidx_test:future_public_visibility"],
)

py_library(
    name = "pep257",
    srcs = glob(["gflags/third_party/pep257/*.py"]),
    srcs_version = "PY2AND3",
    visibility = ["//third_party/android/androidx_test:future_public_visibility"],
)

