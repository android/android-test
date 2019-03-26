"""Generate AXT release artifacts."""

load("//build_extensions:remove_from_jar.bzl", "remove_from_jar")
load("//build_extensions:add_or_update_file_in_zip.bzl", "add_or_update_file_in_zip")

def axt_release_lib(
    name,
    deps,
    custom_package = None,
    proguard_specs = None,
    proguard_library = None,
    multidex = "off",
    jarjar_rules = "//build_extensions:noJarJarRules.txt",
    keep_spec = None,
    remove_spec = None,
    overlapping_jars = [],
    resource_files = None):
  """Generates release artifacts for a AXT library.

  Resulting output will be two files:
  name_no_deps.jar and name.aar

  Args:
    name: The target name
    deps: The dependencies that make up the library
    custom_package: Option custom android package to use
    proguard_specs: Proguard to apply when building the jar
    proguard_library: Proguard to bundle with the jar
    jarjar_rules: Optional file containing jarjar rules to be applied
    keep_spec: A regex to match items to retain in the jar. This is typically the
      root java namespace of the library.
    remove_spec: A regex to match items to remove from the jar.
    overlapping_jars: jars containing entries to be removed from the main jar.
      This is useful when the library has dependencies whose java package namespaces
      overlap with this jar. See remove_from_jar docs for more details.
    resource_files: res files to include in library
  """

  # The rules here produce a final .aar artifact and jar for external release.

  # It is a 5 stage pipeline:
  # 1. Produce a placeholder .aar
  # 2. Produce a .jar including all classes and all its dependencies, and optionally proguard it via
  #    proguard_specs
  # 3. Rename classes if necessary via jarjar
  # 4. Strip out external dependencies from .jar
  # 5. Optionally, add in the proguard_library files to be bundled in the .aar
  # 6. Update the classes.jar inside the .aar from step 1 with the .jar from step 3

  # Step 1. Generate initial shell aar. The generated classes.jar will be empty.
  # See
  # https://bazel.build/versions/master/docs/be/android.html#android_library,
  # name.aar
  native.android_library(
      name = "%s_initial" % name,
      manifest = "AndroidManifest.xml",
      resource_files = resource_files,
      visibility = ["//visibility:private"],
      custom_package = custom_package,
      testonly = 1,
      exports = deps,
  )

  # Step 2. Generate jar containing all classes including dependencies.
  native.android_binary(
      name = "%s_all" % name,
      testonly = 1,
      manifest = "AndroidManifest.xml",
      multidex = multidex,
      custom_package = custom_package,
      proguard_specs = proguard_specs,
      deps = [
          ":%s_initial" % name,
      ],
  )

  expected_output = ":%s_all_deploy.jar" % name
  if proguard_specs:
    expected_output = ":%s_all_proguard.jar" % name

  # Step 3. Rename classes via jarjar
  native.java_binary(
    name = "jarjar_bin",
    main_class = "org.pantsbuild.jarjar.Main",
    runtime_deps = ["@maven//:org_pantsbuild_jarjar"],
  )
  native.genrule(
      name = "%s_jarjared" % name,
      srcs = [expected_output],
      outs = ["%s_jarjared.jar" % name],
      cmd = ("$(location :jarjar_bin) process " +
               "$(location %s) '$<' '$@'") % jarjar_rules,
      tools = [
          jarjar_rules,
	  ":jarjar_bin",
      ],
  )

  # Step 4. Strip out external dependencies. This produces the final name_no_deps.jar.
  remove_from_jar(
      name = "%s_no_deps" % name,
      jar = ":%s_jarjared.jar" % name,
      keep_spec = keep_spec,
      remove_spec = remove_spec,
      overlapping_jars = overlapping_jars,
  )

  expected_output = ":%s_initial.aar" % name
  if proguard_library:
    expected_output = "%s_with_proguard.aar" % name

    # Step 5. Add the proguard library file to the aar from the first step
    add_or_update_file_in_zip(
        name = "%s_add_proguard" % name,
        src = ":%s_initial.aar" % name,
        out = expected_output,
        update_path = "proguard.txt",
        update_src = proguard_library,
    )

  # Step 6. Update the .aar produced in the first step with the final .jar
  add_or_update_file_in_zip(
      name = name,
      src = expected_output,
      out = "%s.aar" % name,
      update_path = "classes.jar",
      update_src = ":%s_no_deps.jar" % name,
  )
