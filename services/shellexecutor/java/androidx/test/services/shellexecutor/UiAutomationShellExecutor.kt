package androidx.test.services.shellexecutor

import android.app.UiAutomation
import android.os.ParcelFileDescriptor
import java.io.InputStream

/**  */
class UiAutomationShellExecutor(private val uiAutomation: UiAutomation) : ShellExecutor {
  override fun getBinderKey(): String {
    return BINDER_KEY
  }

  override fun executeShellCommandSync(
    command: String,
    parameters: List<String>,
    shellEnv: Map<String, String>,
    executeThroughShell: Boolean,
    timeoutMs: Long,
  ): String {
    return executeShellCommand(command, parameters, shellEnv, executeThroughShell, timeoutMs)
      .readBytes()
      .toString(Charsets.UTF_8)
  }

  override fun executeShellCommandSync(
    command: String,
    parameters: List<String>,
    shellEnv: Map<String, String>,
    executeThroughShell: Boolean,
  ): String {
    return executeShellCommandSync(command, parameters, shellEnv, executeThroughShell, 0)
  }

  override fun executeShellCommand(
    command: String,
    parameters: List<String>,
    shellEnv: Map<String, String>,
    executeThroughShell: Boolean,
    timeoutMs: Long,
  ): InputStream {
    val commandLine = if (executeThroughShell) {
      listOf("sh", "-c", command) + parameters
    } else {
      listOf(command) + parameters
    }.joinToString(" ")
    // How to set the environment? The command is sent via an IUiAutomationConnection binder.
    // Ultimately it's calling java.lang.Runtime.exec(). Which *can* take an envp. That is, in turn,
    // using ProcessBuilder.
    val parcelFD = uiAutomation.executeShellCommand(commandLine)
    return ParcelFileDescriptor.AutoCloseInputStream(parcelFD)
  }

  override fun executeShellCommand(
    command: String,
    parameters: List<String>,
    shellEnv: Map<String, String>,
    executeThroughShell: Boolean,
  ): InputStream {
    return executeShellCommand(command, parameters, shellEnv, executeThroughShell, 0)
  }

  public companion object {
    @JvmField val BINDER_KEY = "UiAutomationShellExecutor"
  }
}
