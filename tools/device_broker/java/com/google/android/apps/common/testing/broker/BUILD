# -*- mode: python; -*-
#
# Description:
#   An interface for tests to access physical or virtual devices.

package(
    default_visibility = [
        ":internal",
        ":tests",
    ],
)

licenses(["notice"])  # Apache 2.0

package_group(
    name = "internal",
    packages = [
        "//java/com/google/android/apps/common/testing/broker/...",
    ],
)

package_group(
    name = "tests",
    packages = [
        "//javatests/com/google/android/apps/common/testing/broker/...",  # non-opensource tests.
        "//tools/device_broker/javatests/com/google/android/apps/common/testing/broker/...",
    ],
)

package_group(
    name = "test_suite",
    packages = [
        "//java/com/google/android/apps/common/testing/suite",
        "//javatests/com/google/android/apps/common/testing/suite",
        "//tools/device_broker/java/com/google/android/apps/common/testing/suite/...",
        "//tools/device_broker/javatests/com/google/android/apps/common/testing/suite/...",
    ],
)

java_library(
    name = "struct",
    srcs = [
        "ExecutedTest.java",
        "Instrumentation.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party/java/auto:auto_value",
        "//third_party/java/jsr305_annotations",
    ],
)

java_library(
    name = "enum",
    srcs = ["NetworkType.java"],
)

java_library(
    name = "anno",
    srcs = ["DeviceBrokerAnnotations.java"],
    deps = [
        "//third_party/java/guice",
    ],
)

java_library(
    name = "environment",
    srcs = [
        "Environment.java",
        "EnvironmentModule.java",
    ],
    visibility = [
        ":internal",
        ":tests",
        "//tools:__subpackages__",
    ],
    deps = [
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//third_party/java/guice",
        "//third_party/java/jsr305_annotations",
    ],
)

java_library(
    name = "shared_constants",
    srcs = [
        "SharedEnvironment.java",
    ],
    visibility = [":test_suite"],
    deps = [
        ":environment",
    ],
)

java_library(
    name = "private_impl",
    srcs = [
        "AaptDumpPackageLineProcessor.java",
        "AbstractRegexpLineProcessor.java",
        "AdbDevicesLineProcessor.java",
        "AdbInstrumentationListProcessor.java",
        "AndroidPropertyProcessor.java",
        "InstrumentationTestRunnerProcessor.java",
        "ShellVariableProcessor.java",
        "SimpleLineListProcessor.java",
        "StreamWritingProcessor.java",
        "SubprocessCommunicator.java",
    ],
    visibility = [
        ":internal",
        ":tests",
        "//tools/lib/adb:__subpackages__",
    ],
    deps = [
        ":anno",
        ":struct",
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/eventbus",
        "//java/com/google/common/io",
        "//tools/device_broker/java/com/google/android/apps/common/testing/broker/shell",
        "//third_party/java/jsr305_annotations",
        "//third_party/java/jsr330_inject",
    ],
)

java_library(
    name = "port_manager_module",
    srcs = [
        "PortManager.java",
        "PortManagerModule.java",
    ],
    deps = [
        ":anno",
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//third_party/java/guice",
        "//third_party/java/jsr305_annotations",
    ],
)

java_library(
    name = "test_info",
    srcs = ["TestInfoRepository.java"],
    visibility = [
        ":internal",
        "//tools:__subpackages__",
    ],
    deps = [
        ":anno",
        ":environment",
        ":struct",
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/cache",
        "//java/com/google/common/collect",
        "//java/com/google/common/io",
        "//tools/device_broker/java/com/google/android/apps/common/testing/broker/shell",
        "//tools/device_broker/java/com/google/android/apps/common/testing/suite/dex",
        "//tools/device_broker/proto:test_info_pb_java_proto",
        "//third_party/java/jsr330_inject",
    ],
)

java_library(
    name = "broker_intf",
    srcs = [
        "ActivityAction.java",
        "AdbController.java",
        "BadIdeaRuntimeException.java",
        "BrokeredDevice.java",
        "DeviceBroker.java",
        "DeviceBrokerDecorator.java",
        "DeviceBrokerType.java",
        "HostTestSize.java",
        "InstrumentationRepository.java",
        "LogcatFilter.java",
        "LogcatStreamer.java",
        "OpenGlDriver.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
        ":anno",
        ":private_impl",
        ":struct",
        ":test_info",
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/eventbus",
        "//java/com/google/common/hash",
        "//java/com/google/common/io",
        "//java/com/google/common/net",
        "//tools/device_broker/java/com/google/android/apps/common/testing/broker/shell",
        "//tools/device_broker/proto:test_info_pb_java_proto",
        "//third_party/java/guice",
        "//third_party/java/jsr305_annotations",
        "//third_party/java/jsr330_inject",
    ],
)

java_library(
    name = "broker_factory",
    srcs = ["DeviceBrokerFactory.java"],
    visibility = [":test_suite"],
    deps = [
        ":anno",
        ":base_module",
        ":broker_decorators",
        ":broker_intf",
        ":environment",
        ":jcommander_support",
        ":local_device",
        ":port_manager_module",
        ":wrapped_device",
        "//third_party/java/guice",
    ],
)

java_library(
    name = "local_device",
    srcs = [
        "LocalAdbServerDeviceBroker.java",
        "LocalBrokerModule.java",
    ],
    visibility = [":internal"],
    deps = [
        ":anno",
        ":broker_intf",
        ":private_impl",
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/cache",
        "//java/com/google/common/collect",
        "//third_party/java/guice",
    ],
)

java_library(
    name = "broker_decorators",
    srcs = [
        "BrokeredDeviceExportedPropertyRecorder.java",
        "DataStagingDeviceBrokerDecorator.java",
        "FixtureScriptExecutingDeviceBrokerDecorator.java",
        "InstrumentationExecutingDeviceBrokerDecorator.java",
        "ProxyConfiguringDeviceBrokerDecorator.java",
        "ProxyConfiguringModule.java",
    ],
    visibility = [
        ":internal",
        ":test_suite",
        ":tests",
    ],
    deps = [
        ":anno",  # ready
        ":broker_intf",  # logcat filter :(
        ":struct",  # ready
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/net",
        "//third_party/java/guice",
        "//third_party/java/jsr330_inject",
    ],
)

java_library(
    name = "wrapped_device",
    srcs = [
        "WrappedBrokerModule.java",
        "WrappedEmulatedDeviceBroker.java",
    ],
    visibility = [":internal"],
    deps = [
        ":anno",
        ":broker_intf",
        ":enum",
        ":environment",
        ":private_impl",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/io",
        "//java/com/google/common/util",
        "//java/com/google/protobuf",
        "//tools/android/emulator:emulator_meta_data_pb_java_proto",
        "//third_party/java/guice",
        "//third_party/java/jsr305_annotations",
    ],
)

java_library(
    name = "base_module",
    srcs = [
        "AaptUtil.java",
        "DeviceBrokerModule.java",
        "DirectorySearcher.java",
        "ResourceProvider.java",
    ],
    data = [
        "//third_party/java/android/android_sdk_linux:dynamic_adb_tool",
        "//third_party/java/android/android_sdk_linux:static_adb_tool",
    ],
    resources = [
        ":executor.sh",
        "//java/com/google/android/apps/common/testing/services:basic_services.apk",
        "//runner/android_test_orchestrator/stubapp:stubapp.apk",
        "//services:test_services.apk",
        "//third_party/java/android/android_sdk_linux:dynamic_aapt_tool",
        "//tools/android:dexdump",
    ],
    visibility = [
        ":internal",
        ":test_suite",
        ":tests",
    ],
    deps = [
        ":anno",
        ":broker_intf",
        ":enum",
        ":environment",
        ":private_impl",
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/cache",
        "//java/com/google/common/collect",
        "//java/com/google/common/io",
        "//java/com/google/common/net",
        "//java/com/google/common/util/concurrent",
        "//third_party/java/guice",
    ],
)

java_library(
    name = "jcommander_support",
    srcs = [
        "DeviceBrokerOptions.java",
        "EnumConverters.java",
        "LogcatFilterConverter.java",
    ],
    visibility = [":internal"],
    deps = [
        ":anno",
        ":broker_intf",
        ":enum",
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//third_party/java/guice",
        "//third_party/java/jcommander",
        "//third_party/java/jsr305_annotations",
    ],
)
