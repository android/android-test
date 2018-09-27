"""Hardware object contains hardware specifications we wish to emulator."""

load("//tools/android/emulated_devices:macro/props.bzl", "new_props")

def new_hardware(
        horizontal_resolution,
        vertical_resolution,
        screen_density,
        ram,
        vm_heap,
        cache,
        props = None):
    """Creates a hardware object See android_device documentation."""
    return {
        "horizontal_resolution": horizontal_resolution,
        "vertical_resolution": vertical_resolution,
        "screen_density": screen_density,
        "ram": ram,
        "vm_heap": vm_heap,
        "cache": cache,
        "props": props or new_props(),
    }

def hardware_device_attributes(hardware):
    """Returns a dict of all hardware attributes to place on an android_device."""
    attribs = dict(hardware)
    attribs.pop("props")
    return attribs

def hardware_props(hardware):
    """Props which should be added to the device running the hardware."""
    return hardware["props"]

def hardware_screen_density(hardware):
    return hardware["screen_density"]

def hardware_vm_heap(hardware):
    return hardware["vm_heap"]
