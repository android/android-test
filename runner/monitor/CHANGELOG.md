### monitor {version} {:#monitor-{version}}

{{date}}

`androidx.test:monitor:{version}` is released.

**Bug Fixes**

* Fixes missing IntentMonitor for startActivity with user overload. Now it's
  possible to intercept intents started with startActivityAsUser.

**New Features**

* Adds @Supersedes to ServiceLoaderWrapper so it's possible to choose one
implementation over another when multiple exist.

**Breaking Changes**

**API Changes**

**Breaking API Changes**

**Known Issues**
