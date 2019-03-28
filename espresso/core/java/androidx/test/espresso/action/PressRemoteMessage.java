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
import androidx.test.espresso.proto.action.ViewActions.PressProto.Precision;
import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.ProtoUtils;
import java.util.Locale;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * Press}.
 */
public final class PressRemoteMessage implements EspressoRemoteMessage.To<Precision> {

  /**
   * This field is used to create an instance of {@link PrecisionDescriber} from its unwrapped proto
   * message.
   */
  public static final EspressoRemoteMessage.From<PrecisionDescriber, Precision> FROM =
      new EspressoRemoteMessage.From<PrecisionDescriber, Precision>() {
        @Override
        public PrecisionDescriber fromProto(Precision precision) {
          return getPrecisionDescriberFromPrecisionProto(precision);
        }
      };

  private final Press press;

  public PressRemoteMessage(@NonNull Press press) {
    this.press = checkNotNull(press);
  }

  private static PrecisionDescriber getPrecisionDescriberFromPrecisionProto(Precision precision) {
    return ProtoUtils.checkedGetEnumForProto(precision.getNumber(), Press.class);
  }

  @Override
  public Precision toProto() {
    switch (press) {
      case PINPOINT:
        return Precision.PINPOINT;
      case FINGER:
        return Precision.FINGER;
      case THUMB:
        return Precision.THUMB;
      default:
        throw new IllegalArgumentException(
            String.format(
                Locale.ROOT, "Precision proto enum for general location: %s not found!", press));
    }
  }
}
