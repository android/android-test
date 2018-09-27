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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.UnmodifiableIterator;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.annotation.concurrent.GuardedBy;

/**
 * A port manager provides tests with unique open ports within a certain range.
 *
 * <p>These ports can be used to start tcp / udp servers.
 *
 * <p>The port manager assures the port is not in use and once it gives a caller a port it'll never
 * give that port to another caller.
 */
public class PortManager {

  private static final Random SECURE_RANDOM = new SecureRandom();

  private final InUseChecker inUseChecker = new InUseChecker();
  private final Random random;
  private final ContiguousSet<Integer> portSet;
  private final Collection<PortChecker> checkers;
  private final PortChecker clientConnectChecker;

  interface PortChecker {
    /** Determines if a given port is free for a protocol. */
    boolean isPortFree(int port);
  }

  enum SocketType implements PortChecker {
    TCP {
      @Override
      public boolean isPortFree(int port) {
        if (port <= 0) {
          return false;
        }
        try {
          ServerSocket socket = new ServerSocket(port);
          socket.close();
          return true;
        } catch (IOException ioe) {
          return false;
        }
      }
    },
    UDP {
      @Override
      public boolean isPortFree(int port) {
        if (port <= 0) {
          return false;
        }
        try {
          DatagramSocket udpSocket = new DatagramSocket(port);
          udpSocket.close();
          return true;
        } catch (IOException ioe) {
          return false;
        }
      }
    }
  };

  /** Used to ensure the dummy serversocket a TCP.isPortFree call makes has been shutdown. */
  static class ClientConnectChecker implements PortChecker {
    @Override
    public boolean isPortFree(int port) {
      try {
        Socket socket = new Socket("localhost", port);
        socket.close();
        return false;
      } catch (IOException closed) {
        return true;
      }
    }
  }

  /** Ensures that a PortManager will never pass out the same port twice. */
  static class InUseChecker implements PortChecker {
    private final Object inUseLock = new Object();

    @GuardedBy("inUseLock")
    private final Set<Integer> inUse = new HashSet<>();

    @Override
    public boolean isPortFree(int port) {
      synchronized (inUseLock) {
        return !inUse.contains(port);
      }
    }

    public boolean claim(int port) {
      synchronized (inUseLock) {
        return inUse.add(port);
      }
    }
  }

  /** Serves ports in the range of 32768 and 60000. */
  public PortManager() {
    this(Range.closed(32768, 60000));
  }

  /** Serves ports in the user specified range. */
  public PortManager(Range<Integer> portRange) {
    this(
        portRange,
        Lists.newArrayList(SocketType.TCP, SocketType.UDP),
        SECURE_RANDOM,
        new ClientConnectChecker());
  }

  /** For testing, control all the dependencies of the PortManager. */
  @VisibleForTesting
  PortManager(
      Range<Integer> portRange,
      Collection<PortChecker> checkers,
      Random random,
      PortChecker clientConnectChecker) {
    this.random = checkNotNull(random);
    this.portSet = ContiguousSet.create(checkNotNull(portRange), DiscreteDomain.integers());
    this.checkers = ImmutableList.copyOf(checkNotNull(checkers));
    this.clientConnectChecker = checkNotNull(clientConnectChecker);
  }

  /**
   * Returns a free port within this PortManager's range.
   *
   * <p>A free port is capable to be used either as a TCP or UDP port.
   *
   * @return a free port to use for tcp/udp connections.
   * @throws NoFreePortsException if no port can be found.
   */
  public int getFreePort() {
    int startingPoint = startingPoint();

    // In order to avoid quadratic behaviour from linearly scanning the same range every time a
    // caller asks for a port, start at a random point in the port range and work upward / downwards
    // until a free port is found.
    UnmodifiableIterator<Integer> goingDown =
        portSet.headSet(startingPoint, true).descendingIterator();
    UnmodifiableIterator<Integer> goingUp = portSet.tailSet(startingPoint, false).iterator();

    while (goingUp.hasNext() || goingDown.hasNext()) {
      if (goingUp.hasNext()) {
        int port = goingUp.next();
        if (isFree(port) && inUseChecker.claim(port)) {
          return waitForPortToClose(port);
        }
      }
      if (goingDown.hasNext()) {
        int port = goingDown.next();
        if (isFree(port) && inUseChecker.claim(port)) {
          return waitForPortToClose(port);
        }
      }
    }
    throw new NoFreePortsException(
        "No ports between: " + portSet.first() + " & " + portSet.last() + " inclusive");
  }

  private int waitForPortToClose(int port) {
    long deadline = System.currentTimeMillis() + 1000;
    while (System.currentTimeMillis() < deadline) {
      if (clientConnectChecker.isPortFree(port)) {
        return port;
      }
    }
    throw new IllegalStateException("port: " + port + " hasn't closed after 1 second");
  }

  private boolean isFree(int port) {
    if (!inUseChecker.isPortFree(port)) {
      return false;
    }

    for (PortChecker checker : checkers) {
      if (!checker.isPortFree(port)) {
        return false;
      }
    }
    return true;
  }

  private int startingPoint() {
    int low = portSet.first();
    int high = portSet.last();
    synchronized (random) {
      return low + random.nextInt(high - low);
    }
  }

  /** Could not find a free port! */
  public static class NoFreePortsException extends RuntimeException {
    public NoFreePortsException(String sorry) {
      super(sorry);
    }
  }
}
