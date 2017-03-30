How to build GoogleAccountUtil.apk

Getting the Source:
-------------------
repo init -u persistent-https://googleplex-android.git.corp.google.com/platform/manifest -b tradefed
repo sync

Building:
---------
. build/envsetup
tapas GoogleAccountUtil
make


Source code in code search:
---------------------------
https://cs.corp.google.com/#android/vendor/google_tradefederation/util-apps/AccountUtil/


Trade Federation‎ development environment :
-----------------------------------------
https://sites.google.com/a/google.com/android-test-engineering/test-infrastructure-1/trade-federation-1/getting-started/development-environment
