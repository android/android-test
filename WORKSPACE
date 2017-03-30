workspace(name = "android_test_support")


load("//tools/android/emulator:unified_launcher.bzl", "load_workspace")
load_workspace()

# This is needed to run the integration tests. $ANDROID_HOME must be set.
android_sdk_repository(name = "androidsdk")