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
 *
 */
package androidx.test.espresso.action;

import static androidx.test.espresso.remote.TypeProtoConverters.anyToType;
import static androidx.test.espresso.remote.TypeProtoConverters.typeToAny;
import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.NonNull;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.proto.action.ViewActions.SwipeViewActionProto;
import androidx.test.espresso.proto.action.ViewActions.SwipeViewActionProto.Builder;
import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.RemoteProtocolException;
import com.google.protobuf.MessageLite;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * GeneralSwipeAction}.
 */
public final class GeneralSwipeActionRemoteMessage
    implements EspressoRemoteMessage.To<MessageLite> {

  /**
   * This field is used to create an instance of this view action from its unwrapped proto message.
   */
  public static final EspressoRemoteMessage.From<ViewAction, MessageLite> FROM =
      new EspressoRemoteMessage.From<ViewAction, MessageLite>() {
        @Override
        public ViewAction fromProto(MessageLite messageLite) {
          SwipeViewActionProto swipeViewActionMessage = (SwipeViewActionProto) messageLite;

          Swiper swiper = SwipeRemoteMessage.FROM.fromProto(swipeViewActionMessage.getSwipe());

          // We need to distinguish between implementations using GeneralLocation and custom
          // CoordinatesProvider implementations, which require a custom RemoteDescriptor for
          // serialization.
          CoordinatesProvider startCoordinatesProvider;
          switch (swipeViewActionMessage.getStartCoordsLocOrStartCoordsAnyCase()) {
            case STARTCOORDSLOC:
              startCoordinatesProvider =
                  GeneralLocationRemoteMessage.FROM.fromProto(
                      swipeViewActionMessage.getStartCoordsLoc());
              break;
            case STARTCOORDSANY:
              startCoordinatesProvider = anyToType(swipeViewActionMessage.getStartCoordsAny());
              break;
            case STARTCOORDSLOCORSTARTCOORDSANY_NOT_SET:
            default:
              throw new RemoteProtocolException("Unable to deserialize start coordinates provider");
          }

          CoordinatesProvider endCoordinatesProvider;
          switch (swipeViewActionMessage.getEndCoordsLocOrEndCoordsAnyCase()) {
            case ENDCOORDSLOC:
              endCoordinatesProvider =
                  GeneralLocationRemoteMessage.FROM.fromProto(
                      swipeViewActionMessage.getEndCoordsLoc());
              break;
            case ENDCOORDSANY:
              endCoordinatesProvider = anyToType(swipeViewActionMessage.getEndCoordsAny());
              break;
            case ENDCOORDSLOCORENDCOORDSANY_NOT_SET:
            default:
              throw new RemoteProtocolException("Unable to deserialize end coordinates provider");
          }

          PrecisionDescriber precisionDescriber =
              PressRemoteMessage.FROM.fromProto(swipeViewActionMessage.getPrecision());

          return new GeneralSwipeAction(
              swiper, startCoordinatesProvider, endCoordinatesProvider, precisionDescriber);
        }
      };

  private final Swiper swiper;
  private final CoordinatesProvider startCoordinatesProvider;
  private final CoordinatesProvider endCoordinatesProvider;
  private final PrecisionDescriber precisionDescriber;

  public GeneralSwipeActionRemoteMessage(@NonNull GeneralSwipeAction generalSwipeAction) {
    checkNotNull(generalSwipeAction, "generalSwipeAction cannot be null!");
    this.swiper = generalSwipeAction.swiper;
    this.startCoordinatesProvider = generalSwipeAction.startCoordinatesProvider;
    this.endCoordinatesProvider = generalSwipeAction.endCoordinatesProvider;
    this.precisionDescriber = generalSwipeAction.precisionDescriber;
  }

  @Override
  public SwipeViewActionProto toProto() {
    Builder builder =
        SwipeViewActionProto.newBuilder()
            .setId(GeneralSwipeAction.class.getCanonicalName())
            .setSwipe(new SwipeRemoteMessage((Swipe) swiper).toProto());
    if (startCoordinatesProvider instanceof GeneralLocation) {
      builder.setStartCoordsLoc(
          new GeneralLocationRemoteMessage((GeneralLocation) startCoordinatesProvider).toProto());
    } else {
      builder.setStartCoordsAny(typeToAny(startCoordinatesProvider));
    }
    if (endCoordinatesProvider instanceof GeneralLocation) {
      builder.setEndCoordsLoc(
          new GeneralLocationRemoteMessage((GeneralLocation) endCoordinatesProvider).toProto());
    } else {
      builder.setEndCoordsAny(typeToAny(endCoordinatesProvider));
    }
    return builder
        .setPrecision(new PressRemoteMessage((Press) precisionDescriber).toProto())
        .build();
  }
}
