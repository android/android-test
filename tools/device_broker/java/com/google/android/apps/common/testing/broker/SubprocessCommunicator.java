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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Throwables.throwIfUnchecked;

import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ExecReporterAnnotation;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.ExecutorLocation;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SubprocessExecution;
import com.google.android.apps.common.testing.broker.DeviceBrokerAnnotations.SubprocessLogDir;
import com.google.android.apps.common.testing.broker.shell.BadExitStatusException;
import com.google.android.apps.common.testing.broker.shell.Command;
import com.google.android.apps.common.testing.broker.shell.CommandResult;
import com.google.android.apps.common.testing.broker.shell.SimpleKillableObserver;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharSource;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.inject.Inject;

/** Executes subprocesses and provides a way to send them input and process their output. */
public class SubprocessCommunicator {

  public static final String PACKAGE_NAMESPACE = "com.google.android.apps.common.testing.broker";
  public static final String EXEC_NAMESPACE = String.format("%s:%s", PACKAGE_NAMESPACE, "exec");

  private static final AtomicInteger SEQ_NUM = new AtomicInteger(0);
  private static final byte[] EMPTY_INPUT = new byte[0];
  private final ExecutorService executor;
  private final String executorLocation;
  private final String commandBasename;
  private final ImmutableMap<String, String> environment;
  private final long timeout;
  private final TimeUnit unit;
  private final LineProcessor<?> stdoutProcessor;
  private final LineProcessor<?> stderrProcessor;
  private final ExecReporter adbReporter;
  private File logFile;
  @Nullable private final String input;
  private final AtomicBoolean communicatorCalled = new AtomicBoolean(false);
  @VisibleForTesting
  final ImmutableList<String> arguments;


  private SubprocessCommunicator(Builder builder) {
    arguments = ImmutableList.copyOf(checkNotNull(builder.arguments));
    environment = ImmutableMap.copyOf(checkNotNull(builder.environment));
    executor = checkNotNull(builder.executor);
    executorLocation = checkNotNull(builder.executorLocation);
    commandBasename = checkNotNull(builder.commandBasename);
    timeout = builder.timeout;
    checkArgument(0 < timeout, "timeout invalid: %s", timeout);
    unit = checkNotNull(builder.unit);

    input = builder.input;
    stdoutProcessor = builder.stdoutProcessor;
    stderrProcessor = builder.stderrProcessor;
    logFile = builder.logFile;
    adbReporter = checkNotNull(builder.adbReporter);
  }

  /**
   * Spawns a process and communicates with it.
   *
   * 1. Spawn the subprocess
   * 2. Write any input to its stream and flush it.
   * 3. Close the input stream.
   * 4. Read the stderr / stdout streams into memory if their are processors.
   * 5. Wait for the subprocess
   * 6. Process the stdout/stderr thru the callers line processors.
   * 7. return the subprocess exit code.
   *
   * Because lineprocessors are stateful, it only makes sense to allow the communicator to be called
   * once.
   *
   * @return the process exit code
   * @throws IllegalStateException when called more then once
   * @throws RuntimeException when process runs passed timeout or when it is interrupted.
   */
  public int communicate() {
    checkState(communicatorCalled.compareAndSet(false, true), "Already called!");
    List<String> executorArgs = Lists.newArrayList(executorLocation, logFile.getPath());
    executorArgs.addAll(arguments);

    Command command =
        new Command(
            executorArgs.toArray(new String[0]), environment, null /* use current working dir*/);

    Stopwatch startTime = Stopwatch.createStarted();
    int returnCode = innerCommunicate(command);
    long elapsed = startTime.elapsed(TimeUnit.MILLISECONDS);
    boolean success = true;
    try {
      Files.asCharSink(logFile, Charsets.UTF_8, FileWriteMode.APPEND)
          .write(String.format("EXIT CODE: %s\n", returnCode));
      // Also encode exit status in log file name to help debugging.
      String newSuffix = "ok.txt";
      if (returnCode != 0) {
        success = false;
        newSuffix = "fail.txt";
      }
      File newName = new File(logFile.getPath().replaceAll("txt$", newSuffix));
      if (!logFile.getPath().equals(newName.getPath())) {
        if (logFile.renameTo(newName)) {
          logFile = newName;
        }
      }
    } catch (IOException ignore) {
      /* lumber on and let the caller handle the return */
    } finally {
      adbReporter.report(
          String.format("%s:%s", EXEC_NAMESPACE, commandBasename),
          Joiner.on(" ").join(arguments),
          elapsed,
          success);
    }
    return returnCode;
  }

  private int innerCommunicate(final Command process) {


    final ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();
    final SimpleKillableObserver observer = new SimpleKillableObserver();

    Callable<Integer> waiter = new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        byte[] commandInput = EMPTY_INPUT;
        if (null != input) {
          commandInput = input.getBytes();
        }
        try {
          CommandResult result = process.execute(
              commandInput,
              observer,
              stdoutStream,
              stderrStream,
              true); /* kill subprocess if we're interrupted */


          return result.getTerminationStatus().getExitCode();
        } catch (BadExitStatusException bese) {
          // don't care some of our commands are whacky in that non-zero is okay.
          return bese.getResult().getTerminationStatus().getExitCode();
        }
      }
    };

    int exitCode = 0;
    try {
      // start the wait on a seperate thread.
      // This work should be done on a seperate thread because we also must
      // quickly consume all the output and send all the input or we may
      // deadlock.
      exitCode = executor.submit(waiter).get(timeout, unit);
    } catch (TimeoutException te) {
      observer.kill();
      throw new RuntimeException(te);
    } catch (InterruptedException ie) {
      observer.kill();
      throw new RuntimeException(ie);
    } catch (ExecutionException ee) {
      observer.kill();
      throwIfUnchecked(ee.getCause());
      throw new RuntimeException(ee.getCause());
    }

    try {
      CharSource.wrap(new String(stdoutStream.toByteArray(), Charsets.UTF_8))
          .readLines(stdoutProcessor);
      CharSource.wrap(new String(stderrStream.toByteArray(), Charsets.UTF_8))
          .readLines(stderrProcessor);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }

    return exitCode;
  }

  public InputStream getLog() throws IOException {
    return new FileInputStream(logFile);
  }

  /** Builder for [SubprocessCommunicator]. */
  public static class Builder {
    private final ExecutorService executor;
    private final File logDir;
    private final String executorLocation;
    private final ExecReporter adbReporter;
    private List<String> arguments;
    private Map<String, String> environment = Maps.newHashMap();
    private long timeout = 60;
    private TimeUnit unit = TimeUnit.SECONDS;
    private LineProcessor<?> stdoutProcessor = new NullProcessor<Object>();
    private LineProcessor<?> stderrProcessor = new NullProcessor<Object>();
    private String input;
    private File logFile;
    private String commandBasename;

    @Inject
    public Builder(
        @SubprocessExecution ExecutorService executor,
        @SubprocessLogDir File logDir,
        @ExecutorLocation String executorLocation,
        @ExecReporterAnnotation ExecReporter adbReporter) {
      this.executor = executor;
      this.logDir = logDir;
      this.executorLocation = executorLocation;
      this.adbReporter = adbReporter;
    }

    public Builder withArguments(List<String> arguments) {
      this.arguments = Lists.newArrayList(arguments);
      return this;
    }

    public Builder withEnvironment(Map<String, String> environment) {
      this.environment = Maps.newHashMap(environment);
      return this;
    }

    public Builder withTimeout(long time, TimeUnit unit) {
      this.timeout = time;
      this.unit = unit;
      return this;
    }

    public Builder withInput(String input) {
      this.input = input;
      return this;
    }

    public Builder withStdoutProcessor(LineProcessor<?> stdoutProcessor) {
      this.stdoutProcessor = stdoutProcessor;
      return this;
    }

    public Builder withStderrProcessor(LineProcessor<?> stderrProcessor) {
      this.stderrProcessor = stderrProcessor;
      return this;
    }

    public SubprocessCommunicator build() {
      commandBasename = new File(arguments.get(0)).getName();
      String subCommand = ".";
      if ((commandBasename.equals("adb.turbo") || commandBasename.equals("waterfall_bin"))
          && arguments.get(1).equals("-s")) {
        // It should be something like adb.turbo -s localhost:123 shell ls
        subCommand = arguments.get(3);
        if (subCommand.equals("shell")) {
          String cmd = arguments.get(4).split(" ")[0];
          subCommand = new File(cmd).getName();
        }
        subCommand = "." + subCommand + ".";
      }
      logFile = new File(logDir, String.format("%s.%d%stxt", commandBasename,
          SEQ_NUM.incrementAndGet(), subCommand));

      try {
        logFile.createNewFile();
      } catch (IOException ioe) {
        throw new RuntimeException("Couldnt create logfile: " + logFile, ioe);
      }

      return new SubprocessCommunicator(this);
    }
  }

  private static class NullProcessor<ObjectT> implements LineProcessor<ObjectT> {

    @Override
    public boolean processLine(String in) {
      return true;
    }

    @Override
    public ObjectT getResult() {
      return null;
    }
  }

}
