"""Emulator object exposes data about a particular emulator."""
load('//tools/android/emulated_devices:macro/props.bzl', 'new_props')
load('//tools/android/emulated_devices:macro/image.bzl', 'image_arch')


def new_emulator(emu_type,
                 extra_files=None,
                 supports=None,
                 uses_kvm=True,
                 suffix=True,
                 props=None):
  """Creates a new emulator object.

  Args:
    emu_type: a string type identifying the emulator binary
    extra_files: a list of file groups that must be present in runfiles.
    supports: a dictionary of arch: [api_levels] that are supported.
    uses_kvm: emulator can use kvm for x86 images.
    suffix: a suffix to add to any target that is dependent on this emulator.
    props: a props object to add to all images started by this emulator.

  Returns:
    An emulator object.
  """
  return {
      'emulator_type': emu_type,
      'supports': supports or {},
      'uses_kvm': uses_kvm,
      'extra_files': extra_files or [],
      'suffix': suffix,
      'props': props or new_props()
  }


def supported_arches_for_api(emulator, api):
  """Returns a list of supported archs for this emulator and api."""
  arches = []
  for arch, apis in emulator['supports'].items():
    if api in apis:
      arches.append(arch)
  return arches


def emulator_tags(emulator, image):
  """Adds required tags for an emulator to run this particular image."""
  if emulator_uses_kvm(emulator) and image_arch(image) == 'x86':
    return ['requires-kvm']
  return []


def emulator_uses_kvm(emulator):
  """Determines if the emulator can use kvm."""
  return emulator['uses_kvm']


def emulator_type(emulator):
  """Identifies the type of emulator."""
  return emulator['emulator_type']


def emulator_files(emulator):
  """Files that are runtime dependencies of this emulator."""
  return emulator['extra_files']


def emulator_props(emulator):
  """Properties that should be setup on any image this emulator starts."""
  return emulator['props']


def emulator_suffix(emulator):
  """A suffix for this emulator."""
  if emulator['suffix']:
    return '_%s' % emulator_type(emulator)
  return ''
