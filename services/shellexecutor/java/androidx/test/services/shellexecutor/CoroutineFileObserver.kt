/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.services.shellexecutor

import android.os.Build
import android.os.FileObserver
import android.util.Log
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking

/**
 * A FileObserver that is friendly with Kotlin coroutines.
 *
 * Note that the documentation on FileObserver is wrong: it doesn't see events from subdirectories.
 */
@Suppress("DEPRECATION") // the non-deprecated constructor needs API 29
open class CoroutineFileObserver(public val watch: File) :
  FileObserver(watch.toString(), FileObserver.ALL_EVENTS) {
  private data class Event(val event: Int, val file: File)

  private val eventChannel: EventChannel
  protected var logLevel: Int = 0 // by default, don't log at all; derived classes can override
  protected var logTag = "CoroutineFileObserver"

  init {
    // On API 21 and 22, about 1% of the time, Channel code will deadlock and, thirty seconds later,
    // crash the application with 'art/runtime/thread_list.cc:170] Thread suspend timeout'. In that
    // case, we resort to Java.
    if (
      Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ||
        Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    ) {
      eventChannel = CoroutineEventChannel()
    } else {
      eventChannel = WorkaroundEventChannel()
    }
  }

  // This runs on a special FileObserver thread provided by Android.
  final override fun onEvent(event: Int, path: String?) {
    val file =
      when {
        path == null -> watch
        path.startsWith("/") -> File(path)
        else -> File(watch, path)
      }
    eventChannel.send(Event(event, file))
  }

  final fun stop(): Unit = eventChannel.stop()

  // Events are processed in order by run(). If you do nontrivial work in one of the handlers,
  // launch it in another job.
  final suspend fun run() {
    startWatching()
    try {
      onWatching()
      eventChannel.receive { event -> handleEvent(event) }
    } catch (x: Exception) {
      Log.w(logTag, "Error while processing events", x)
    } finally {
      log("stopWatching")
      stopWatching()
      log("stoppedWatching")
    }
  }

  private suspend fun handleEvent(event: Event) =
    when (event.event) {
      FileObserver.ACCESS -> onAccess(event.file)
      FileObserver.ATTRIB -> onAttrib(event.file)
      FileObserver.CLOSE_NOWRITE -> onCloseNoWrite(event.file)
      FileObserver.CLOSE_WRITE -> onCloseWrite(event.file)
      FileObserver.CREATE -> onCreate(event.file)
      FileObserver.DELETE -> onDelete(event.file)
      FileObserver.DELETE_SELF -> onDeleteSelf(event.file)
      FileObserver.MODIFY -> onModify(event.file)
      FileObserver.MOVED_FROM -> onMovedFrom(event.file)
      FileObserver.MOVED_TO -> onMovedTo(event.file)
      FileObserver.MOVE_SELF -> onMoveSelf(event.file)
      FileObserver.OPEN -> onOpen(event.file)
      else -> Unit
    }

  protected final fun log(message: String): Unit {
    if (logLevel >= Log.VERBOSE) Log.println(logLevel, logTag, message)
  }

  protected open suspend fun onAccess(file: File) = log("ACCESS $file")

  protected open suspend fun onAttrib(file: File) = log("ATTRIB $file")

  protected open suspend fun onCloseNoWrite(file: File) = log("CLOSE_NOWRITE $file")

  protected open suspend fun onCloseWrite(file: File) = log("CLOSE_WRITE $file")

  protected open suspend fun onCreate(file: File) = log("CREATE $file")

  protected open suspend fun onDelete(file: File) = log("DELETE $file")

  protected open suspend fun onDeleteSelf(file: File) = log("DELETE_SELF $file")

  protected open suspend fun onModify(file: File) = log("MODIFY $file")

  protected open suspend fun onMovedFrom(file: File) = log("MOVED_FROM $file")

  protected open suspend fun onMovedTo(file: File) = log("MOVED_TO $file")

  protected open suspend fun onMoveSelf(file: File) = log("MOVE_SELF $file")

  protected open suspend fun onOpen(file: File) = log("OPEN $file")

  /** Called in run() after startWatching(). Override as needed. */
  protected open fun onWatching() = Unit

  private companion object {
    private interface EventChannel {
      /** Send one event. */
      fun send(event: Event)

      /** Receive events until stop() is called. */
      suspend fun receive(handler: suspend (Event) -> Unit)

      /** Stops the channel. */
      fun stop()
    }

    private class CoroutineEventChannel : EventChannel {
      private val channel: Channel<Event> = Channel(Channel.UNLIMITED)

      override fun send(event: Event) {
        if (channel.isClosedForSend) return
        runBlocking {
          try {
            channel.send(event)
          } catch (x: ClosedSendChannelException) {
            // Just in case the channel was closed after the previous call
          }
        }
      }

      override suspend fun receive(handler: suspend (Event) -> Unit) {
        channel.receiveAsFlow().collect(handler)
      }

      override fun stop() {
        channel.close()
      }
    }

    private class WorkaroundEventChannel : EventChannel {
      private val queue = LinkedBlockingQueue<Event>()

      override fun send(event: Event) {
        queue.put(event)
      }

      override suspend fun receive(handler: suspend (Event) -> Unit) {
        while (true) {
          val event = queue.take()
          if (event.event < 0) return
          handler(event)
        }
      }

      override fun stop() {
        send(Event(-1, File(".")))
      }
    }
  }
}
