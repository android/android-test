load(
    '//tools/android/emulated_devices:macro/device_config_info.bzl',
    'compatible_cheeses',)

load('//tools/android/emulated_devices:macro/image.bzl', 'image_flavor', 'image_api',
     'image_arch', 'image_supports_gms_channels', 'image_version_string',
     'image_device_visibility')

load('//tools/android/emulated_devices:macro/emulator.bzl',
     'supported_arches_for_api', 'emulator_suffix', 'emulator_tags')

load('//tools/android/emulated_devices:macro/hardware.bzl', 'new_hardware',
     'hardware_device_attributes')

load('//tools/android/emulated_devices:macro/props.bzl', 'new_props')
load('//tools/android/emulated_devices:macro/image_info.bzl', 'API_TO_IMAGES')
load('//tools/android/emulated_devices:macro/emulator_info.bzl', 'TYPE_TO_EMULATOR',
     'UMA', 'QEMU', 'QEMU2', 'extra_system_image_contents')
load('//tools/android/emulated_devices:macro/props_info.bzl',
     'generate_prop_content')


def _property_writer_impl(ctx):
  """Outputs a property file."""
  output = ctx.outputs.out
  ctx.file_action(output=output, content=ctx.attr.content)


_property_writer = rule(
    attrs={'content': attr.string()},
    outputs={'out': '%{name}.properties'},
    implementation=_property_writer_impl,)


HEAP_GROWTH_LIMIT = 'dalvik.vm.heapgrowthlimit'


def make_device(name,
                horizontal_resolution,
                vertical_resolution,
                screen_density,
                ram,
                vm_heap,
                cache,
                min_api,
                max_api=None,
                system_image_flavors=None,
                boot_properties=None,
                avd_properties=None,
                boot_apks=None,
                visibility=None,
                emulator_types=None,
                archs_override=None,
                pastries=None):
  """Generates a set of android_device targets.

  It is recommended to use new_devices() instead.

  Arguments:
    name: Unused - SORRY!
    horizontal_resolution: see android_device.
    vertical_resolution: see android_device.
    screen_density: see android_device.
    ram: see android_device.
    vm_heap: see android_device.
    cache: see android_device.
    min_api: the minimum api to generate.
    max_api: [optional] the maximum api level to generate.
    system_image_flavors: a list of flavours to generate from. Flavours are
      defined in //tools/android/emulated_devices/macro/image_info
    boot_properties: [optional] A dictionary containing system propreties.
    avd_properties: [optional] A dictionary containing avd propreties.
    boot_apks: [optional] a list of apks to install on the emulator during the
      boot cycle.
    visibility: [optional] visibility to assign the android_device objects.
    emulator_types: [optional] A list of qemu, qemu2, uma.
    archs_override: [optional] overrides any restrictions a particular emulator
      may define about running a particular architecture. (For example, we do
      not use QEMU to run ARM images after api level 19, but if you want to
      deal with the inheritant issues, feel free!
    pastries: [optional] a list of images in active development from pastry_info

    Returns:
      A set of all types of android_devices that meet the specified
      requirements.
  """
  name = name  # unused argument - historical reasons :(
  emulators = None
  if emulator_types:
    emulators = [TYPE_TO_EMULATOR[et] for et in emulator_types]

  new_devices(
      name='',
      hardware=new_hardware(horizontal_resolution, vertical_resolution,
                            screen_density, ram, vm_heap, cache),
      min_api=min_api,
      max_api=max_api,
      user_props=new_props(
          boot_properties=boot_properties, avd_properties=avd_properties),
      boot_apks=boot_apks,
      emulators=emulators,
      archs_override=archs_override,
      system_image_flavors=system_image_flavors,
      visibility=visibility,
      pastries=pastries)


def _make_property_target(name, user_props, emulator, hardware, image):
  target_name = '%s_%s_%s_%s%s_props' % (name, image_flavor(image),
                                         image_version_string(image), image_arch(image),
                                         emulator_suffix(emulator))
  _property_writer(
      name=target_name,
      content=generate_prop_content(user_props, emulator, hardware, image),
      visibility=['//visibility:private'])
  return ':%s' % target_name


def _make_system_image_target(name, contents):
  target_name = '%s_images' % name
  native.filegroup(
      name=target_name, srcs=contents, visibility=['//visibility:private'])
  return ':%s' % target_name


def new_devices(name,
                hardware,
                min_api,
                max_api=None,
                system_image_flavors=None,
                user_props=None,
                emulators=None,
                boot_apks=None,
                visibility=None,
                archs_override=None,
                pastries=None):
  """Generates a set of android_device targets.

  Arguments:
    name: a name to prefix devices with.
    hardware: A dictionary containing hardware attributes. Create with
      //tools/android/emulated_devices/macro/hardware:new_hardware.
    min_api: the minimum api to generate.
    max_api: [optional] the maximum api level to generate.
    system_image_flavors: a list of flavours to generate from. Flavours are
      defined in //tools/android/emulated_devices/macro/image_info
    user_props: [optional] A dictionary containing system and avd properties.
      Create with //tools/android/emulated_devices/macro/props:new_props.
      Elements in this dictionary will override any properties defined by the
      system image or hardware themselves. The emulator (QEMU/UMA/QEMU2) may
      override certain user specified properties though.
    emulators: [optional] A list of emulator binaries to run the device on
      use the constants defined in
      //tools/android/emulated_devices/macro/emulator_info
    boot_apks: [optional] a list of apks to install on the emulator during the
      boot cycle.
    visibility: [optional] visibility to assign the android_device objects.
    archs_override: [optional] overrides any restrictions a particular emulator
      may define about running a particular architecture. (For example, we do
      not use QEMU to run ARM images after api level 19, but if you want to
      deal with the inheritant issues, feel free!
    pastries: [optional] a list of images in active development from pastry_info

    Returns:
      A set of all types of android_devices that meet the specified
      requirements.
  """

  user_props = user_props or new_props()
  emulators = emulators or [UMA, QEMU, QEMU2]
  visibility = visibility or ['//visibility:public']
  boot_apks = boot_apks or []
  requested_flavors = system_image_flavors or ['google', 'android']
  images_to_build = []
  for api, images in sorted(API_TO_IMAGES.items()):
    if max_api and api > max_api:
      continue
    if api < min_api:
      continue
    images_to_build.extend(images)
  images_to_build.extend(pastries or [])
  for image in images_to_build:
    if image_flavor(image) not in requested_flavors:
      continue
    for emulator in emulators:
      api = image_api(image)
      arches = archs_override or supported_arches_for_api(emulator, api)
      if image_arch(image) not in arches:
        continue
      tags = emulator_tags(emulator, image)
      property_target = _make_property_target(name, user_props, emulator,
                                              hardware, image)
      system_image_contents = extra_system_image_contents(emulator, image)
      device_name = '%s_%s_%s%s' % (image_flavor(image),
                                    image_version_string(image), image_arch(image),
                                    emulator_suffix(emulator))

      if name:
        device_name = '%s_%s' % (name, device_name)
      # once boot_apks is a top level attribute, simplify this.
      system_image_target = _make_system_image_target(
          device_name, system_image_contents + boot_apks)

      native.android_device(
          name=device_name,
          default_properties=property_target,
          system_image=system_image_target,
          tags=tags,
          visibility=image_device_visibility(image) or visibility,
          **hardware_device_attributes(hardware))

      if image_supports_gms_channels(image):
        for cheese in compatible_cheeses[image_api(image)]:
          gms_device_name = '%s_gms_%s' % (device_name, cheese)

          cheese_apk = _get_cheese_apk(image, cheese)
          # once boot_apks is a top level attribute, simplify this.
          cheese_system_image_target = _make_system_image_target(
              gms_device_name,
              system_image_contents + boot_apks + [cheese_apk])
          native.android_device(
              name=gms_device_name,
              default_properties=property_target,
              system_image=cheese_system_image_target,
              tags=tags,
              visibility=image_device_visibility(image) or visibility,
              **hardware_device_attributes(hardware))

def _get_cheese_apk(image, cheese):
  """Helper function to get the proper gms core apk."""
  cheese_filegroup_target = '//third_party/java/android_apps/gcore:gcore_'
  if image_flavor(image) == 'wear':
    cheese_filegroup_target += 'wearable_'
  elif image_flavor(image) == 'tv':
    cheese_filegroup_target += 'atv_'
  else:
    if image_api(image) >= 23:
      cheese_filegroup_target += 'mnc_'
    elif image_api(image) >= 21:
      cheese_filegroup_target += 'lmp_'
    if image_arch(image) == 'arm':
      cheese_filegroup_target += 'arm_'

  cheese_filegroup_target += 'apk'
  if cheese != 'stable':
    cheese_filegroup_target += '_%s' % cheese
  return cheese_filegroup_target

