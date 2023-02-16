"""A fast, experimental wrapper around axt_android_library_test."""

load("//build_extensions:register_extension_info.bzl", "register_extension_info")
load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

def android_library_slim_test(
        name,
        nocompress_extensions = ["dex"],
        **kwargs):
    """An experimental wrapper around axt_android_library_test built for performance and additional features.

    The purpose of this API is to benchmark and measure the benefits of
    new features, and eventually upstream them to axt_android_library_test/android_library_test.


    Current features/caveats:
      - Don't compress dex files by default to save cost of extracting them at runtime.

    Args:
        name: the rule name
        nocompress_extensions: default 'dex'
        **kwargs: args to pass to axt_android_library_test
    """

    axt_android_library_test(
        name = name,
        nocompress_extensions = nocompress_extensions,
        **kwargs
    )

register_extension_info(
    extension = android_library_slim_test,
    label_regex_for_dep = "{extension_name}",
)
