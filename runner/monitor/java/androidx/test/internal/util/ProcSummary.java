/*
 * Copyright (C) 2018 The Android Open Source Project
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
package androidx.test.internal.util;

import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.annotation.VisibleForTesting;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/** Represents summarized process information. */
public final class ProcSummary {
  public final String name;
  public final String pid;
  public final String parent;
  public final String realUid;
  public final String cmdline;
  public final long startTime;

  private ProcSummary(Builder b) {
    this.name = checkNotNull(b.name);
    this.pid = checkNotNull(b.pid);
    this.realUid = checkNotNull(b.realUid);
    this.parent = checkNotNull(b.parent);
    this.cmdline = checkNotNull(b.cmdline);
    this.startTime = b.startTime;
  }

  /**
   * Uses data in /proc to summarize a process.
   *
   * @param pid the process directory name under /proc
   * @return a ProcSummary
   * @throws SummaryException if a summary can't be generated.
   */
  public static ProcSummary summarize(String pid) {
    // /proc/$PID/stat has pid, command name, parent, start time
    File statFile = new File(new File("/proc", pid), "stat");
    String statContent = readToString(statFile);

    // /proc/$PID/status has Uid
    File statusFile = new File(new File("/proc", pid), "status");
    String statusContent = readToString(statusFile);

    // /proc/$PID/cmdline has the packagename.
    File cmdlineFile = new File(new File("/proc", pid), "cmdline");
    String cmdline = readToString(cmdlineFile);

    return parse(statContent, statusContent, cmdline);
  }

  private static final String readToString(File path) {
    StringBuilder sb = new StringBuilder();
    char[] buff = new char[1024];
    InputStreamReader isr = null;
    try {
      isr = new InputStreamReader(new FileInputStream(path));
      int read = 0;

      while ((read = isr.read(buff, 0, buff.length)) != -1) {
        sb.append(buff, 0, read);
      }
      return sb.toString();
    } catch (RuntimeException re) {
      throw new SummaryException("Error reading: " + path, re);
    } catch (IOException ioe) {
      throw new SummaryException("Could not read: " + path, ioe);
    } finally {
      if (null != isr) {
        try {
          isr.close();
        } catch (IOException ignored) {
          // ignore.
        }
      }
    }
  }

  /** Thrown when we just cant figure out a process :) */
  public static class SummaryException extends RuntimeException {
    public SummaryException(String msg, Throwable cause) {
      super(msg, cause);
    }

    public SummaryException(String msg) {
      super(msg);
    }
  }

  @VisibleForTesting
  static ProcSummary parse(String statLine, String statusContent, String cmdline) {
    // See man proc(5)
    String[] stats = statLine.substring(statLine.lastIndexOf(')') + 2).split(" ", -1);

    statusContent = statusContent.substring(statusContent.indexOf("\nUid:") + 1);
    statusContent = statusContent.substring(0, statusContent.indexOf('\n'));
    String[] uids = statusContent.split("\\s", -1);

    ProcSummary.Builder b =
        new ProcSummary.Builder()
            .withPid(statLine.substring(0, statLine.indexOf(' ')))
            .withName(statLine.substring(statLine.indexOf('(') + 1, statLine.lastIndexOf(')')))
            .withParent(stats[1])
            .withRealUid(uids[1])
            .withCmdline(cmdline.trim().replace('\0', ' '))
            .withStartTime(Long.parseLong(stats[19]));
    return b.build();
  }

  @VisibleForTesting
  static class Builder {
    private String name;
    private String pid;
    private String realUid;
    private String cmdline;
    private long startTime;
    private String parent;

    Builder withParent(String ppid) {
      try {
        Integer.parseInt(ppid);
      } catch (NumberFormatException nfe) {
        throw new IllegalArgumentException("not a pid: " + ppid);
      }
      this.parent = ppid;
      return this;
    }

    Builder withCmdline(String cmdline) {
      this.cmdline = cmdline;
      return this;
    }

    Builder withName(String name) {
      this.name = name;
      return this;
    }

    Builder withPid(String pid) {
      try {
        Integer.parseInt(pid);
      } catch (NumberFormatException nfe) {
        throw new IllegalArgumentException("not a pid: " + pid);
      }
      this.pid = pid;
      return this;
    }

    Builder withRealUid(String uid) {
      try {
        Integer.parseInt(uid);
      } catch (NumberFormatException nfe) {
        throw new IllegalArgumentException("not a uid: " + uid);
      }
      this.realUid = uid;
      return this;
    }

    Builder withStartTime(long startTime) {
      this.startTime = startTime;
      return this;
    }

    ProcSummary build() {
      return new ProcSummary(this);
    }
  }

  @Override
  public String toString() {
    return String.format(
        "ProcSummary("
            + "name: '%s', "
            + "cmdline: '%s', "
            + "pid: '%s', "
            + "parent: '%s', "
            + "realUid: '%s', "
            + "startTime: %d)",
        name, cmdline, pid, parent, realUid, startTime);
  }

  @Override
  public int hashCode() {
    return pid.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof ProcSummary)) {
      return false;
    }
    ProcSummary ops = (ProcSummary) o;
    return ops.name.equals(name)
        && ops.pid.equals(pid)
        && ops.parent.equals(parent)
        && ops.realUid.equals(realUid)
        && ops.cmdline.equals(cmdline)
        && ops.startTime == startTime;
  }
}
