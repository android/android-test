"""Emulator object exposes data about a particular emulator."""

load("//tools/android/emulated_devices:macro/props.bzl", "new_props")
load("//tools/android/emulated_devices:macro/image.bzl", "image_arch")

def new_emulator(
        emu_type,
        extra_files = None,
        supports = None,
        uses_kvm = True,
        props = None,
        qemu2 = True,
        default_visibility = ["//visibility:public"]):
    """Creates a new emulator object.

    Args:
      emu_type: a string type identifying the emulator binary
      extra_files: a list of file groups that must be present in runfiles.
      supports: a dictionary of arch: [api_levels] that are supported.
      uses_kvm: emulator can use kvm for x86 images.
      props: a props object to add to all images started by this emulator.
      qemu2: indicates if this emulator is qemu2-based (the default).
          Qemu1 emulators are deprecated.
      default_visibility: The emulator's visibility, if not overridden by an
          explicit visibility argument to new_devices. Only applies to targets
          with an explciit emulator suffix. Defaults to public.

    Returns:
      An emulator object.
    """
    return {
        "emulator_type": emu_type,
        "supports": supports or {},
        "uses_kvm": uses_kvm,
        "extra_files": extra_files or [],
        "props": props or new_props(),
        "qemu2": qemu2,
        "default_visibility": default_visibility,
    }

def supported_arches_for_api(emulator, api):
    """Returns a list of supported archs for this emulator and api."""
    arches = []
    for arch, apis in emulator["supports"].items():
        if api in apis:
            arches.append(arch)
    return arches

def emulator_tags(emulator, image):
    """Adds required tags for an emulator to run this particular image."""
    if emulator_uses_kvm(emulator) and image_arch(image) == "x86":
        return ["requires-kvm"]
    return []

def emulator_uses_kvm(emulator):
    """Determines if the emulator can use kvm."""
    return emulator["uses_kvm"]

def emulator_type(emulator):
    """Identifies the type of emulator."""
    return emulator["emulator_type"]

def emulator_files(emulator):
    """Files that are runtime dependencies of this emulator."""
    return emulator["extra_files"]

def emulator_props(emulator):
    """Properties that should be setup on any image this emulator starts."""
    return emulator["props"]

def emulator_suffix(emulator):
    """A suffix for this emulator."""
    return "_%s" % emulator_type(emulator)

def emulator_is_qemu2(emulator):
    """Identifies if the emulator is qemu2-based."""
    return emulator["qemu2"]

def emulator_default_visibility(emulator):
    """The default visibility for this emulator."""
    return emulator["default_visibility"]
