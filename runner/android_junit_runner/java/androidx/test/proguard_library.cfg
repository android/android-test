# Proguard flags for the AndroidJUnitRunner library.

# avoid obfuscation of the instrumentation runners and orchestrator
-keepnames class androidx.test.**

# Annotation classes accessed via reflection
-keep class androidx.test.annotation.** { *; }

# for 'can't find referenced method 'android.app.Instrumentation$ActivityResult execStartActivity' etc
-dontwarn androidx.test.runner.MonitoringInstrumentation

# for 'library class android.test.* extends or implements program class'
-dontwarn android.test.**

# for 'can't find referenced class java.lang.management.RuntimeMXBean'
-dontwarn org.junit.rules.DisableOnDebug

# for 'can't find referenced class java.lang.management.ThreadMXBean'
-dontwarn org.junit.internal.runners.statements.FailOnTimeout

# for 'can't find referenced class java.beans.**, easymock, etc
-dontwarn org.hamcrest.**