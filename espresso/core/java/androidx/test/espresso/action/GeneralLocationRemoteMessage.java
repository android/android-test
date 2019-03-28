/*
 * Copyright (C) 2016 The Android Open Source Project
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
package androidx.test.espresso.action;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.NonNull;
import androidx.test.espresso.proto.action.ViewActions.GeneralLocationProto.Location;
import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.ProtoUtils;
import java.util.Locale;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * GeneralLocation}.
 */
public final class GeneralLocationRemoteMessage implements EspressoRemoteMessage.To<Location> {

  private final GeneralLocation generalLocation;

  public GeneralLocationRemoteMessage(@NonNull GeneralLocation generalLocation) {
    this.generalLocation = checkNotNull(generalLocation);
  }

  private static CoordinatesProvider getCoordinatesProviderFromLocationProto(Location location) {
    return ProtoUtils.checkedGetEnumForProto(location.getNumber(), GeneralLocation.class);
  }

  @Override
  public Location toProto() {
    switch (generalLocation) {
      case TOP_LEFT:
        return Location.TOP_LEFT;
      case TOP_CENTER:
        return Location.TOP_CENTER;
      case TOP_RIGHT:
        return Location.TOP_RIGHT;
      case CENTER_LEFT:
        return Location.CENTER_LEFT;
      case CENTER:
        return Location.CENTER;
      case CENTER_RIGHT:
        return Location.CENTER_RIGHT;
      case BOTTOM_LEFT:
        return Location.BOTTOM_LEFT;
      case BOTTOM_CENTER:
        return Location.BOTTOM_CENTER;
      case BOTTOM_RIGHT:
        return Location.BOTTOM_RIGHT;
      case VISIBLE_CENTER:
        return Location.VISIBLE_CENTER;
      default:
        throw new IllegalArgumentException(
            String.format(
                Locale.ROOT,
                "Location proto enum for general location: %s not found!",
                generalLocation));
    }
  }

  /**
   * This field is used to create an instance of {@link CoordinatesProvider} from its unwrapped
   * proto message.
   */
  public static final EspressoRemoteMessage.From<CoordinatesProvider, Location> FROM =
      new EspressoRemoteMessage.From<CoordinatesProvider, Location>() {
        @Override
        public CoordinatesProvider fromProto(Location location) {
          return getCoordinatesProviderFromLocationProto(location);
        }
      };
}
