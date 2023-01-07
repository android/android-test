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
package androidx.test.espresso.web.model;

import static kotlin.collections.CollectionsKt.mutableListOf;

import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.TypeProtoConverters;
import androidx.test.espresso.web.model.Atoms.ScriptWithArgsSimpleAtom;
import androidx.test.espresso.web.proto.model.WebModelAtoms.ScriptWithArgsSimpleAtomProto;
import androidx.test.espresso.web.proto.model.WebModelAtoms.ScriptWithArgsSimpleAtomProto.Builder;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * ScriptWithArgsSimpleAtom}.
 */
public class ScriptWithArgsSimpleAtomRemoteMessage
    implements EspressoRemoteMessage.To<ScriptWithArgsSimpleAtomProto> {

  /**
   * This field is used to create an instance of this view action from its unwrapped proto message.
   */
  public static final EspressoRemoteMessage.From<ScriptWithArgsSimpleAtom, MessageLite> FROM =
      new EspressoRemoteMessage.From<ScriptWithArgsSimpleAtom, MessageLite>() {
        @Override
        public ScriptWithArgsSimpleAtom fromProto(MessageLite message) {
          ScriptWithArgsSimpleAtomProto scriptWithArgsSimpleAtomProto =
              (ScriptWithArgsSimpleAtomProto) message;

          String script = scriptWithArgsSimpleAtomProto.getScript();
          List<Object> nonContextualArguments = mutableListOf();
          List<ByteString> argsList = scriptWithArgsSimpleAtomProto.getArgsList();
          for (ByteString arg : argsList) {
            nonContextualArguments.add(TypeProtoConverters.byteStringToType(arg));
          }
          return new ScriptWithArgsSimpleAtom(script, nonContextualArguments);
        }
      };

  private final ScriptWithArgsSimpleAtom scriptWithArgsSimpleAtom;

  public ScriptWithArgsSimpleAtomRemoteMessage(ScriptWithArgsSimpleAtom scriptWithArgsSimpleAtom) {
    this.scriptWithArgsSimpleAtom = scriptWithArgsSimpleAtom;
  }

  @Override
  public ScriptWithArgsSimpleAtomProto toProto() {
    Builder builder =
        ScriptWithArgsSimpleAtomProto.newBuilder().setScript(scriptWithArgsSimpleAtom.getScript());

    List<Object> nonContextualArguments = scriptWithArgsSimpleAtom.getNonContextualArguments();
    if (!nonContextualArguments.isEmpty()) {
      List<ByteString> argsList = new ArrayList<>(nonContextualArguments.size());
      for (Object object : nonContextualArguments) {
        argsList.add(TypeProtoConverters.typeToByteString(object));
      }
      builder.addAllArgs(argsList);
    }
    return builder.build();
  }
}
