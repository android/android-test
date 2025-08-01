package androidx.test.services.shellexecutor

import android.net.LocalSocket
import androidx.test.services.shellexecutor.LocalSocketProtocol.addressFromBinderKey
import androidx.test.services.shellexecutor.LocalSocketProtocol.hasExited
import androidx.test.services.shellexecutor.LocalSocketProtocol.readResponse
import androidx.test.services.shellexecutor.LocalSocketProtocol.secretFromBinderKey
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
      client.connect(addressFromBinderKey(server.binderKey()))
      client.sendRequest(
        secretFromBinderKey(server.binderKey()),
        listOf("echo", "\${POTRZEBIE}"),
        mapOf("POTRZEBIE" to "furshlugginer"),
        1000.milliseconds,
      )
      do {
        client.readResponse()?.let { responses.add(it) }
      } while (!responses.last().hasExited())
      server.stop(100.milliseconds)
    }
    // On rare occasions, the output of the command will come back in two packets! So to keep
    // this test from being 1% flaky:
    val stdout = buildString {
      for (response in responses) {
        if (response.buffer.size() > 0) append(response.buffer.toStringUtf8())
      }
    }
    assertThat(stdout).isEqualTo("\${POTRZEBIE}\n")
    assertThat(responses.last().hasExited()).isTrue()
    assertThat(responses.last().exitCode).isEqualTo(0)
  }

  @Test
  fun success_shell_expansion() {
    val responses = mutableListOf<RunCommandResponse>()
    runBlocking {
      val server = ShellCommandLocalSocketExecutorServer()
      server.start()
      val client = LocalSocket(LocalSocket.SOCKET_STREAM)
      client.connect(addressFromBinderKey(server.binderKey()))
      client.sendRequest(
        secretFromBinderKey(server.binderKey()),
        listOf("sh", "-c", "echo \${POTRZEBIE}"),
        mapOf("POTRZEBIE" to "furshlugginer"),
        1000.milliseconds,
      )
      do {
        client.readResponse()?.let { responses.add(it) }
      } while (!responses.last().hasExited())
      server.stop(100.milliseconds)
    }
    val stdout = buildString {
      for (response in responses) {
        if (response.buffer.size() > 0) append(response.buffer.toStringUtf8())
      }
    }
    assertThat(stdout).isEqualTo("furshlugginer\n")
    assertThat(responses.last().hasExited()).isTrue()
    assertThat(responses.last().exitCode).isEqualTo(0)
  }

  @Test
  fun failure_bad_secret() {
    runBlocking {
      val server = ShellCommandLocalSocketExecutorServer()
      server.start()
      val client = LocalSocket(LocalSocket.SOCKET_STREAM)
      client.connect(addressFromBinderKey(server.binderKey()))
      client.sendRequest(
        "potrzebie!",
        listOf("sh", "-c", "echo \${POTRZEBIE}"),
        mapOf("POTRZEBIE" to "furshlugginer"),
        1000.milliseconds,
      )
      assertThat(client.inputStream.read()).isEqualTo(-1)
    }
  }
}
