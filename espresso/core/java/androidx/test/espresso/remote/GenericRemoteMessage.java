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

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.google.protobuf.MessageLite;

/**
 * Generic implementation of the {@link EspressoRemoteMessage} interface, which uses reflection for
 * proto message serialization and deserialization.
 *
 * <p>Every Espresso matcher, view action or view assertion needs to support proto serialization to
 * participate in any kind of cross process UI interaction using Espresso Remote.
 *
 * <p>Each serializable type T needs to provide two things. First a proto message declaration of
 * type T. Second an {@link EspressoRemoteMessage} class which implements the {@link
 * EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From} interfaces to
 * serialize/deserialization any state into their proto message equivalent.
 *
 * <p>This {@link GenericRemoteMessage} class is special type of {@link EspressoRemoteMessage},
 * which implements the the {@link EspressoRemoteMessage.To} and {@link EspressoRemoteMessage.From}
 * interfaces and uses reflection to perform serialisation of an object of type T into its proto
 * message representation.
 *
 * <p>Espresso Remote uses a global {@link RemoteDescriptorRegistry} for {@link RemoteDescriptor}
 * lookups. Where a {@link RemoteDescriptor} is a descriptor object which contains all the necessary
 * meta information for proto serialization.
 *
 * <p>Usage:
 *
 * <p>For a type {@code Foo} with fields {@code bar} and {@code baz}:
 *
 * <pre>{@code
 * public class Foo {
 *   private final String bar;
 *   private final int baz;
 *
 *   protected Foo(String bar, int baz) { // set fields }
 * }
 * }</pre>
 *
 * <p>Note: A protected constructor with serializable fields in declared order is required for proto
 * deserialization.
 *
 * <p>Create a corresponding proto definition, {@code FooProto.proto} for {@code Foo}:
 *
 * <pre>{@code
 * message FooProto {
 *   bytes bar = 1; // Name needs to match Foo#bar
 *   bytes baz = 2; // Name needs to match Foo#baz
 * }
 * }</pre>
 *
 * <p>The proto type definitions used with {@link GenericRemoteMessage} need to be of type {@code
 * byte} and the type names must match the variable name of the serializable fields in {@code
 * Foo.java}.
 *
 * <p>The last step is to create a {@link RemoteDescriptor} using a {@link
 * RemoteDescriptor.Builder}, to configure all the meta data required for generic serialization.
 * Finally register the descriptor with the {@link RemoteDescriptorRegistry}. A typical descriptor
 * builder looks like this:
 *
 * <pre>{@code
 * new RemoteDescriptor.Builder()
 *   .setInstanceType(Foo.class)
 *   .setInstanceFieldDescriptors(
 *     FieldDescriptor.of(String.class, "bar")),
 *     FieldDescriptor.of(int.class, 32))
 *   .setRemoteType(GenericRemoteMessage.class)
 *   .setProtoType(FooProto.class)
 *   .build();
 * }</pre>
 *
 * <p>First set the instance type, {@code Foo.class}. Then specify the serializable fields using a
 * {@link FieldDescriptor}. Where a {@link FieldDescriptor} represents the name and the type of a
 * reflective field of {@code Foo}. Any fields described by the field properties and passed to
 * {@link RemoteDescriptor.Builder#setInstanceFieldDescriptors(FieldDescriptor...)} will be
 * serialised into {@code FooProto.proto}. Next specify that type {@code Foo} will use for it's
 * serialization entry point, in this case {@link GenericRemoteMessage} and lastly set the proto
 * message class.
 *
 * <p>Note: The declared field properties order, must match the protected constructor of {@code
 * Foo}, which takes the serializable fields in declared order!
 *
 * @throws RemoteProtocolException if a {@link FieldDescriptor}s field name does not match any field
 *     in the declared type or proto message.
 * @throws RemoteProtocolException if type cannot be serialised or dematerialised
 */
public final class GenericRemoteMessage implements EspressoRemoteMessage.To<MessageLite> {

  private final RemoteMessageSerializer remoteMessageSerializer;

  /**
   * Creates a new {@link GenericRemoteMessage}.
   *
   * <p>This constructor is called reflectively and should not be used.
   *
   * @param instance the object that needs to be serialized into a proto
   */
  public GenericRemoteMessage(@NonNull Object instance) {
    this(
        checkNotNull(instance, "instance cannot be null!"), RemoteDescriptorRegistry.getInstance());
  }

  @VisibleForTesting
  GenericRemoteMessage(Object instance, RemoteDescriptorRegistry remoteDescriptorRegistry) {
    this(new RemoteMessageSerializer(instance, remoteDescriptorRegistry));
  }

  private GenericRemoteMessage(RemoteMessageSerializer remoteMessageSerializer) {
    this.remoteMessageSerializer = remoteMessageSerializer;
  }

  /** {@inheritDoc} */
  @Override
  public MessageLite toProto() {
    return remoteMessageSerializer.toProto();
  }

  private static RemoteDescriptorRegistry remoteDescriptorRegistry =
      RemoteDescriptorRegistry.getInstance();

  /** This is used to create and deserialize a proto message into an instance type */
  public static final EspressoRemoteMessage.From<Object, MessageLite> FROM =
      new EspressoRemoteMessage.From<Object, MessageLite>() {
        /** {@inheritDoc} */
        @Override
        public Object fromProto(@NonNull MessageLite messageLite) {
          checkNotNull(messageLite, "messageLite cannot be null!");
          return new RemoteMessageDeserializer(remoteDescriptorRegistry).fromProto(messageLite);
        }
      };

  /**
   * Overrides the default {@link RemoteDescriptorRegistry} to use a custom registry for testing.
   *
   * @param remoteDescriptorRegistry the remote descriptor registry to use for proto serialization
   */
  @VisibleForTesting
  static void setRemoteDescriptorRegistry(RemoteDescriptorRegistry remoteDescriptorRegistry) {
    GenericRemoteMessage.remoteDescriptorRegistry = remoteDescriptorRegistry;
  }
}
