#!/system/bin/sh

exec >/dev/null
exec </dev/null
exec 2>&1


CLASSPATH=$(pm path  androidx.test.services.speakeasy.client) logwrapper app_process / androidx.test.services.speakeasy.client.Reverser &
exit 0
