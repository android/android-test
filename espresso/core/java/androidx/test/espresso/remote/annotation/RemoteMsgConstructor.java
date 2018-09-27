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
package androidx.test.espresso.remote.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constructors annotated with {@link RemoteMsgConstructor} are called during remote message
 * deserialization to recreate a previously serialized instance.
 *
 * <p>This annotation is the counterpart of {@link FieldDescriptor}. Any instance fields marked as
 * serializable using an {@link FieldDescriptor} will be injected into the annotated constructor.
 *
 * <p>Note: The annotated constructors param order must match the order defined in {@link
 * FieldDescriptor}
 *
 * @see androidx.test.espresso.remote.GenericRemoteMessage
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR})
public @interface RemoteMsgConstructor {}
