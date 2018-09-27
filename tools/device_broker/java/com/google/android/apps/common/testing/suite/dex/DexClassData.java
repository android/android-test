/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.common.testing.suite.dex;

import com.google.android.apps.common.testing.proto.TestInfo.AnnotationPb;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

/**
 * Container object for information extracted from DexDump.
 *
 * <p>Note - the dex dump does contain more information then we capture here. Currently we have no
 * use for that info so it is discarded.
 *
 */
@AutoValue
abstract class DexClassData {

  enum Visibility { PUBLIC, PACKAGE, PROTECTED, PRIVATE };

  abstract boolean getIsAbstract();
  public boolean isAbstract() {
    return getIsAbstract();
  }

  public abstract ImmutableList<AnnotationPb> getAnnotations();
  public abstract Visibility getVisibility();
  public abstract ImmutableList<MethodData> getMethods();
  public abstract String getExtendsClass();
  public abstract String getPackageName();

  public abstract String getClassName();

  public String getFullClassName() {
    return getPackageName() + "." + getClassName();
  }

  abstract boolean getIsUiTest();
  public boolean isUiTest() {
    return getIsUiTest();
  }

  static DexClassData.Builder builder() {
    return new AutoValue_DexClassData.Builder()
        .setIsUiTest(false)
        .setIsAbstract(false)
        .setAnnotations(ImmutableList.<AnnotationPb>of())
        .setMethods(ImmutableList.<MethodData>of());
  }

  @AutoValue.Builder
  abstract static class Builder {
    public abstract Builder setIsUiTest(boolean isUiTest);
    public abstract Builder setIsAbstract(boolean isAbstract);
    public abstract Builder setClassName(String className);
    abstract Builder setVisibility(Visibility visibility);
    public Builder setVisibility(String visibility) {
      return setVisibility(Enum.valueOf(Visibility.class, visibility.toUpperCase()));
    }

    abstract ImmutableList<MethodData> getMethods();
    abstract Builder setMethods(ImmutableList<MethodData> annotations);
    public Builder addMethod(MethodData method) {
      setMethods(ImmutableList.<MethodData>builder()
          .addAll(getMethods())
          .add(method)
          .build());
      return this;
    }
    public Builder clearMethods() {
      return setMethods(ImmutableList.<MethodData>of());
    }

    abstract ImmutableList<AnnotationPb> getAnnotations();
    abstract Builder setAnnotations(ImmutableList<AnnotationPb> annotations);
    public Builder addAnnotation(AnnotationPb annotation) {
      setAnnotations(ImmutableList.<AnnotationPb>builder()
          .addAll(getAnnotations())
          .add(annotation)
          .build());
      return this;
    }
    public Builder clearAnnotations() {
      return setAnnotations(ImmutableList.<AnnotationPb>of());
    }

    public abstract Builder setExtendsClass(String extendsClass);
    public abstract Builder setPackageName(String packageName);
    public abstract DexClassData build();
  }

  @AutoValue
  abstract static class MethodData {

    public abstract ImmutableList<AnnotationPb> getAnnotations();
    abstract boolean getIsAbstract();
    public final boolean isAbstract() {
      return getIsAbstract();
    }

    public abstract Visibility getVisibility();
    abstract boolean getHasArguments();
    public final boolean hasArguments() {
      return getHasArguments();
    }

    abstract boolean getHasReturnType();
    public final boolean hasReturnType() {
      return getHasReturnType();
    }

    @Nullable
    public abstract String getMethodName();
    public abstract MethodBuilder toBuilder();
    static MethodBuilder builder() {
      return new AutoValue_DexClassData_MethodData.Builder()
          .setHasReturnType(false)
          .setHasArguments(false)
          .setMethodName(null)
          .setAnnotations(ImmutableList.<AnnotationPb>of());
    }

    @AutoValue.Builder
    abstract static class MethodBuilder {

      abstract ImmutableList<AnnotationPb> getAnnotations();
      abstract MethodBuilder setAnnotations(ImmutableList<AnnotationPb> annotations);
      public MethodBuilder addAnnotation(AnnotationPb annotation) {
        setAnnotations(ImmutableList.<AnnotationPb>builder()
            .addAll(getAnnotations())
            .add(annotation)
            .build());
        return this;
      }
      public MethodBuilder clearAnnotations() {
        return setAnnotations(ImmutableList.<AnnotationPb>of());
      }

      public abstract MethodBuilder setIsAbstract(boolean isAbstract);

      abstract MethodBuilder setVisibility(Visibility visibility);
      public MethodBuilder setVisibility(String visibility) {
        return setVisibility(Enum.valueOf(Visibility.class, visibility.toUpperCase()));
      }

      public abstract MethodBuilder setHasArguments(boolean hasArguments);
      public abstract MethodBuilder setHasReturnType(boolean hasReturnType);
      public abstract MethodBuilder setMethodName(@Nullable String methodName);
      public abstract MethodData build();
    }
  }
}
