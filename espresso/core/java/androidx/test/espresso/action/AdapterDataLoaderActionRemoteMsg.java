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

import static androidx.test.espresso.remote.TypeProtoConverters.byteStringToType;
import static androidx.test.espresso.remote.TypeProtoConverters.typeToAny;
import static androidx.test.espresso.remote.TypeProtoConverters.typeToByteString;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.proto.action.ViewActions.AdapterDataLoaderActionProto;
import androidx.test.espresso.remote.ConstructorInvocation;
import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.TypeProtoConverters;
import androidx.test.espresso.util.EspressoOptional;
import org.hamcrest.Matcher;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * AdapterDataLoaderAction}.
 */
public final class AdapterDataLoaderActionRemoteMsg
    implements EspressoRemoteMessage.To<AdapterDataLoaderActionProto> {
  @VisibleForTesting static final int NO_POSITION_SET = -1;

  private final EspressoOptional<Integer> atPosition;
  private final Matcher<? extends Object> dataToLoadMatcher;
  private final Class<? extends AdapterViewProtocol> adapterViewProtocolClass;

  public AdapterDataLoaderActionRemoteMsg(
      @NonNull AdapterDataLoaderAction adapterDataLoaderAction) {
    this.atPosition = adapterDataLoaderAction.atPosition;
    this.dataToLoadMatcher = adapterDataLoaderAction.dataToLoadMatcher;
    this.adapterViewProtocolClass = adapterDataLoaderAction.adapterViewProtocol.getClass();
  }

  @Override
  public AdapterDataLoaderActionProto toProto() {
    return AdapterDataLoaderActionProto.newBuilder()
        .setAtPosition(atPosition.or(NO_POSITION_SET))
        .setDataToLoadMatcher(typeToAny(dataToLoadMatcher))
        .setAdapterViewProtocolClass(typeToByteString(adapterViewProtocolClass))
        .build();
  }

  /**
   * This field is used to create an instance of {@link AdapterDataLoaderAction} from its unwrapped
   * proto message.
   */
  public static final EspressoRemoteMessage.From<
          AdapterDataLoaderAction, AdapterDataLoaderActionProto>
      FROM =
          new EspressoRemoteMessage.From<AdapterDataLoaderAction, AdapterDataLoaderActionProto>() {
            @Override
            public AdapterDataLoaderAction fromProto(
                AdapterDataLoaderActionProto dataLoaderActionProto) {
              Class<? extends AdapterViewProtocol> adapterViewProtocolClass =
                  byteStringToType(dataLoaderActionProto.getAdapterViewProtocolClass());
              AdapterViewProtocol adapterViewProtocol =
                  adapterViewProtocolClass.cast(
                      new ConstructorInvocation(adapterViewProtocolClass, null)
                          .invokeConstructor());
              return new AdapterDataLoaderAction(
                  TypeProtoConverters.<Matcher<? extends Object>>anyToType(
                      dataLoaderActionProto.getDataToLoadMatcher()),
                  NO_POSITION_SET == dataLoaderActionProto.getAtPosition()
                      ? EspressoOptional.<Integer>absent()
                      : EspressoOptional.of(dataLoaderActionProto.getAtPosition()),
                  adapterViewProtocol);
            }
          };
}
