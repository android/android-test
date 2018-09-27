/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.common.testing.broker;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Closeables.close;
import static com.google.common.io.Closeables.closeQuietly;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Starts logcat and streams its output to a file.
 *
 */
public class LogcatStreamer {
  private final List<String> adbAndDevicePrefix;
  private final File outputFile;
  private final OutputFormat outputFormat;
  private final List<Buffer> buffer;
  private final List<LogcatFilter> logcatFilters;
  private final Map<String, String> adbEnvironment;
  private AtomicBoolean alreadyExecuted = new AtomicBoolean(false);
  private Process proc;
  private int scriptPid = -1;

  /**
   * The format to stream logcat data in for the test run.
   */
  public static enum OutputFormat { BRIEF, PROCESS, TAG, RAW, TIME, THREADTIME, LONG }

  /** The buffer to dump during the test run */
  public static enum Buffer {
    MAIN,
    EVENTS,
    RADIO,
    ALL,
    CRASH,
    SYSTEM
  }

  LogcatStreamer(
      List<String> adbAndDevicePrefix,
      Buffer buffer,
      OutputFormat outputFormat,
      List<LogcatFilter> logcatFilters,
      File outputFile,
      Map<String, String> adbEnvironment) {
    this(
        adbAndDevicePrefix,
        Collections.singletonList(buffer),
        outputFormat,
        logcatFilters,
        outputFile,
        adbEnvironment);
  }

  LogcatStreamer(
      List<String> adbAndDevicePrefix,
      List<Buffer> buffer,
      OutputFormat outputFormat,
      List<LogcatFilter> logcatFilters,
      File outputFile,
      Map<String, String> adbEnvironment) {
    this.adbAndDevicePrefix = checkNotNull(adbAndDevicePrefix);
    this.outputFile = checkNotNull(outputFile);
    this.outputFormat = checkNotNull(outputFormat);
    this.buffer = checkNotNull(buffer);
    this.logcatFilters = checkNotNull(logcatFilters);
    this.adbEnvironment = checkNotNull(adbEnvironment);
  }

  public void startStream() {
    checkState(alreadyExecuted.compareAndSet(false, true), "Streamer already used");
    List<String> logcatCommand = Lists.newArrayList();
    logcatCommand.addAll(adbAndDevicePrefix);
    logcatCommand.add("logcat");
    logcatCommand.add("-v");
    logcatCommand.add(outputFormat.toString().toLowerCase());
    logcatCommand.add("-b");
    logcatCommand.add(Joiner.on(',').join(buffer).toLowerCase());
    for (LogcatFilter filter : logcatFilters) {
      logcatCommand.add(filter.toString());
    }

    logcatCommand = Lists.newArrayList(
        "/bin/sh",
        "-c",
        "echo $$; exec </dev/null; exec >" + outputFile.getPath() + " 2>/dev/null; " +
             Joiner.on(" ").join(logcatCommand));
    ProcessBuilder procBuilder = new ProcessBuilder()
        .command(logcatCommand)
        .redirectErrorStream(true);

    procBuilder.environment().clear();
    procBuilder.environment().putAll(adbEnvironment);
    try {
      proc = procBuilder.start();
    } catch (IOException ioe) {
      throw new RuntimeException(String.format("Couldnt start logcat (args: %s)", logcatCommand),
          ioe);
    }
    Scanner inputScanner = new Scanner(proc.getInputStream());
    scriptPid = inputScanner.nextInt();

    try {
      close(proc.getOutputStream(), true);
    } catch (IOException ioe) {
      throw new RuntimeException("Unable to close output stream", ioe);
    }
    closeQuietly(proc.getErrorStream());
    inputScanner.close();
  }

  public void stopStream() {
    checkNotNull(proc, "Stream process not running!");

    try {
      Process killer = new ProcessBuilder(
          Lists.newArrayList("/usr/bin/pkill", "-P", String.valueOf(scriptPid))).start();
      close(killer.getOutputStream(), true);
      closeQuietly(killer.getErrorStream());
      closeQuietly(killer.getInputStream());
      killer.waitFor();
    } catch (IOException ioe) {
      throw new RuntimeException("Unable to invoke pkill", ioe);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("pkill interrupted.", ie);
    }

    proc.destroy();
    try {
      proc.waitFor();
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(ie);
    } finally {
      proc = null;
    }
  }

  @VisibleForTesting
  public int waitFor() throws InterruptedException {
    return proc.waitFor();
  }

}
