/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.services.storage.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.test.services.storage.TestStorageConstants
import androidx.test.services.storage.TestStorageServiceProto.TestArguments
import com.google.testing.platform.android.core.orchestration.strategy.GrpcDiagnosticsOrchestrationStrategy
import com.google.testing.platform.core.telemetry.android.opencensus.exporter.SpanDataWrapper
import com.google.testing.platform.core.telemetry.opencensus.TraceProtoUtils
import com.google.testing.platform.lib.coroutines.scope.JobScope
import com.google.testing.platform.proto.api.android.DiagnosticEventProto
import io.grpc.android.AndroidChannelBuilder
import io.opencensus.trace.export.SpanData
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.ObjectInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

/**
 * Enables streaming of diagnostics events by different applications on the device to a GRPC
 * server. Diagnostic events from different android applications emitted by [https://opencensus.io/]
 * use the [TestDiagnosticsContentProvider] as a single entry point to stream these messages off
 * the device.
 *
 * This provider supports only the insert api. Use [SpanDataWrapper] to bundle your [SpanData]
 * into [ContentValues] and insert it by calling
 * [contentResolver#insert(CONTENT_URI, contentValues)].
 */
class TestDiagnosticsContentProvider : ContentProvider() {

  private var serverPort: Lazy<String> = lazy { getServerPortFromTestArgs().toString() }
  private lateinit var grpcDiagnosticsOrchestrationStrategy: GrpcDiagnosticsOrchestrationStrategy
  private lateinit var serverJob: Job

  companion object {
    private const val INVALID_SERVER_PORT = -1
    private const val PORT_ZERO = 0
    private const val DEFAULT_SERVER_PORT = 64676
    private const val TAG = "DiagnosticsCP"
    private const val DIAGNOSTIC_SERVER_PORT_ARG = "diagnosticsServerPort"

    /**
     * Reads the [DIAGNOSTIC_SERVER_PORT_ARG] from the [TestArguments] proto present in the device
     * and returns the appropriate port.
     *
     * @return [DEFAULT_SERVER_PORT] if [PORT_ZERO] is found in [TestArguments], if
     * [DIAGNOSTIC_SERVER_PORT_ARG] is not set we return an [INVALID_SERVER_PORT].
     */
    private fun getServerPortFromTestArgs(): Int {
      val testArgsFile = File(
        Environment.getExternalStorageDirectory(),
        TestStorageConstants.ON_DEVICE_PATH_INTERNAL_USE + TestStorageConstants.TEST_ARGS_FILE_NAME
      )
      if (!testArgsFile.exists()) {
        return INVALID_SERVER_PORT
      }
      try {
        val testArgs = TestArguments.parseFrom(FileInputStream(testArgsFile))

        var serverPort: String? = testArgs.argList.find {
          it.name == DIAGNOSTIC_SERVER_PORT_ARG
        }?.value

        // If we receive [PORT_ZERO], use the invalid port instead
        serverPort?.let {
          if (it == PORT_ZERO.toString()) {
            INVALID_SERVER_PORT.toString()
          } else {
            serverPort
          }
        }

        // If we receive no port, set it to the default port
        serverPort = serverPort ?: DEFAULT_SERVER_PORT.toString()
        return serverPort.toInt()
      } catch (e: IOException) {
        throw RuntimeException("Not able to read from file: " + testArgsFile.name, e)
      }
    }
  }

  override fun onCreate(): Boolean {
    return false
  }

  override fun query(
    uri: Uri,
    strings: Array<String>?,
    s: String?,
    strings1: Array<String>?,
    s1: String?
  ): Cursor? {
    return null
  }

  override fun getType(uri: Uri): String? {
    return null
  }

  /**
   * On receiving an `insert` request the content provider connects to the GRPC server
   * using the [serverPort]. The [Span] proto is built from [SpanData] obtained from
   * [ContentValues].
   */
  override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
    if (contentValues!!.containsKey("FINISH")) {
      if (::grpcDiagnosticsOrchestrationStrategy.isInitialized) {
        grpcDiagnosticsOrchestrationStrategy.diagnosticsEvents.close()
        runBlocking { serverJob.join() }
      }
      Log.i(TAG, "GRPC Channel closed by TestDiagnosticsContentProvider")
      return null
    }

    ByteArrayInputStream(contentValues!!.getAsByteArray("span")).use {
      ObjectInputStream(it).use { ois ->
        val receivedSpans = SpanDataWrapper.readObject(ois)
        Log.i(TAG, "Received diagnostics events")

        if (!serverPort.isInitialized()) {
          try {
            connectToAtpServer()
          } catch (e: Exception) {
            Log.w(TAG, "Connecting to the diagnostics service resulted in an error: $e")
            return null
          }
        }
        sendDiagnosticsEvents(receivedSpans)
      }
    }
    return null
  }

  override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
    return 0
  }

  override fun update(
    uri: Uri,
    contentValues: ContentValues?,
    s: String?,
    strings: Array<String>?
  ): Int {
    return 0
  }

  /**
   * Connects to the GRPC server running the diagnostics service.
   */
  private fun connectToAtpServer() {
    if (serverPort.value.toInt() == INVALID_SERVER_PORT) {
      Log.i(TAG, "Invalid server port, will not connect to the diagnostics server!")
      return
    }
    grpcDiagnosticsOrchestrationStrategy = GrpcDiagnosticsOrchestrationStrategy(
      JobScope(Dispatchers.Default)
    ) { target ->
      AndroidChannelBuilder.forTarget(target).context(context)
    }
    serverJob = grpcDiagnosticsOrchestrationStrategy.start(serverPort.value.toInt())
    Log.i(TAG, "Connected to server on port : ${serverPort.value.toInt()}")
  }

  /**
   * Sends the diagnostics event to the GRPC service.
   */
  private fun sendDiagnosticsEvents(spans: List<SpanData>) {
    if (serverPort.value.toInt() == INVALID_SERVER_PORT) {
      Log.i(TAG, "Invalid server port, dropping diagnostic event!")
      return
    }
    val diagnosticsEvent = DiagnosticEventProto.DiagnosticEvent.newBuilder().addAllSpans(
      spans.map { TraceProtoUtils.toSpanProto(it) }
    ).build()

    try {
      runBlocking {
        grpcDiagnosticsOrchestrationStrategy.diagnosticsEvents.send(diagnosticsEvent)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Sending events to the diagnostics service resulted in an error: $e")
    }
  }
}
