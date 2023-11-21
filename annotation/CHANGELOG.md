### Annotation 1.1.0-alpha02 {:#annotation-1.1.0-alpha02}

{{date}}

`androidx.test:annotation:1.1.0-alpha02` is released.

**Dependency Changes/Updates**

* minSdkVersion is now 19, targetSdkVersion is now 34

**Other**

* Replace build aliases to maven artifacts with direct references

* Replace single-export android_library with aliases

* Rework maven release build.

    This change reworks the aar building logic to produce aars with non-desugared, non-proguarded java8 bytecode,

    The aars corresponding source jar and external maven dependencies will also be automatically derived from the build graph.
