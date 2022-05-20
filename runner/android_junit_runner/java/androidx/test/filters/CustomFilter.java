/*
 * Copyright (C) 2022 The Android Open Source Project
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

package androidx.test.filters;

import androidx.test.annotation.ExperimentalTestApi;
import androidx.test.internal.runner.filters.AbstractFilter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotations for custom test filters that will be plugged into Android JUnit Runner.
 *
 * <p>This annotation takes in a filter implementation class, which must extend {@link
 * AbstractFilter} and handle the test annotation that @CustomFilter is being added to.
 *
 * <p>This API is experimental and subject to change or removal.
 */
@ExperimentalTestApi
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface CustomFilter {
  Class<? extends AbstractFilter> filterClass();
}
