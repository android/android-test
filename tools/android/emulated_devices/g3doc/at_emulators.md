# Running Android Things Emulator

## 1. Running on Work Station:

### Running within android_test_support client(with bazel):

To start the emulator: `bazel run
tools/android/emulated_devices/amber:jasper_24_x86_qemu2` \
**jasper_24_x86** refer to the image
here:android_test_support/third_party/java/android/system_images/jasper_24/x86/ \
**qemu2** refer to the supported emualtor. \
**amber** refer to the device specification. (other options are: blueberry,
coral and daffodil)

### Running outside android_test_support client(with crow):

You need to install crow (go/crow) first. \
To start the emulator: `crow --device=amber --api_level=24 [-arch=x86_qemu2]` \
x86_qemu2 is the by default architecture for crow.

### Running from AndroidStudio(via crow plugin):

You can start the emulator from the crow plugin \
Click Crow Plugin -> Select Device:amber(or any Jasper device) -> Select API
level:24 -> Select Architecture:x86_qemu2 -> OK

![this](https://screenshot/NDfo5aUUf4a.png)

## 2. Running with android_test:

You can also run android_test with Andorid Things Emulators the same way as
other emulators. \
example android_test BUILD:

```live-snippet
cs/file://depot/android_test_support/javatests/com/google/assistant/display/android/core/BUILD build_rule:AssistantCoreServiceTest
```
