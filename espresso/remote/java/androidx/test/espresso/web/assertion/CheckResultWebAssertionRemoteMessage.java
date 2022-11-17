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
package androidx.test.espresso.web.assertion;

import androidx.test.espresso.remote.EspressoRemoteMessage;
import androidx.test.espresso.remote.RemoteProtocolException;
import androidx.test.espresso.remote.TypeProtoConverters;
import androidx.test.espresso.web.assertion.WebAssertion.CheckResultWebAssertion;
import androidx.test.espresso.web.proto.assertion.WebAssertions.CheckResultAssertionProto;
import com.google.protobuf.MessageLite;
import java.io.Serializable;
import org.w3c.dom.Document;

/**
 * {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} implementation of {@link
 * CheckResultWebAssertion}.
 */
@SuppressWarnings("unused") // called reflectively
public class CheckResultWebAssertionRemoteMessage
    implements EspressoRemoteMessage.To<CheckResultAssertionProto> {

  /**
   * This field is used to create an instance of this view action from its unwrapped proto message.
   */
  public static final EspressoRemoteMessage.From<CheckResultWebAssertion<?>, MessageLite> FROM =
      new EspressoRemoteMessage.From<CheckResultWebAssertion<?>, MessageLite>() {
        @Override
        public CheckResultWebAssertion<?> fromProto(MessageLite message) {
          CheckResultAssertionProto checkResultProto = (CheckResultAssertionProto) message;

          // We need to distinguish between implementations that are serializable and
          // implementations that require a custom RemoteDescriptor for serialization.
          Object result;
          switch (checkResultProto.getResultCase()) {
            case SERIALISABLERESULT:
              result =
                  TypeProtoConverters.byteStringToType(checkResultProto.getSerialisableResult());
              break;
            case DOCUMENTRESULT:
              result =
                  DocumentProtoConverters.byteStringToDocument(
                      checkResultProto.getDocumentResult());
              break;
            case ANYRESULT:
              result = TypeProtoConverters.anyToType(checkResultProto.getAnyResult());
              break;
            case RESULT_NOT_SET:
            default:
              throw new RemoteProtocolException("Unable to deserialize CheckResultWebAssertion");
          }

          WebAssertion<?> webAssertion =
              TypeProtoConverters.anyToType(checkResultProto.getWebAssertion());

          return new CheckResultWebAssertion(result, webAssertion);
        }
      };

  private final CheckResultWebAssertion<?> checkResultWebAssertion;

  public CheckResultWebAssertionRemoteMessage(CheckResultWebAssertion<?> checkResultWebAssertion) {
    this.checkResultWebAssertion = checkResultWebAssertion;
  }

  @Override
  public CheckResultAssertionProto toProto() {
    CheckResultAssertionProto.Builder checkResultBuilder = CheckResultAssertionProto.newBuilder();
    Object result = checkResultWebAssertion.result;
    if (result instanceof Serializable) {
      checkResultBuilder.setSerialisableResult(TypeProtoConverters.typeToByteString(result));
    } else if (result instanceof Document) {
      checkResultBuilder.setDocumentResult(
          DocumentProtoConverters.documentToByteString((Document) result));
    } else {
      // process as any type.
      checkResultBuilder.setAnyResult(TypeProtoConverters.typeToAny(result));
    }

    checkResultBuilder.setWebAssertion(
        TypeProtoConverters.typeToAny(checkResultWebAssertion.webAssertion));
    return checkResultBuilder.build();
  }
}
