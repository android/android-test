# Copyright 2012 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Tests for android_test_support.third_party.android.android_test_support.tools.android.emulator.emulated_device."""




import collections
import os
import tempfile


import mox

from tools.android.emulator import resources
from google.apputils import basetest as googletest
from tools.android.emulator import common
from tools.android.emulator import emulated_device
from tools.android.emulator import emulator_meta_data_pb2
from tools.android.emulator import fake_android_platform_util


root_dir = os.path.abspath(os.path.join(resources.GetRunfilesDir(),
                                        'android_test_support'))

ANR_LOGS = """
I/am_proc_died(  951): [0,1753,com.android.contacts]
I/am_proc_died(  951): [0,1644,com.android.music]
I/am_crash(  951): [2280,0,com.an.de,89,java.lang.Ill,When they c,Ba.java,147]
I/am_proc_died(  951): [0,2280,com.android.development]
I/am_crash(  951): [2386,0,c.a.d,8961605,j.lang.Il,When they come,Bad.java,155]
I/am_proc_died(  951): [0,2386,com.android.development]
I/am_proc_died(  951): [0,2409,com.android.development]
I/am_anr  (  951): [0,2426,c.a.d,8961605,keyDispatchingTimedOut]
I/am_anr  (  951): [0,2426,c.a.de,8961605,keyDispatchingTimedOut]
I/am_proc_died(  951): [0,2426,com.android.development]
I/am_anr  (  951): [0,2452,com.android.de,8961605,keyDispatchingTimedOut]
I/am_proc_died(  951): [0,1446,com.google.process.gapps]
I/am_proc_died(  951): [0,1484,com.android.phone]
I/am_proc_died(  951): [0,1400,com.google.process.location]
I/am_proc_died(  951): [0,1796,com.google.android.apps.maps:FriendService]
I/am_proc_died(  951): [0,1873,com.google.android.apps.maps:LocationFriendSer]
I/am_proc_died(  951): [0,1821,com.android.mms]
I/am_proc_died(  951): [0,1934,com.android.defcontainer]
I/am_proc_died(  951): [0,1692,com.android.quicksearchbox]
I/am_proc_died(  951): [0,1967,com.svox.pico]
I/am_proc_died(  951): [0,2129,com.android.browser]
I/am_proc_died(  951): [0,2154,com.android.sharedstoragebackup]
I/am_proc_died(  951): [0,1506,com.android.settings]
I/am_proc_died(  951): [0,1674,com.android.calendar]
I/am_proc_died(  951): [0,1657,com.android.deskclock]
I/am_proc_died(  951): [0,1706,com.android.providers.calendar]
I/am_crash(  951): [2712,0,com.a.dev,896,ja.la.Il,When they come ,Bad.java,147]
"""

_CONSOLE_TOKEN_DEVICE_PATH = '/data/console_token'


class EmulatedDeviceTest(mox.MoxTestBase):

  def setUp(self):
    super(EmulatedDeviceTest, self).setUp()
    self._common_spawn = common.SpawnAndWaitWithRetry

  def tearDown(self):
    super(EmulatedDeviceTest, self).tearDown()
    common.SpawnAndWaitWithRetry = self._common_spawn

  def testExecOnEmulator_ToggledOn(self):
    self.mox.ReplayAll()
    test_plat = emulated_device.AndroidPlatform()
    test_plat.adb = '/bin/echo'
    test_plat.real_adb = '/bin/echo'
    mock_device = emulated_device.EmulatedDevice(
        android_platform=test_plat,
        emulator_adb_port=1234,
        emulator_telnet_port=4567,
        device_serial='localhost:1234')
    mock_device._metadata_pb = emulator_meta_data_pb2.EmulatorMetaDataPb()

    self.assertEquals('-s localhost:1234 shell echo hello\n',
                      mock_device.ExecOnDevice(['echo', 'hello']))

  def testExecOnEmulator_NormalBoot(self):
    self.mox.ReplayAll()
    test_plat = emulated_device.AndroidPlatform()
    test_plat.adb = '/bin/echo'
    test_plat.real_adb = '/bin/echo'
    mock_device = emulated_device.EmulatedDevice(
        android_platform=test_plat,
        emulator_adb_port=1234,
        emulator_telnet_port=4567,
        device_serial='localhost:1234')
    mock_device._metadata_pb = emulator_meta_data_pb2.EmulatorMetaDataPb()

    self.assertEquals('-s localhost:1234 shell echo hello\n',
                      mock_device.ExecOnDevice(['echo', 'hello']))

  def testEmulatorPing_noConnect(self):
    mock_device = emulated_device.EmulatedDevice(
        android_platform=fake_android_platform_util.BuildAndroidPlatform())
    self.mox.StubOutWithMock(mock_device, '_CanConnect')
    mock_device._CanConnect().AndReturn(False)

    self.mox.ReplayAll()

    self.assertFalse(mock_device.Ping())

  def testEmulatorPing_noSystemServer(self):
    mock_device = emulated_device.EmulatedDevice(
        android_platform=fake_android_platform_util.BuildAndroidPlatform())
    self.mox.StubOutWithMock(mock_device, '_CanConnect')
    self.mox.StubOutWithMock(mock_device, '_CheckSystemServerProcess')
    mock_device._CanConnect().AndReturn(True)
    mock_device._CheckSystemServerProcess().AndReturn(False)

    self.mox.ReplayAll()

    self.assertFalse(mock_device.Ping())

  def testEmulatorPing_healthy(self):
    mock_device = emulated_device.EmulatedDevice(
        android_platform=fake_android_platform_util.BuildAndroidPlatform())
    self.mox.StubOutWithMock(mock_device, '_CanConnect')
    self.mox.StubOutWithMock(mock_device, '_CheckSystemServerProcess')
    self.mox.StubOutWithMock(mock_device, '_CheckPackageManagerRunning')
    mock_device._CanConnect().AndReturn(True)
    mock_device._CheckSystemServerProcess().AndReturn(True)
    mock_device._CheckPackageManagerRunning().AndReturn(True)

    self.mox.ReplayAll()

    self.assertTrue(mock_device.Ping())

  def testBiosDir_NoExplicitFiles(self):
    android_platform = fake_android_platform_util.BuildAndroidPlatform()
    bios_dir = android_platform.MakeBiosDir(tempfile.mkdtemp())
    self.assertTrue(os.path.exists(bios_dir), 'pc-bios dir doesnt exist: %s' %
                    bios_dir)
    self.assertTrue(os.listdir(bios_dir), 'pc-bios dir is empty! %s' %
                    bios_dir)

  def testBiosDir_ExplicitFiles(self):
    android_platform = fake_android_platform_util.BuildAndroidPlatform()
    temp = tempfile.NamedTemporaryFile()
    android_platform.bios_files = [temp.name]
    bios_dir = android_platform.MakeBiosDir(tempfile.mkdtemp())
    bios_contents = os.listdir(bios_dir)

    self.assertTrue(
        os.path.basename(temp.name) in bios_contents,
        'bios dir missing: %s, has: %s' % (temp.name, bios_contents))

  def testDetermineArchitecture_FromSourceProperties(self):
    random_properties = {'systemimage.abi': 'foobarbaz'}
    device = emulated_device.EmulatedDevice()
    self.assertEquals('foobarbaz',
                      device._DetermineArchitecture(random_properties),
                      'systemimage.abi should be the source of arch.')

  def testDetermineQemuArgs_FromSourceProperties(self):
    props = {'systemimage.abi': 'armeabi-v7a'}
    device = emulated_device.EmulatedDevice()

    props = {'systemimage.abi': 'armeabi'}
    self.assertEquals([],
                      device._DetermineQemuArgs(props, False))
    self.assertEquals([],
                      device._DetermineQemuArgs(props, True))

    props = {'systemimage.abi': 'x86'}
    self.assertEquals(['-disable-kvm'],
                      device._DetermineQemuArgs(props, False))
    self.assertEquals(['-enable-kvm', '-append', 'nopat'],
                      device._DetermineQemuArgs(props, True))

  def testAdbEnv_NoPort(self):
    device = emulated_device.EmulatedDevice(
        android_platform=fake_android_platform_util.BuildAndroidPlatform())
    env = device._AdbEnv()
    self.assertIsNotNone(device.adb_server_port, 'Adb Server Port should '
                         'auto assign')
    self.assertEquals(
        str(device.adb_server_port), env['ANDROID_ADB_SERVER_PORT'],
        'Adb port mismatches between class and environment.')

  def testAdbEnv_AssignedPort(self):
    device = emulated_device.EmulatedDevice(
        adb_server_port=1234,
        android_platform=fake_android_platform_util.BuildAndroidPlatform())
    self.assertEquals(str(1234), device._AdbEnv()['ANDROID_ADB_SERVER_PORT'])

  def testStartEmulator_BadSystemDir(self):
    device = emulated_device.EmulatedDevice()
    self.assertRaises(
        Exception,
        device.Configure,
        'foo/bar/baz/1234',
        '480x800',
        '1024',
        '130',
        '64',
        source_properties={
            'systemimage.abi': 'x86',
            'androidversion.apilevel': '10'
        })

  def testStartEmulator_NoInitialData(self):
    device = emulated_device.EmulatedDevice()
    self.assertRaises(
        Exception,
        device.Configure,
        tempfile.mkdtemp(),
        '480x800',
        '1024',
        '130',
        '64',
        source_properties={
            'systemimage.abi': 'x86',
            'androidversion.apilevel': '10'
        })

  def testStartEmulator_EmulatorDies(self):
    platform = fake_android_platform_util.BuildAndroidPlatform()
    platform.adb = '/bin/echo'
    device = emulated_device.EmulatedDevice(
        android_platform=fake_android_platform_util.BuildAndroidPlatform())
    with tempfile.NamedTemporaryFile(delete=False) as f:
      device._emulator_log_file = f.name
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     source_properties={'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '10'},
                     system_image_path=os.path.join(
                         fake_android_platform_util.GetSystemImageDir(),
                         'system.img'))
    try:
      device.StartDevice(False, 0)
      self.fail('Device couldn\'t possibly launch - bad arch')
    except Exception as e:
      if 'has died' not in e.message:
        raise e

  def testConfigureEmulator_avdProps(self):
    device = emulated_device.EmulatedDevice()
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     default_properties={'avd_config_ini.hw.mainkeys': 'no'},
                     source_properties={'avd_config_ini.hw.keyboard.lid': 'no',
                                        'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '10'})
    found = set()
    for prop in device._metadata_pb.avd_config_property:
      if prop.name == 'hw.mainKeys':
        found.add(prop.name)
        self.assertEquals('no', prop.value)
      elif prop.name == 'hw.keyboard.lid':
        found.add(prop.name)
        self.assertEquals('no', prop.value)
      elif prop.name == 'hw.keyboard':
        found.add(prop.name)
        self.assertEquals('yes', prop.value)

    self.assertEquals(3, len(found))

  def testConfigureEmulator_customSdCard(self):
    device = emulated_device.EmulatedDevice()
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     default_properties={'sdcard_size_mb': '2048'},
                     source_properties={'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '15'})
    self.assertEquals(2048, device._metadata_pb.sdcard_size_mb)

  def testConfigureEmulator_defaultSdCard(self):
    device = emulated_device.EmulatedDevice()
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     source_properties={'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '15'})
    self.assertEquals(256, device._metadata_pb.sdcard_size_mb)

    device = emulated_device.EmulatedDevice()
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     default_properties={'some_other_key': 'foo'},
                     source_properties={'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '10'})
    self.assertEquals(256, device._metadata_pb.sdcard_size_mb)

  def testBroadcastDeviceReady_extras(self):
    device = emulated_device.EmulatedDevice(
        android_platform=fake_android_platform_util.BuildAndroidPlatform())
    device._CanConnect = lambda: True
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     default_properties={'some_other_key': 'foo'},
                     source_properties={'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '15'})

    called_with = []

    def StubExecOnEmulator(args, **unused_kwds):
      called_with.extend(args)

    device.ExecOnDevice = StubExecOnEmulator
    extras = collections.OrderedDict()
    extras['hello'] = 'world'
    extras['something'] = 'new'
    device.BroadcastDeviceReady(extras)
    self.assertEquals(
        [
            'am', 'broadcast', '-a',
            'ACTION_MOBILE_NINJAS_START', '-f', '268435488',
            '-e', 'hello', 'world', '-e', 'something', 'new',
            'com.google.android.apps.common.testing.services.bootstrap'],
        called_with)

  def testBroadcastDeviceReady_withNoArgs(self):
    device = emulated_device.EmulatedDevice()
    device._CanConnect = lambda: True
    called_with = []

    def StubTestAdbCall(args):
      called_with.extend(args)
    device.ExecOnDevice = StubTestAdbCall
    device.BroadcastDeviceReady()
    # do not broadcast!
    self.assertEquals([], called_with)

  def testBroadcastDeviceReady_booleanExtras(self):
    device = emulated_device.EmulatedDevice(
        android_platform=fake_android_platform_util.BuildAndroidPlatform())
    device._CanConnect = lambda: True
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     default_properties={'some_other_key': 'foo'},
                     source_properties={'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '15'})

    called_with = []

    def StubExecOnEmulator(args, **unused_kwds):
      called_with.extend(args)

    device.ExecOnDevice = StubExecOnEmulator
    extras = collections.OrderedDict()
    extras['boolkey'] = True
    device.BroadcastDeviceReady(extras)
    self.assertEquals(
        [
            'am', 'broadcast', '-a',
            'ACTION_MOBILE_NINJAS_START', '-f', '268435488',
            '--ez', 'boolkey', 'true',
            'com.google.android.apps.common.testing.services.bootstrap'],
        called_with)

  def testBroadcastDeviceReady_action(self):
    device = emulated_device.EmulatedDevice(
        android_platform=fake_android_platform_util.BuildAndroidPlatform())
    device._CanConnect = lambda: True
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     default_properties={'some_other_key': 'foo'},
                     source_properties={'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '15'})

    called_with = []

    def StubExecOnEmulator(args, **unused_kwds):
      called_with.extend(args)

    device.ExecOnDevice = StubExecOnEmulator
    extras = collections.OrderedDict()
    extras['hello'] = 'world'
    extras['something'] = 'new'
    action = 'com.google.android.apps.common.testing.services.TEST_ACTION'
    device.BroadcastDeviceReady(extras, action)
    self.assertEquals(
        [
            'am', 'broadcast', '-a',
            action, '-f', '268435488',
            '-e', 'hello', 'world', '-e', 'something', 'new'],
        called_with)

  def testFindProcsToKill(self):
    device = emulated_device.EmulatedDevice()
    procs_to_kill = device._FindProcsToKill(ANR_LOGS)
    self.assertEquals(set(procs_to_kill), set(['2712', '2452']))

  def testMapToSupportedDensity(self):
    device = emulated_device.EmulatedDevice()

    self.assertEquals(120, device._MapToSupportedDensity(1))
    self.assertEquals(213, device._MapToSupportedDensity(213))
    self.assertEquals(240, device._MapToSupportedDensity(214))
    self.assertEquals(280, device._MapToSupportedDensity(270))
    self.assertEquals(360, device._MapToSupportedDensity(370))
    self.assertEquals(640, device._MapToSupportedDensity(1000))

  def testConfigureEmulator_useAdbdPipe(self):
    device = emulated_device.EmulatedDevice()
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     source_properties={'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '15'})
    self.assertEquals(False, device._metadata_pb.with_adbd_pipe)
    self.assertEquals(False, device._metadata_pb.with_patched_adbd)

    device = emulated_device.EmulatedDevice()
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     source_properties={'systemimage.abi': 'x86',
                                        'androidversion.apilevel': '10'})
    self.assertEquals(False, device._metadata_pb.with_adbd_pipe)
    self.assertEquals(False, device._metadata_pb.with_patched_adbd)

    device = emulated_device.EmulatedDevice()
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     source_properties={'systemimage.abi': 'armeabi',
                                        'androidversion.apilevel': '10'})
    self.assertEquals(False, device._metadata_pb.with_adbd_pipe)
    self.assertEquals(False, device._metadata_pb.with_patched_adbd)

    device = emulated_device.EmulatedDevice()
    device.Configure(fake_android_platform_util.GetSystemImageDir(),
                     '480x800',
                     1024,
                     133,
                     36,
                     source_properties={'systemimage.abi': 'armeabi-v7a',
                                        'androidversion.apilevel': '15'})
    self.assertEquals(False, device._metadata_pb.with_adbd_pipe)
    self.assertEquals(False, device._metadata_pb.with_patched_adbd)

  def testSanityTestOpenGL_bogusDriver(self):
    device = emulated_device.EmulatedDevice()

    self.assertRaises(AssertionError, device._SanityCheckOpenGLDriver,
                      'bacon', False)

    self.assertRaises(AssertionError, device._SanityCheckOpenGLDriver,
                      'Reinheitsgebot', True)

  def testSanityTestOpenGL_supportedChecking(self):
    device = emulated_device.EmulatedDevice()
    device._metadata_pb = emulator_meta_data_pb2.EmulatorMetaDataPb(
        supported_open_gl_drivers=[
            emulated_device.MESA_OPEN_GL])

    self.assertRaises(AssertionError, device._SanityCheckOpenGLDriver,
                      emulated_device.HOST_OPEN_GL, False)

    self.assertRaises(AssertionError, device._SanityCheckOpenGLDriver,
                      emulated_device.NO_OPEN_GL, False)

    device._SanityCheckOpenGLDriver(emulated_device.MESA_OPEN_GL,
                                    False)
    device._SanityCheckOpenGLDriver(emulated_device.MESA_OPEN_GL,
                                    True)
    device._SanityCheckOpenGLDriver(emulated_device.HOST_OPEN_GL,
                                    True)
    device._SanityCheckOpenGLDriver(emulated_device.NO_OPEN_GL,
                                    True)

  def testDisableSideLoading(self):
    self.VerifyDisableSideLoading('17', 'global')
    self.VerifyDisableSideLoading('19', 'global')
    self.VerifyDisableSideLoading('21', 'secure')
    self.VerifyDisableSideLoading('15', 'secure')

  def testAuth(self):
    device = emulated_device.EmulatedDevice()

    base_dir = tempfile.mkdtemp()
    fname = os.path.join(base_dir, '.emulator_auth_token')
    with open(fname, 'w') as f:
      f.write('123\n')

    mock_sock = self.mox.CreateMockAnything()
    mock_sock.read_until('OK', 1.0).AndReturn(
        'Android Console: Authentication required\n'
        'Android Console: type \'auth <auth_token>\' to authenticate\n'
        'Android Console: you can find your <auth_token> in\n'
        '\'%s\'\nOK' % fname)
    mock_sock.write('auth 123\n')
    mock_sock.read_until('\n', 1.0).AndReturn('OK')

    self.mox.ReplayAll()
    device._TryAuth(mock_sock)
    self.mox.VerifyAll()

  def testNoAuth(self):
    device = emulated_device.EmulatedDevice()

    mock_sock = self.mox.CreateMockAnything()
    mock_sock.read_until('OK', 1.0).AndReturn('OK')

    self.mox.ReplayAll()
    device._TryAuth(mock_sock)
    self.mox.VerifyAll()

  def VerifyDisableSideLoading(self, api_level, table_name):
    device = emulated_device.EmulatedDevice()
    device._metadata_pb = emulator_meta_data_pb2.EmulatorMetaDataPb(
        api_name=api_level)
    called_with = []

    def StubTestAdbCall(args):
      called_with.extend(args)
    device.ExecOnDevice = StubTestAdbCall
    device._DisableSideloading()
    self.assertSideLoading(api_level, table_name, called_with)

  def assertSideLoading(self, api_level, table_name, called_with,):
    api_level = int(api_level)
    if api_level > 16:
      self.assertEquals([
          'settings put %s install_non_market_apps 0' % table_name,
          'pm',
          'disable com.android.providers.settings'], called_with)
    elif api_level == 16:
      self.assertEquals([
          'content insert --uri content://settings/%s --bind '
          'name:s:install_non_market_apps --bind value:s:0' %
          table_name, 'pm',
          'disable com.android.providers.settings'], called_with)
    else:
      self.assertEquals(
          ['sqlite3',
           '/data/data/com.android.providers.settings/databases/settings.db',
           '"INSERT OR REPLACE INTO secure (name, value) VALUES '
           '(\'install_non_market_apps\', 0);"',
           'pm', 'disable com.android.providers.settings'], called_with)


if __name__ == '__main__':
  googletest.main()
