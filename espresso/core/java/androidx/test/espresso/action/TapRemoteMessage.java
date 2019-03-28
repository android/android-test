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
import androidx.test.espresso.proto.action.ViewActions.ClickViewActionProto;
import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.ProtoUtils;
import java.util.Locale;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * Tap}.
 */
public final class TapRemoteMessage implements EspressoRemoteMessage.To<ClickViewActionProto.Tap> {

  private final Tap tap;

  public TapRemoteMessage(@NonNull Tap tap) {
    this.tap = checkNotNull(tap);
  }

  @Override
  public ClickViewActionProto.Tap toProto() {
    switch (tap) {
      case SINGLE:
        return ClickViewActionProto.Tap.SINGLE;
      case LONG:
        return ClickViewActionProto.Tap.LONG;
      case DOUBLE:
        return ClickViewActionProto.Tap.DOUBLE;
      default:
        throw new IllegalArgumentException(
            String.format(Locale.ROOT, "Tap proto enum for general location: %s not found!", tap));
    }
  }

  /**
   * This field is used to create an instance of {@link Tapper} from its unwrapped proto message.
   */
  public static final EspressoRemoteMessage.From<Tapper, ClickViewActionProto.Tap> FROM =
      new EspressoRemoteMessage.From<Tapper, ClickViewActionProto.Tap>() {
        @Override
        public Tapper fromProto(ClickViewActionProto.Tap tap) {
          return getTapperFromTapProto(tap);
        }
      };

  private static Tapper getTapperFromTapProto(ClickViewActionProto.Tap tap) {
    return ProtoUtils.checkedGetEnumForProto(tap.getNumber(), Tap.class);
  }
}
