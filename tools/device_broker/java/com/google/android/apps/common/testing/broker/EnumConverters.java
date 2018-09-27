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

import com.beust.jcommander.IStringConverter;

/** Converts strings to enums for JCommander. */
public class EnumConverters {
  /** Abs enum converter */
  public static class AbsEnumConverter<T extends Enum<T>> implements IStringConverter<T> {
    private final Class<T> clazz;

    public AbsEnumConverter(Class<T> clazz) {
      this.clazz = checkNotNull(clazz);
    }

    @Override
    public final T convert(String value) {
      return Enum.valueOf(clazz, value);
    }
  }

  /** DeviceBroker type converter */
  public static class DeviceBrokerTypeConverter extends AbsEnumConverter<DeviceBrokerType> {
    public DeviceBrokerTypeConverter() {
      super(DeviceBrokerType.class);
    }
  }

  /** Open GL driver converter */
  public static class OpenGlDriverConverter extends AbsEnumConverter<OpenGlDriver> {
    public OpenGlDriverConverter() {
      super(OpenGlDriver.class);
    }
  }

  /** Network type converter */
  public static class NetworkTypeConverter extends AbsEnumConverter<NetworkType> {
    public NetworkTypeConverter() {
      super(NetworkType.class);
    }
  }
}
