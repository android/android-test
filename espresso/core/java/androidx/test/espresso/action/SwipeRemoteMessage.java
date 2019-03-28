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
package androidx.test.espresso.action;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.NonNull;
import androidx.test.espresso.proto.action.ViewActions.SwipeViewActionProto;
import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.ProtoUtils;
import java.util.Locale;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * Swipe}.
 */
public final class SwipeRemoteMessage
    implements EspressoRemoteMessage.To<SwipeViewActionProto.Swipe> {

  /**
   * This field is used to create an instance of {@link Tapper} from its unwrapped proto message.
   */
  public static final EspressoRemoteMessage.From<Swiper, SwipeViewActionProto.Swipe> FROM =
      new EspressoRemoteMessage.From<Swiper, SwipeViewActionProto.Swipe>() {
        @Override
        public Swiper fromProto(SwipeViewActionProto.Swipe swipe) {
          return getTapperFromTapProto(swipe);
        }
      };

  private final Swipe swipe;

  public SwipeRemoteMessage(@NonNull Swipe swipe) {
    this.swipe = checkNotNull(swipe);
  }

  private static Swiper getTapperFromTapProto(SwipeViewActionProto.Swipe swipe) {
    return ProtoUtils.checkedGetEnumForProto(swipe.getNumber(), Swipe.class);
  }

  @Override
  public SwipeViewActionProto.Swipe toProto() {
    switch (swipe) {
      case FAST:
        return SwipeViewActionProto.Swipe.FAST;
      case SLOW:
        return SwipeViewActionProto.Swipe.SLOW;
      default:
        throw new IllegalArgumentException(
            String.format(Locale.ROOT, "Swipe proto enum for swipe: %s not found!", swipe));
    }
  }
}
