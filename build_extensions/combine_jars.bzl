"""Combines multiple jars into one jar."""

def combine_jars(name, srcs, **genrule_kwargs):
  '''Combines multiple jars into one jar.

  Args:
    name: Name to be used for this rule. It produces name.jar
    srcs: List of jars to be combined.
    genrule_kwargs: Keyword arguments to pass through to the genrule.
  '''

  native.genrule(
        name=name,
        srcs=srcs,
        outs=["%s.jar" % name],
        tools=["@local_jdk//:jar"],
        message="Combining following jars: %s" % ",".join(srcs),
        cmd=(
            # Absolutify $JAR for jdk-in-perforce
            '{ [[ "$${JAR=$(location @local_jdk//:jar)}" =~ ^/ ]] || ' +
            'JAR="$$PWD/$$JAR"; } && ' +
            "cwd=$$PWD && tmp=$$(mktemp -d) && cd $${tmp} && " +
            # Extract each jar to its own subdirectory so there's no race between
            # parallel extraction processes trying to write/overwrite same files.
            "src_jar_num=0 && " +
            "for src in $(SRCS);" +
            "  do mkdir $${src_jar_num} && " +
            "     (cd ./$${src_jar_num} && $$JAR xf $${cwd}/$${src} > /dev/null) & " +
            "     src_jar_num=$$((src_jar_num + 1));" +
            "done && wait && " +
            "i=1 && while ((i < src_jar_num));" +
            "  do cp -R ./$$i/* ./0/; (rm -rf ./$$i) & i=$$((i+1)); done && " +
            "if ((src_jar_num == 0)); then mkdir 0; fi && " +
            "($$JAR cf $${cwd}/$@ -C ./0 . > /dev/null) && wait &&" +
            "rm -fr $${tmp} > /dev/null"),
        **genrule_kwargs)
