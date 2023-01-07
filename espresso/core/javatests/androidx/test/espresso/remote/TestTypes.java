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

import static kotlin.collections.CollectionsKt.listOf;

import android.net.Uri;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.web.model.Atom;
import androidx.test.espresso.web.model.ElementReference;
import androidx.test.espresso.web.model.Evaluation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Objects;

/**
 * Collection of test classes for testing proto serialization using {@link EspressoRemoteMessage}
 */
public final class TestTypes {

  static final String TEST_TYPE_STRING1 = "4";
  static final String TEST_TYPE_STRING2 = "8";
  static final String TEST_TYPE_STRING3 = "15";
  static final String TEST_TYPE_STRING4 = "16";
  static final byte A_BYTE = Byte.parseByte("48");
  static final int AN_INT = 15162342;
  static final long A_LONG = 4815162342L;
  static final String A_STRING = "aString";
  static final TestType A_TEST_TYPE1 = new TestType(TEST_TYPE_STRING1);
  static final TestType A_TEST_TYPE2 = new TestType(TEST_TYPE_STRING2);
  static final TestType A_TEST_TYPE3 = new TestType(TEST_TYPE_STRING3);
  static final TestType A_TEST_TYPE4 = new TestType(TEST_TYPE_STRING4);
  static final Iterable<TestType> ANY_TYPE_ITERABLE =
      listOf(A_TEST_TYPE1, A_TEST_TYPE2, A_TEST_TYPE3, A_TEST_TYPE4);

  private TestTypes() {
    // no instance
  }

  static final class MultipleFieldClass {
    final byte aByte;
    final int anInt;
    final long aLong;
    final String aString;
    final TestType anyRegisteredType;
    final Iterable<TestType> anyTypeIterable;

    public MultipleFieldClass(
        byte aByte,
        int anInt,
        long aLong,
        String aString,
        TestType anyRegisteredType,
        Iterable<TestType> anyTypeIterable) {
      this.aByte = aByte;
      this.anInt = anInt;
      this.aLong = aLong;
      this.aString = aString;
      this.anyRegisteredType = anyRegisteredType;
      this.anyTypeIterable = anyTypeIterable;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      MultipleFieldClass that = (MultipleFieldClass) o;
      return aByte == that.aByte
          && anInt == that.anInt
          && aLong == that.aLong
          && Objects.equals(aString, that.aString)
          && Objects.equals(anyRegisteredType, that.anyRegisteredType)
          && Objects.equals(anyTypeIterable, that.anyTypeIterable);
    }

    @Override
    public int hashCode() {
      return Objects.hash(aByte, anInt, aLong, aString, anyRegisteredType, anyTypeIterable);
    }
  }

  static final class ParcelableFieldClass {
    final Uri uri;

    public ParcelableFieldClass(Uri uri) {
      this.uri = uri;
    }
  }

  static final class TestType {
    protected final String hello;
    protected final String hello1 = "hello1";

    public TestType(String hello) {
      this.hello = hello;
    }

    public String getHello() {
      return hello;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TestType testType = (TestType) o;

      return hello != null ? hello.equals(testType.hello) : testType.hello == null;
    }

    @Override
    public int hashCode() {
      return hello != null ? hello.hashCode() : 0;
    }
  }

  public static final class NoArgClass {}

  public static final class RemoteMsgFieldAnnotatedFieldsClass {

    @RemoteMsgField(order = 0)
    final String aString;

    @RemoteMsgField(order = 1)
    final TestType anyRegisteredType;

    @RemoteMsgField(order = 2)
    final Iterable<TestType> anyTypeIterable;

    public RemoteMsgFieldAnnotatedFieldsClass(
        String aString, TestType anyRegisteredType, Iterable<TestType> anyTypeIterable) {
      this.aString = aString;
      this.anyRegisteredType = anyRegisteredType;
      this.anyTypeIterable = anyTypeIterable;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      RemoteMsgFieldAnnotatedFieldsClass that = (RemoteMsgFieldAnnotatedFieldsClass) o;

      if (!aString.equals(that.aString)) {
        return false;
      }
      if (!anyRegisteredType.equals(that.anyRegisteredType)) {
        return false;
      }
      return anyTypeIterable.equals(that.anyTypeIterable);
    }

    @Override
    public int hashCode() {
      int result = aString.hashCode();
      result = 31 * result + anyRegisteredType.hashCode();
      result = 31 * result + anyTypeIterable.hashCode();
      return result;
    }
  }

  public static class TestAtom implements Atom<Void> {

    @RemoteMsgConstructor
    public TestAtom() {}

    @Override
    public String getScript() {
      return null;
    }

    @Override
    public List<Object> getArguments(ElementReference elementContext) {
      return null;
    }

    @Override
    public Void transform(Evaluation evaluation) {
      return null;
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.CONSTRUCTOR})
  public @interface TestAnnotation {}
}
