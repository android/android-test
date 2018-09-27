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
 *
 */
package androidx.test.espresso.action;

import android.util.Log;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.proto.action.ViewActions.ClickViewActionProto;
import androidx.test.espresso.remote.EspressoRemoteMessage;
import com.google.protobuf.MessageLite;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * GeneralClickAction}.
 */
public final class GeneralClickActionRemoteMessage
    implements EspressoRemoteMessage.To<MessageLite> {
  private static final String TAG = "GCARemoteMessage";

  private final CoordinatesProvider coordinatesProvider;
  private final Tapper tapper;
  private final PrecisionDescriber precisionDescriber;

  public GeneralClickActionRemoteMessage(GeneralClickAction generalClickAction) {
    this.tapper = generalClickAction.tapper;
    this.coordinatesProvider = generalClickAction.coordinatesProvider;
    this.precisionDescriber = generalClickAction.precisionDescriber;
  }

  @Override
  public ClickViewActionProto toProto() {
    try {
      return ClickViewActionProto.newBuilder()
          .setId(GeneralClickAction.class.getCanonicalName())
          .setTap(new TapRemoteMessage((Tap) tapper).toProto())
          .setLocation(
              new GeneralLocationRemoteMessage((GeneralLocation) coordinatesProvider).toProto())
          .setPrecision(new PressRemoteMessage((Press) precisionDescriber).toProto())
          .build();
    } catch (ClassCastException cce) {
      Log.e(
          TAG,
          "Your implementation is not compatible with Espresso Remote. Implement the"
              + "EspressoRemoteMessage.To interface in your custom Tapper, CoordinatesProvider or"
              + "PrecisionDescriber");
      // TODO: We might want to replace this a custom Espresso remote exception.
      throw cce;
    }
  }

  /**
   * This field is used to create an instance of this view action from its unwrapped proto message.
   */
  public static final EspressoRemoteMessage.From<ViewAction, MessageLite> FROM =
      new EspressoRemoteMessage.From<ViewAction, MessageLite>() {
        @Override
        public ViewAction fromProto(MessageLite messageLite) {
          ClickViewActionProto clickViewActionMessage = (ClickViewActionProto) messageLite;

          Tapper tapper = TapRemoteMessage.FROM.fromProto(clickViewActionMessage.getTap());

          CoordinatesProvider coordinatesProvider =
              GeneralLocationRemoteMessage.FROM.fromProto(clickViewActionMessage.getLocation());

          PrecisionDescriber precisionDescriber =
              PressRemoteMessage.FROM.fromProto(clickViewActionMessage.getPrecision());

          return new GeneralClickAction(tapper, coordinatesProvider, precisionDescriber);
        }
      };
}
