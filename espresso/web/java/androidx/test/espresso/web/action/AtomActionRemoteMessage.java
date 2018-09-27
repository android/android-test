/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.web.action;

import android.text.TextUtils;
import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.TypeProtoConverters;
import androidx.test.espresso.web.model.Atom;
import androidx.test.espresso.web.model.ElementReference;
import androidx.test.espresso.web.model.WindowReference;
import androidx.test.espresso.web.proto.action.WebActions.AtomActionProto;
import com.google.protobuf.MessageLite;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * AtomAction}.
 */
@SuppressWarnings("unused") // called reflectively
final class AtomActionRemoteMessage implements EspressoRemoteMessage.To<AtomActionProto> {

  private final AtomAction atomAction;

  public AtomActionRemoteMessage(AtomAction atomAction) {
    this.atomAction = atomAction;
  }

  @Override
  public AtomActionProto toProto() {
    AtomActionProto.Builder builder = AtomActionProto.newBuilder();
    builder.setAtom(TypeProtoConverters.typeToAny(atomAction.atom));
    if (atomAction.window != null) {
      builder.setWindow(TypeProtoConverters.typeToAny(atomAction.window));
    }
    if (atomAction.element != null) {
      builder.setElement(TypeProtoConverters.typeToAny(atomAction.element));
    }
    return builder.build();
  }

  /**
   * This field is used to create an instance of this view action from its unwrapped proto message.
   */
  public static final EspressoRemoteMessage.From<AtomAction, MessageLite> FROM =
      new EspressoRemoteMessage.From<AtomAction, MessageLite>() {
        @Override
        public AtomAction fromProto(MessageLite messageLite) {
          AtomActionProto atomActionProto = (AtomActionProto) messageLite;
          WindowReference window = null;
          ElementReference element = null;
          Atom atom = TypeProtoConverters.anyToType(atomActionProto.getAtom());
          if (!TextUtils.isEmpty(atomActionProto.getWindow().getTypeUrl())) {
            window = TypeProtoConverters.anyToType(atomActionProto.getWindow());
          }
          if (!TextUtils.isEmpty(atomActionProto.getElement().getTypeUrl())) {
            element = TypeProtoConverters.anyToType(atomActionProto.getElement());
          }

          // noinspection unchecked called reflectively
          return new AtomAction(atom, window, element);
        }
      };
}
