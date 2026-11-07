### Core Core-ktx {version} {:#core-{version}}

{{date}}

`androidx.test:core:{version}` and `androidx.test:core-ktx:{version}` are released.

**Bug Fixes**

*   Fix `ActivityScenario.moveToState` and `ActivityScenario.close` throwing a
    `NullPointerException` ("Current state was null unexpectedly") when the
    activity is finishing but still in a transient lifecycle stage, such as an
    activity that finished itself while stopped.

**New Features**

**Breaking Changes**

**API Changes**

**Breaking API Changes**

**Known Issues**
