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
import androidx.test.espresso.proto.action.ViewActions.TranslatedCoordinatesProviderProto;
import androidx.test.espresso.proto.action.ViewActions.TranslatedCoordinatesProviderProto.Builder;
import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.RemoteProtocolException;
import com.google.protobuf.MessageLite;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * TranslatedCoordinatesProvider}.
 */
final class TranslatedCoordinatesProviderRemoteMessage
    implements EspressoRemoteMessage.To<TranslatedCoordinatesProviderProto> {

  /**
   * This field is used to create an instance of this view action from its unwrapped proto message.
   */
  public static final EspressoRemoteMessage.From<TranslatedCoordinatesProvider, MessageLite> FROM =
      new EspressoRemoteMessage.From<TranslatedCoordinatesProvider, MessageLite>() {
        @Override
        public TranslatedCoordinatesProvider fromProto(MessageLite messageLite) {
          TranslatedCoordinatesProviderProto translatingCoordinatesProviderProto =
              (TranslatedCoordinatesProviderProto) messageLite;

          // We need to distinguish between implementations using GeneralLocation and custom
          // CoordinatesProvider implementations, which require a custom RemoteDescriptor for
          // serialization.
          CoordinatesProvider coordinatesProvider;
          switch (translatingCoordinatesProviderProto.getCoordsLocOrCoordsAnyCase()) {
            case COORDSLOC:
              coordinatesProvider =
                  GeneralLocationRemoteMessage.FROM.fromProto(
                      translatingCoordinatesProviderProto.getCoordsLoc());
              break;
            case COORDSANY:
              coordinatesProvider = anyToType(translatingCoordinatesProviderProto.getCoordsAny());
              break;
            case COORDSLOCORCOORDSANY_NOT_SET:
            default:
              throw new RemoteProtocolException(
                  "Unable to deserialize translating coordinates " + "provider");
          }

          float dx = translatingCoordinatesProviderProto.getDx();
          float dy = translatingCoordinatesProviderProto.getDy();

          return new TranslatedCoordinatesProvider(coordinatesProvider, dx, dy);
        }
      };

  private final TranslatedCoordinatesProvider translatedCoordinatesProvider;

  /**
   * Creates an instance of {@link TranslatedCoordinatesProviderRemoteMessage}
   *
   * @param translatedCoordinatesProvider the {@link TranslatedCoordinatesProvider}
   */
  public TranslatedCoordinatesProviderRemoteMessage(
      @NonNull TranslatedCoordinatesProvider translatedCoordinatesProvider) {
    this.translatedCoordinatesProvider =
        checkNotNull(
            translatedCoordinatesProvider, "translatedCoordinatesProvider cannot be null!");
  }

  @Override
  public TranslatedCoordinatesProviderProto toProto() {

    Builder builder = TranslatedCoordinatesProviderProto.newBuilder();
    CoordinatesProvider coordinatesProvider = translatedCoordinatesProvider.coordinatesProvider;
    if (coordinatesProvider instanceof GeneralLocation) {
      builder.setCoordsLoc(
          new GeneralLocationRemoteMessage((GeneralLocation) coordinatesProvider).toProto());
    } else {
      builder.setCoordsAny(typeToAny(coordinatesProvider));
    }
    return builder
        .setDx(translatedCoordinatesProvider.dx)
        .setDy(translatedCoordinatesProvider.dy)
        .build();
  }
}
