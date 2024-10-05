package androidx.test.services.shellexecutor

import android.net.LocalServerSocket
import android.net.LocalSocketAddress
import androidx.test.services.shellexecutor.LocalSocketProtocol.asBinderKey
import androidx.test.services.shellexecutor.LocalSocketProtocol.fromBinderKey
import androidx.test.services.shellexecutor.LocalSocketProtocol.readRequest
import androidx.test.services.shellexecutor.LocalSocketProtocolProto.RunCommandRequest
import com.google.common.truth.Truth.assertThat
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ShellCommandLocalSocketClientTest {

  @Before fun setUp() {}

  @Test
  fun binderkey_success() {
    val address = LocalSocketAddress("binderkey_success 12345")
    assertThat(fromBinderKey(address.asBinderKey()).name).isEqualTo(address.name)
    assertThat(fromBinderKey(address.asBinderKey()).namespace).isEqualTo(address.namespace)
  }

  @Test
  fun request_regular() {
    val server = LocalServerSocket("request_regular")
    val client = ShellCommandLocalSocketClient(server.localSocketAddress)

    val request: RunCommandRequest

    runBlocking {
      val result = async {
        val socket = server.accept()
        socket.readRequest()
      }

      client.request(
        "foo",
        listOf("bar", "baz"),
        mapOf("quem" to "quux", "potrzebie" to "furshlugginer"),
        executeThroughShell = false,
        timeout = 1.seconds,
      )
      request = result.await()
    }

    assertThat(request.argvList).containsExactly("foo", "bar", "baz")
    assertThat(request.environmentMap)
      .containsExactlyEntriesIn(mapOf("quem" to "quux", "potrzebie" to "furshlugginer"))
    // The overall timeout will have the connect time shaved off.
    assertThat(request.timeoutMs).isGreaterThan(950)
  }

  @Test
  fun request_executeThroughShell() {
    val server = LocalServerSocket("request_executeThroughShell")
    val client = ShellCommandLocalSocketClient(server.localSocketAddress)

    val request: RunCommandRequest

    runBlocking {
      val result = async {
        val socket = server.accept()
        socket.readRequest()
      }

      client.request(
        "foo",
        listOf("bar", "baz"),
        mapOf("quem" to "quux", "potrzebie" to "furshlugginer"),
        executeThroughShell = true,
        timeout = 1.seconds,
      )
      request = result.await()
    }

    assertThat(request.argvList).containsExactly("sh", "-c", "foo bar baz")
    assertThat(request.environmentMap)
      .containsExactlyEntriesIn(mapOf("quem" to "quux", "potrzebie" to "furshlugginer"))
    // The overall timeout will have the connect time shaved off.
    assertThat(request.timeoutMs).isGreaterThan(950)
  }
}
