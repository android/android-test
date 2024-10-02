package androidx.test.services.shellexecutor

import android.net.LocalSocket
import android.os.Build
import androidx.test.services.shellexecutor.LocalSocketProtocol.fromBinderKey
import androidx.test.services.shellexecutor.LocalSocketProtocol.readResponse
import androidx.test.services.shellexecutor.LocalSocketProtocol.sendRequest
import androidx.test.services.shellexecutor.LocalSocketProtocolProto.RunCommandResponse
import com.google.common.truth.Truth.assertThat
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ShellCommandLocalSocketExecutorServerTest {

  @Test
  fun success_simple() {
    val responses = mutableListOf<RunCommandResponse>()
    runBlocking {
      val server = ShellCommandLocalSocketExecutorServer()
      server.start()
      val client = LocalSocket(LocalSocket.SOCKET_STREAM)
      client.connect(fromBinderKey(server.binderKey()))
      client.sendRequest(
        listOf("echo", "\${POTRZEBIE}"),
        mapOf("POTRZEBIE" to "furshlugginer"),
        1000.milliseconds,
      )
      do {
        client.readResponse()?.let { responses.add(it) }
      } while (!responses.last().hasExitCode())
      server.stop(100.milliseconds)
    }
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
      // On API 21 and 22, echo only exists as a shell builtin!
      assertThat(responses).hasSize(1)
      assertThat(responses[0].exitCode).isEqualTo(-1)
      assertThat(responses[0].buffer.toStringUtf8()).contains("Permission denied")
    } else {
      // On rare occasions, the output of the command will come back in two packets! So to keep
      // this test from being 1% flaky:
      val stdout = buildString {
        for (response in responses) {
          if (response.hasBuffer()) append(response.buffer.toStringUtf8())
        }
      }
      assertThat(stdout).isEqualTo("\${POTRZEBIE}\n")
      assertThat(responses.last().hasExitCode()).isTrue()
      assertThat(responses.last().exitCode).isEqualTo(0)
    }
  }

  @Test
  fun success_shell_expansion() {
    val responses = mutableListOf<RunCommandResponse>()
    runBlocking {
      val server = ShellCommandLocalSocketExecutorServer()
      server.start()
      val client = LocalSocket(LocalSocket.SOCKET_STREAM)
      client.connect(fromBinderKey(server.binderKey()))
      client.sendRequest(
        listOf("sh", "-c", "echo \${POTRZEBIE}"),
        mapOf("POTRZEBIE" to "furshlugginer"),
        1000.milliseconds,
      )
      do {
        client.readResponse()?.let { responses.add(it) }
      } while (!responses.last().hasExitCode())
      server.stop(100.milliseconds)
    }
    val stdout = buildString {
      for (response in responses) {
        if (response.hasBuffer()) append(response.buffer.toStringUtf8())
      }
    }
    assertThat(stdout).isEqualTo("furshlugginer\n")
    assertThat(responses.last().hasExitCode()).isTrue()
    assertThat(responses.last().exitCode).isEqualTo(0)
  }
}
