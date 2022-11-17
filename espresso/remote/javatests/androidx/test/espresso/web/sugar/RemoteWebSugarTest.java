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

package androidx.test.espresso.web.sugar;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import androidx.test.espresso.remote.GenericRemoteMessage;
import androidx.test.espresso.remote.RemoteDescriptorRegistry;
import androidx.test.espresso.web.proto.sugar.WebSugar.ExceptionPropagatorProto;
import androidx.test.espresso.web.sugar.Web.WebInteraction.ExceptionPropagator;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Remote message transformation related test for all web sugar */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RemoteWebSugarTest {

  @Before
  public void registerWebSugarWithRegistry() {
    RemoteDescriptorRegistry remoteDescriptorRegistry = RemoteDescriptorRegistry.getInstance();
    RemoteWebSugar.init(remoteDescriptorRegistry);
  }

  @Test
  public void exceptionPropagator_transformationToProto() {
    ExceptionPropagator exceptionPropagator = new ExceptionPropagator(new Throwable());

    ExceptionPropagatorProto exceptionPropagatorProto =
        (ExceptionPropagatorProto) new GenericRemoteMessage(exceptionPropagator).toProto();
    assertThat(exceptionPropagatorProto, notNullValue());
  }

  @Test
  public void exceptionPropagator_transformationFromProto() {
    ExceptionPropagator exceptionPropagator = new ExceptionPropagator(new Throwable());

    ExceptionPropagatorProto exceptionPropagatorProto =
        (ExceptionPropagatorProto) new GenericRemoteMessage(exceptionPropagator).toProto();
    ExceptionPropagator exceptionPropagatorFromProto =
        (ExceptionPropagator) GenericRemoteMessage.FROM.fromProto(exceptionPropagatorProto);

    assertThat(exceptionPropagatorFromProto, notNullValue());
    assertThat(exceptionPropagatorFromProto, instanceOf(ExceptionPropagator.class));
  }
}
