/*
 * Copyright (C) 2017 The Android Open Source Project
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
package androidx.test.rule.logging;

import android.app.Instrumentation;
import android.app.UiAutomation;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.test.annotation.Beta;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class contains helper methods to dump atrace info asynchronously while running the test case.
 *
 * <p><b>This API is currently in beta.</b>
 */
@Beta
public class AtraceLogger {

  private static final String ATRACE_START = "atrace --async_start -b %d -c %s";
  private static final String ATRACE_DUMP = "atrace --async_dump -b %d -z %s";
  private static final String ATRACE_STOP = "atrace --async_stop -b %d -z %s";
  private static final String ATRACEHELPER_TAG = "AtraceLogger";
  private static final String CATEGORY_SEPARATOR = " ";
  private static final int BUFFER_SIZE = 8192;
  private static volatile AtraceLogger loggerInstance;
  private UiAutomation uiAutomation;
  private String traceFileName;
  private List<ByteArrayOutputStream> atraceDataList;
  private Thread dumpThread;
  private File destAtraceDirectory;
  private boolean atraceRunning = false;
  private IOException dumpIOException;

  private AtraceLogger(Instrumentation instrumentation) {
    uiAutomation = instrumentation.getUiAutomation();
  }

  /**
   * To make sure only one instance of atrace logger is created. Note : Supported only for minsdk
   * version 23 and above because of UiAutomation executeShellCommand limitation.
   *
   * @param instrumentation Used to execute atrace shell commands
   * @return instance of the AtraceLogger
   */
  public static AtraceLogger getAtraceLoggerInstance(Instrumentation instrumentation) {
    if (loggerInstance == null) {
      synchronized (AtraceLogger.class) {
        if (loggerInstance == null) {
          loggerInstance = new AtraceLogger(instrumentation);
        }
      }
    }
    return loggerInstance;
  }

  /**
   * Method to start atrace and dump the data at regular interval. Note : Trace info will not be
   * captured during the dumping if there are multiple dumps between the atraceStart and atraceStop
   *
   * @param traceCategoriesSet Set of atrace categories (i.e atrace --list_categories)
   * @param atraceBufferSize Size of the circular buffer in kb
   * @param dumpIntervalSecs Periodic interval to dump data from circular buffer
   * @param destDirectory Directory under which atrace logs are stored
   * @param traceFileName is optional parameter.Atrace files are dumped under destDirectory.
   *     traceFileName will be indexed based on number of dumps between atraceStart and atraceStop
   *     under destDirectory. If traceFileName is null or empty "atrace" name will be used for
   *     indexing the files and stored under destDirectory
   * @throws IOException
   */
  public void atraceStart(
      Set<String> traceCategoriesSet,
      int atraceBufferSize,
      int dumpIntervalSecs,
      File destDirectory,
      String traceFileName)
      throws IOException {
    if (atraceRunning) {
      throw new IllegalStateException("Attempted multiple atrace start");
    }
    if (traceCategoriesSet.isEmpty()) {
      throw new IllegalArgumentException("Empty categories. Should contain atleast one category");
    }
    if (destDirectory == null) {
      throw new IllegalArgumentException("Destination directory cannot be null");
    }
    if (!destDirectory.exists() && !destDirectory.mkdirs()) {
      throw new IOException("Unable to create the destination directory");
    }
    destAtraceDirectory = destDirectory;

    StringBuffer traceCategoriesList = new StringBuffer();
    for (String traceCategory : traceCategoriesSet) {
      traceCategoriesList.append(traceCategory).append(CATEGORY_SEPARATOR);
    }
    if (traceFileName != null && !traceFileName.isEmpty()) {
      this.traceFileName = traceFileName;
    }

    String startCommand =
        String.format(ATRACE_START, atraceBufferSize, traceCategoriesList.toString());

    /*
     * Since execute shell command is not blocked until the command executes successfully,
     * write the output to byte array to make sure atrace start command is completed before
     * starting the test
     */
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    try {
      writeDataToByteStream(uiAutomation.executeShellCommand(startCommand), outStream);
    } finally {
      outStream.close();
    }
    atraceRunning = true;
    dumpIOException = null;
    atraceDataList = new ArrayList<ByteArrayOutputStream>();
    dumpThread =
        new Thread(
            new DumpTraceRunnable(
                traceCategoriesList.toString(), atraceBufferSize, dumpIntervalSecs));
    dumpThread.start();
  }

  /**
   * Method to write data into byte array
   *
   * @param pfDescriptor Used to read the content returned by shell command
   * @param outputStream Write the data to this output stream read from pfDescriptor
   * @throws IOException
   */
  private void writeDataToByteStream(
      ParcelFileDescriptor pfDescriptor, ByteArrayOutputStream outputStream) throws IOException {
    InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfDescriptor);
    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int length;
      while ((length = inputStream.read(buffer)) >= 0) {
        outputStream.write(buffer, 0, length);
      }
    } finally {
      inputStream.close();
    }
  }

  /**
   * Method to stop the atrace and write the atrace data cached in byte array list to file.
   *
   * @throws IOException
   * @throws InterruptedException
   */
  public void atraceStop() throws IOException, InterruptedException {
    if (!atraceRunning) {
      throw new IllegalStateException(
          "ATrace is not running currently. Start atrace before" + "stopping.");
    }
    try {
      dumpThread.interrupt();
      dumpThread.join();
      if (dumpIOException != null) {
        throw dumpIOException;
      }
      atraceWrite();
    } finally {
      for (ByteArrayOutputStream outStream : atraceDataList) {
        outStream.close();
      }
      atraceRunning = false;
      traceFileName = null;
    }
  }

  /**
   * Method to write atrace data buffered in byte array stream to file
   *
   * @throws IOException
   */
  private void atraceWrite() throws IOException {
    int count = 0;
    for (ByteArrayOutputStream outStream : atraceDataList) {
      // Indexing the files from 0..(atraceDataList.size)-1 based on number of dumps
      File file = null;
      if (traceFileName != null) {
        file =
            new File(destAtraceDirectory, String.format("%s-atrace-%d.txt", traceFileName, count));
      } else {
        file = new File(destAtraceDirectory, String.format("atrace-%d.txt", count));
      }
      OutputStream fileOutputStream = new FileOutputStream(file);
      try {
        fileOutputStream.write(outStream.toByteArray());
      } finally {
        fileOutputStream.close();
      }
      count++;
    }
  }

  /*
   * Thread class periodically dumps the atrace data into byte stream
   */
  private class DumpTraceRunnable implements Runnable {
    private String traceCategories;
    private int bufferSize;
    private int dumpIntervalInSecs;

    DumpTraceRunnable(String traceCategories, int bufferSize, int dumpIntervalInSecs) {
      this.traceCategories = traceCategories;
      this.bufferSize = bufferSize;
      this.dumpIntervalInSecs = dumpIntervalInSecs;
    }

    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          try {
            Thread.sleep(dumpIntervalInSecs * 1000);
          } catch (InterruptedException e) {
            break;
          }
          String dumpCommand = String.format(ATRACE_DUMP, bufferSize, traceCategories);
          // Dump into byte array and maintain in the list
          long startTime = System.currentTimeMillis();
          ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
          writeDataToByteStream(uiAutomation.executeShellCommand(dumpCommand), byteArrayOutStream);
          atraceDataList.add(byteArrayOutStream);
          long endTime = System.currentTimeMillis();
          Log.i(ATRACEHELPER_TAG, "Time taken by - DumpTraceRunnable " + (endTime - startTime));
        }
        String stopCommand = String.format(ATRACE_STOP, bufferSize, traceCategories);
        ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
        writeDataToByteStream(uiAutomation.executeShellCommand(stopCommand), byteArrayOutStream);
        atraceDataList.add(byteArrayOutStream);
      } catch (IOException ioe) {
        dumpIOException = ioe;
      }
    }
  }
}
