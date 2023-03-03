"""Update a zip file to update or add a particular file within."""

def add_or_update_file_in_zip(ctx, name, src, out, update_src, update_path):
    """Update a zip file to update or add a particular file within.

    """
    ctx.actions.run_shell(
        inputs = [src, update_src],
        outputs = [out],
        command = ";".join([
            "tmp={}_tmp".format(name),
            "rm -rf $$tmp",
            "mkdir -p $$tmp",
            "cp {update_src} $$tmp/{update_path}".format(
                update_src = update_src.path,
                update_path = update_path,
            ),
            "zip -j -X -q -l " +
            "{src} $$tmp/{update_path} -O {out}".format(
                src = src.path,
                update_path = update_path,
                out = out.path,
            ),
        ]),
    )
