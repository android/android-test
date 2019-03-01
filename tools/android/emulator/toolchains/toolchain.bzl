"""Defines the emulator_toolchain rule to allow configuring emulator binaries to use."""

EmulatorInfo = provider(
    doc = "Information used to launch a specific version of the emulator.",
    fields = {
        "emulator": "A label for the emulator launcher executable.",
        "emulator_dir": "Path to the emulator directory containing the launcher, for when there is no direct label to the launcher",
        "emulator_deps": "Additional files required to launch the emulator.",
    },
)

def _emulator_toolchain_impl(ctx):
    toolchain_info = platform_common.ToolchainInfo(
        info = EmulatorInfo(
            emulator = ctx.attr.emulator,
            emulator_dir = ctx.attr.emulator_dir,
            emulator_deps = ctx.attr.emulator_deps,
        ),
    )
    return [toolchain_info]

emulator_toolchain = rule(
    implementation = _emulator_toolchain_impl,
    attrs = {
        "emulator": attr.label(
            executable = True,
            allow_files = True,
            cfg = "host",
        ),
        "emulator_dir": attr.label(
            allow_files = True,
        ),
        "emulator_deps": attr.label_list(
            allow_files = True,
        ),
    },
)
