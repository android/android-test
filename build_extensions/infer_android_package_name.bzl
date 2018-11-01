"""A rule for inferring an android package name."""

def infer_android_package_name():
    """Infer an android package name based on current path below 'javatests'"""
    path = native.package_name()
    javatests_index = path.rindex("/javatests/") + len("/javatests/")
    return path[javatests_index:].replace("/", ".")
