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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.view.View;
import androidx.test.espresso.Root;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.proto.UiInteraction.InteractionRequestProto;
import androidx.test.espresso.proto.UiInteraction.InteractionRequestProto.ActionOrAssertionCase;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import org.hamcrest.Matcher;

/**
 * Encapsulates an {@link InteractionRequestProto} request. Takes care of all the proto packing and
 * unpacking.
 */
public final class InteractionRequest implements EspressoRemoteMessage.To<MessageLite> {

  /**
   * This field is used to create an instance of {@link InteractionRequest} from its unwrapped proto
   * message.
   */
  private static final EspressoRemoteMessage.From<InteractionRequest, InteractionRequestProto>
      FROM =
          new EspressoRemoteMessage.From<InteractionRequest, InteractionRequestProto>() {
            @Override
            public InteractionRequest fromProto(InteractionRequestProto interactionRequestProto) {
              Builder interactionRequestBuilder = new Builder();
              interactionRequestBuilder
                  .setRootMatcher(
                      TypeProtoConverters.<Matcher<Root>>anyToType(
                          interactionRequestProto.getRootMatcher()))
                  .setViewMatcher(
                      TypeProtoConverters.<Matcher<View>>anyToType(
                          interactionRequestProto.getViewMatcher()));

              int actionOrAssertionCaseNumber =
                  interactionRequestProto.getActionOrAssertionCase().getNumber();

              if (ActionOrAssertionCase.VIEW_ACTION.getNumber() == actionOrAssertionCaseNumber) {
                interactionRequestBuilder.setViewAction(
                    TypeProtoConverters.<ViewAction>anyToType(
                        interactionRequestProto.getViewAction()));
              }

              if (ActionOrAssertionCase.VIEW_ASSERTION.getNumber() == actionOrAssertionCaseNumber) {
                interactionRequestBuilder.setViewAssertion(
                    TypeProtoConverters.<ViewAssertion>anyToType(
                        interactionRequestProto.getViewAssertion()));
              }
              return interactionRequestBuilder.build();
            }
          };

  @Nullable private final Matcher<Root> rootMatcher;
  @Nullable private final Matcher<View> viewMatcher;
  @Nullable private final ViewAction viewAction;
  @Nullable private final ViewAssertion viewAssertion;

  @VisibleForTesting
  InteractionRequest(
      @Nullable Matcher<Root> rootMatcher,
      @Nullable Matcher<View> viewMatcher,
      @Nullable ViewAction viewAction,
      @Nullable ViewAssertion viewAssertion) {
    this.rootMatcher = rootMatcher;
    this.viewMatcher = viewMatcher;
    this.viewAction = viewAction;
    this.viewAssertion = viewAssertion;
  }

  private InteractionRequest(Builder builder) {
    this(builder.rootMatcher, builder.viewMatcher, builder.viewAction, builder.viewAssertion);
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked") // safe covariant cast
  public MessageLite toProto() {
    try {
      InteractionRequestProto.Builder interactionRequestBuilder =
          InteractionRequestProto.newBuilder();
      interactionRequestBuilder.setRootMatcher(TypeProtoConverters.typeToAny(rootMatcher));
      if (viewMatcher != null) {
        interactionRequestBuilder.setViewMatcher(TypeProtoConverters.typeToAny(viewMatcher));
      }

      if (viewAction != null) {
        interactionRequestBuilder.setViewAction(TypeProtoConverters.typeToAny(viewAction));
      }

      if (viewAssertion != null) {
        interactionRequestBuilder.setViewAssertion(TypeProtoConverters.typeToAny(viewAssertion));
      }
      return interactionRequestBuilder.build();
    } catch (ClassCastException cce) {
      throw new RemoteProtocolException(
          "Type does not implement the EspressoRemoteMessage.TO interface", cce);
    }
  }

  /**
   * Returns the {@link Matcher<Root>} associated with this {@link InteractionRequest} or {@code
   * null} if no {@link Matcher<Root>} was set.
   */
  public Matcher<Root> getRootMatcher() {
    return rootMatcher;
  }

  /**
   * Returns the {@link Matcher<View>} associated with this {@link InteractionRequest} or {@code
   * null} if no view matcher was set.
   */
  public Matcher<View> getViewMatcher() {
    return viewMatcher;
  }

  /**
   * Returns the {@link ViewAction} associated with this {@link InteractionRequest} or {@code null}
   * if no {@link ViewAction} was set.
   */
  public ViewAction getViewAction() {
    return viewAction;
  }

  /**
   * Returns the {@link ViewAssertion} associated with this {@link InteractionRequest} or {@code
   * null} if no {@link ViewAssertion} was set.
   */
  public ViewAssertion getViewAssertion() {
    return viewAssertion;
  }

  /**
   * Creates an instance of {@link InteractionRequest} from a View matcher and action.
   *
   * @return remote request object
   */
  public static class Builder {
    private final RemoteDescriptorRegistry remoteDescriptorRegistry;
    private Matcher<Root> rootMatcher;
    private Matcher<View> viewMatcher;
    private ViewAction viewAction;
    private ViewAssertion viewAssertion;
    private byte[] interactionRequestProtoByteArray;

    /** Creates a new {@link Builder} instance. */
    public Builder() {
      remoteDescriptorRegistry = RemoteDescriptorRegistry.getInstance();
    }

    /**
     * Sets the root matcher for this {@link InteractionRequest}
     *
     * @see Root
     * @param rootMatcher the root matcher to set
     * @return fluent interface
     */
    public Builder setRootMatcher(@NonNull Matcher<Root> rootMatcher) {
      this.rootMatcher = checkNotNull(rootMatcher);
      checkArgument(
          remoteDescriptorRegistry.hasArgForInstanceType(rootMatcher.getClass()),
          "No RemoteDescriptor registered for ViewMatcher: %s",
          rootMatcher);
      return this;
    }

    /**
     * Sets the view matcher for this {@link InteractionRequest}
     *
     * @param viewMatcher the view matcher to set
     * @return fluent interface
     */
    public Builder setViewMatcher(@NonNull Matcher<View> viewMatcher) {
      this.viewMatcher = checkNotNull(viewMatcher);
      checkArgument(
          remoteDescriptorRegistry.hasArgForInstanceType(viewMatcher.getClass()),
          "No RemoteDescriptor registered for ViewMatcher: %s",
          viewMatcher);
      return this;
    }

    /**
     * Sets the {@link ViewAction} for this {@link InteractionRequest}
     *
     * @param viewAction the view action to set
     * @return fluent interface
     * @throws IllegalStateException if a {@link ViewAssertion} was already set through {@link
     *     InteractionRequest.Builder#setViewAssertion(ViewAssertion)} before. {@link
     *     InteractionRequest} supports only one of {@link ViewAction} or {@link ViewAssertion}, but
     *     not both.
     */
    public Builder setViewAction(@NonNull ViewAction viewAction) {
      this.viewAction = checkNotNull(viewAction);
      checkArgument(
          remoteDescriptorRegistry.hasArgForInstanceType(viewAction.getClass()),
          "No RemoteDescriptor registered for ViewAction: %s",
          viewAction);
      return this;
    }

    /**
     * Sets the {@link ViewAssertion} for this {@link InteractionRequest}
     *
     * @param viewAssertion the view action to set
     * @return fluent interface
     * @throws IllegalStateException if a {@link ViewAction} was already set through {@link
     *     InteractionRequest.Builder#setViewAction(ViewAction)} before. {@link InteractionRequest}
     *     supports only one of {@link ViewAction} or {@link ViewAssertion}, but not both.
     */
    public Builder setViewAssertion(@NonNull ViewAssertion viewAssertion) {
      this.viewAssertion = checkNotNull(viewAssertion);
      checkArgument(
          remoteDescriptorRegistry.hasArgForInstanceType(viewAssertion.getClass()),
          "No RemoteDescriptor registered for ViewAssertion: %s",
          viewAssertion);
      return this;
    }

    /**
     * Set the result proto as a byte array. This byte array will be parsed into an {@link
     * InteractionRequestProto}. Providing an invalid byte array will result in a {@link
     * RemoteProtocolException} when the {@link #build()} method is called!
     *
     * @param protoByteArray the proto byte array to set
     * @return fluent interface {@link Builder}
     */
    public Builder setRequestProto(@NonNull byte[] protoByteArray) {
      this.interactionRequestProtoByteArray =
          checkNotNull(protoByteArray, "protoByteArray cannot be null!");
      return this;
    }

    /**
     * Builds an {@link InteractionRequest} object.
     *
     * @return an {@link InteractionRequest} object.
     * @throws IllegalStateException when conflicting properties are set. You can either set a
     *     {@link Matcher<View>} and a {@link ViewAction} or set the proto byte array but not both.
     *     Setting all values would result in an override, therefore setting both properties will
     *     result in an exception.
     * @throws RemoteProtocolException when the provided proto byte array cannot be parsed into a
     *     protocol buffer of type {@link InteractionRequestProto}
     */
    public InteractionRequest build() {
      if (viewAction != null && viewAssertion != null) {
        throw new IllegalStateException(
            "View Action and Assertion set. Either set a View Action "
                + "or a View Assertion but not both at the same time!");
      }

      if (viewMatcher != null || viewAction != null || viewAssertion != null) {
        if (interactionRequestProtoByteArray != null) {
          throw new IllegalStateException(
              "Instances can either be create from an view matcher. "
                  + "view action and assertion or an interaction request proto byte array but not "
                  + "both!");
        }
      }

      if (interactionRequestProtoByteArray != null) {
        InteractionRequestProto interactionRequestProto;
        try {
          interactionRequestProto =
              InteractionRequestProto.parseFrom(interactionRequestProtoByteArray);
        } catch (InvalidProtocolBufferException ipbe) {
          throw new RemoteProtocolException("Cannot parse interactionResultProto", ipbe);
        }
        return InteractionRequest.FROM.fromProto(interactionRequestProto);
      }

      checkState(
          rootMatcher != null,
          "root matcher is mandatory and needs to be set using:"
              + "Builder.setRootMatcher(Matcher)");

      return new InteractionRequest(this);
    }
  }
}
