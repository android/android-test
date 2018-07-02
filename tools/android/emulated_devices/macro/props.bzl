"""Property objects specify system and avd properties."""

def new_props(boot_properties = None, avd_properties = None):
    """Creates a new properties object."""
    return {"boot": boot_properties or {}, "avd": avd_properties or {}}

def props_boot(props):
    """Accesses boot properties."""
    return props["boot"]

def props_avd(props):
    """Accesses avd properties."""
    return props["avd"]
