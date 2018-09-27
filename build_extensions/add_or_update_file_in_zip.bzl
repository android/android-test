"""Update a zip file to update or add a particular file within."""

def add_or_update_file_in_zip(
    name,
    src,
    out,
    update_src,
    update_path,
    **kwargs):
  """Update a zip file to update or add a particular file within.

  Args:
    name: Rule name
    src: The zip file to update
    out: Name of the output zip file
    update_src: The source for the file to update in the zip
    update_path: Path for the file to update within the zip.
    **kwargs: Extra arguments that will be passed to the underlying
      genrule rule.
  """

  native.genrule(
      name = name,
      srcs = [
          update_src,
          src,
      ],
      outs = [
          out,
      ],
      cmd = ";".join([
          "tmp={}_tmp".format(name),
          "rm -rf $$tmp",
          "mkdir -p $$tmp",
          "cp $(location {update_src}) $$tmp/{update_path}".format(
              update_src = update_src,
              update_path = update_path),
          "zip -j -X -q -l "
          + "$(location {src}) $$tmp/{update_path} -O $@".format(
              src = src,
              update_path = update_path)
      ]),
      **kwargs)