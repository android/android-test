@echo off

if "%1" == "" (
    echo Usage: NvContainerRecovery {Service Name}
    goto NvContainerRecoveryEnd
)

set __LOG_FILE=NvContainerRecovery.log
if not "%2" == "" set __LOG_FILE=C:\ProgramData\NVIDIA\NvContainerRecovery%1.log

set __RECOVERY_FILE=%LOCALAPPDATA%\NvContainerRecovery%1.reg

echo Create recovery registry file %__RECOVERY_FILE% > %__LOG_FILE%
echo REGEDIT4 > %__RECOVERY_FILE%
echo. >> %__RECOVERY_FILE%
echo [HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\NVIDIA Corporation\NvContainer\%1] >> %__RECOVERY_FILE%
echo "Recovery"=dword:00000001 >> %__RECOVERY_FILE%
echo. >> %__RECOVERY_FILE%
echo [HKEY_LOCAL_MACHINE\SOFTWARE\NVIDIA Corporation\NvContainer\%1] >> %__RECOVERY_FILE%
echo "Recovery"=dword:00000001 >> %__RECOVERY_FILE%
type %__RECOVERY_FILE% >> %__LOG_FILE%
echo Import %__RECOVERY_FILE% in registry >> %__LOG_FILE%
regedit.exe /s %__RECOVERY_FILE%

echo. >> %__RECOVERY_FILE%
echo Starting service %1 >> %__LOG_FILE%
net start %1 >> %__LOG_FILE% 2>&1
if %ERRORLEVEL% EQU 0 goto NvContainerRecoveryEnd

rem In case of failure, remove recovery setting from registry
echo. >> %__RECOVERY_FILE%
echo Prepare rollback registry file %__RECOVERY_FILE% >> %__LOG_FILE%
echo REGEDIT4 > %__RECOVERY_FILE%
echo. >> %__RECOVERY_FILE%
echo [HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\NVIDIA Corporation\NvContainer\%1] >> %__RECOVERY_FILE%
echo "Recovery"=- >> %__RECOVERY_FILE%
echo. >> %__RECOVERY_FILE%
echo [HKEY_LOCAL_MACHINE\SOFTWARE\NVIDIA Corporation\NvContainer\%1] >> %__RECOVERY_FILE%
echo "Recovery"=- >> %__RECOVERY_FILE%
type %__RECOVERY_FILE% >> %__LOG_FILE%

echo Import %__RECOVERY_FILE% in registry >> %__LOG_FILE%
regedit.exe /s %__RECOVERY_FILE%

:NvContainerRecoveryEnd
if exist "%__RECOVERY_FILE%" (
    echo. >> %__RECOVERY_FILE%
    echo Delete registry file %__RECOVERY_FILE% >> %__LOG_FILE%
    del /Q %__RECOVERY_FILE%
)
set __RECOVERY_FILE=
set __LOG_FILE=
