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
package androidx.test.espresso.remote;

import androidx.test.espresso.proto.TestProtos.TestProto;
import androidx.test.espresso.remote.TestTypes.TestType;
import com.google.protobuf.MessageLite;

/** {@link EspressoRemoteMessage} implementation of {@link TestType} */
final class TestTypeRemoteMessage implements EspressoRemoteMessage.To<TestProto> {

  private final TestType testType;

  public TestTypeRemoteMessage(TestType testType) {
    this.testType = testType;
  }

  @Override
  public TestProto toProto() {
    return TestProto.newBuilder().setHello(testType.hello).build();
  }

  /**
   * This field is used to create an instance of {@link TestType} from its unwrapped proto message.
   */
  public static final EspressoRemoteMessage.From<TestType, MessageLite> FROM =
      new EspressoRemoteMessage.From<TestType, MessageLite>() {
        @Override
        public TestType fromProto(MessageLite messageLite) {
          TestProto testProto = (TestProto) messageLite;
          return new TestType(testProto.getHello());
        }
      };
}
