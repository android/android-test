def copy_file(ctx, src, dest):
    if src.is_directory or dest.is_directory:
        fail("Cannot use copy_file with directories")
    ctx.actions.run_shell(
        command = "cp --reflink=auto $1 $2",
        arguments = [src.path, dest.path],
        inputs = [src],
        outputs = [dest],
        mnemonic = "CopyFile",
        progress_message = "Copy %s to %s" % (src.short_path, dest.short_path),
    )
