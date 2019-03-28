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

import static androidx.test.espresso.remote.InteractionResponse.Status.Ok;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import androidx.test.espresso.proto.UiInteraction.Error;
import androidx.test.espresso.proto.UiInteraction.InteractionResultProto;
import androidx.test.espresso.remote.EspressoRemoteMessage.To;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * Encapsulates a {@link InteractionResultProto} request. Takes care of all the proto packing and
 * unpacking.
 */
public final class InteractionResponse implements To<MessageLite> {

  /**
   * This field is used to create an instance of {@link InteractionResponse} from its unwrapped
   * proto message.
   */
  private static final EspressoRemoteMessage.From<InteractionResponse, InteractionResultProto>
      FROM =
          new EspressoRemoteMessage.From<InteractionResponse, InteractionResultProto>() {
            @Override
            public InteractionResponse fromProto(InteractionResultProto interactionResultProto) {
              InteractionResultProto resultProto = interactionResultProto;
              // Get status code
              boolean status = resultProto.getOk();

              // Get remote error if any
              RemoteError remoteError = null;
              if (resultProto.hasErrorMsg()) {
                Error errorMsg = resultProto.getErrorMsg();
                int errorCode = errorMsg.getCode();
                String errorDescription = errorMsg.getDescription();
                if (RemoteError.isWellKnownError(errorCode)) {
                  remoteError =
                      new RemoteError(
                          errorCode,
                          RemoteError.getWellKnownFormattedErrorDescription(
                              errorCode, errorDescription));
                } else {
                  remoteError = new RemoteError(errorCode, errorDescription);
                }
              }
              return new InteractionResponse.Builder()
                  .setStatus(status ? Status.Ok : Status.Error)
                  .setRemoteError(remoteError)
                  .build();
            }
          };

  private final Status status;
  @Nullable private final RemoteError remoteError;

  @VisibleForTesting
  InteractionResponse(Status status, RemoteError remoteError) {
    this.status = status;
    this.remoteError = remoteError;
  }

  private InteractionResponse(Builder builder) {
    this(builder.status, builder.remoteError);
  }

  /** @return true if a {@link RemoteError} has been set on this {@link InteractionResponse} */
  public boolean hasRemoteError() {
    return remoteError != null;
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked") // safe covariant cast
  public MessageLite toProto() {
    InteractionResultProto.Builder builder =
        InteractionResultProto.newBuilder().setOk(Ok == status);
    if (remoteError != null) {
      builder.setErrorMsg(
          Error.newBuilder()
              .setCode(remoteError.getCode())
              .setDescription(remoteError.getDescription()));
    }
    return builder.build();
  }

  /** @return the {@link Status} of this {@link InteractionResponse} */
  public Status getStatus() {
    return status;
  }

  /** @return the {@link Error} of this {@link InteractionResponse} */
  public RemoteError getRemoteError() {
    return remoteError;
  }

  /**
   * The status of this interaction response. Can either be {@link Status#Ok} or {@link
   * Status#Error}
   */
  public enum Status {
    Ok,
    Error
  }

  /**
   * TODO(b/31122396): parse this from a proto Enum representing wire protocol error codes and their
   * default description.
   */
  public static final class RemoteError {
    private static final String TAG = "RemoteError";

    public static final int REMOTE_ESPRESSO_ERROR_CODE = 0;
    public static final int REMOTE_PROTOCOL_ERROR_CODE = 1;

    private static final RemoteError REMOTE_ESPRESSO_ERROR =
        new RemoteError(
            REMOTE_ESPRESSO_ERROR_CODE,
            "The following remote Espresso exception with " + "error code [%s] occurred:\n%s");
    private static final RemoteError REMOTE_PROTOCOL_ERROR =
        new RemoteError(
            REMOTE_PROTOCOL_ERROR_CODE,
            "The following remote protocol Espresso "
                + "exception with error code [%s] occurred:\n%s");

    // Reverse-lookup for getting a RemoteError from an error code
    private static final SparseArray<RemoteError> lookup = new SparseArray<>();

    static {
      for (RemoteError e : Arrays.asList(REMOTE_ESPRESSO_ERROR, REMOTE_PROTOCOL_ERROR)) {
        lookup.put(e.getCode(), e);
      }
    }

    private final int code;
    private final String description;

    RemoteError(int code, String description) {
      this.code = code;
      this.description = description;
    }

    /** @return the description of this {@link RemoteError} instance. */
    public String getDescription() {
      return description;
    }

    /** @return the error code of this {@link RemoteError} instance. */
    public int getCode() {
      return code;
    }

    @Override
    public String toString() {
      return code + ": " + description;
    }

    private static String formatDescription(
        String description, int errorCode, String detailedError) {
      checkState(!TextUtils.isEmpty(description), "description cannot be empty!");
      if (detailedError != null) {
        try {
          description = String.format(Locale.ROOT, description, errorCode, detailedError);
        } catch (IllegalFormatException ife) {
          Log.w(TAG, "Cannot format remote error description: " + description);
        }
      }
      return description;
    }

    private static boolean isWellKnownError(int errorCode) {
      return lookup.get(errorCode) != null;
    }

    private static RemoteError getWellKnownError(int errorCode) {
      return lookup.get(errorCode);
    }

    /**
     * Formats a well known Espresso remote error description with a detailed error message.
     *
     * <p>This method allows to provide a formatted error message using the specified format error
     * description and the detailed error message passed as the second parameter. In order to use
     * this feature the well known error description must define a string format in the form of:
     *
     * <pre>{@code
     * new RemoteError(1, "Some remote error occured: [%s]");
     *
     * }</pre>
     *
     * @param errorCode the error code to use as a key for the lookup
     * @param detailedError the detailed error to add to the error description
     * @return formatted error message
     */
    static String getWellKnownFormattedErrorDescription(int errorCode, String detailedError) {
      checkState(isWellKnownError(errorCode));
      RemoteError remoteError = getWellKnownError(errorCode);
      return formatDescription(remoteError.getDescription(), errorCode, detailedError);
    }
  }

  /** Builder for {@link InteractionResponse} */
  public static class Builder {
    private Status status;
    @Nullable private RemoteError remoteError;
    private byte[] interactionResultProtoByteArray;

    public Builder setStatus(@NonNull Status status) {
      this.status = checkNotNull(status);
      return this;
    }

    /**
     * Set the {@link RemoteError} for this {@link InteractionResponse}
     *
     * @param remoteError the remote error to set
     * @return fluent interface {@link Builder}
     */
    public Builder setRemoteError(@Nullable RemoteError remoteError) {
      this.remoteError = remoteError;
      return this;
    }

    /**
     * Set the result proto as a byte array. This byte array will be parsed into an {@link
     * InteractionResultProto}. Providing an invalid byte byte array will result in a {@link
     * RemoteProtocolException} when the {@link #build()} method is called!
     *
     * @param protoByteArray the proto byte array to set
     * @return fluent interface {@link Builder}
     */
    public Builder setResultProto(@NonNull byte[] protoByteArray) {
      this.interactionResultProtoByteArray =
          checkNotNull(protoByteArray, "protoByteArray cannot be" + "null!");
      return this;
    }

    /**
     * Builds an {@link InteractionResponse} object.
     *
     * @throws RemoteProtocolException when conflicting properties are set. You can either set an
     *     {@link Status} with an optional {@link RemoteError} error in case that the status is
     *     {@link Status#Error} or set the proto byte array not both. Setting both values would
     *     result in an override, therefore setting both properties will result in an exception.
     * @throws RemoteProtocolException when the provided proto byte array cannot be parsed into a
     *     protocol buffer of type {@link InteractionResultProto}
     * @return an {@link InteractionResponse} object.
     */
    public InteractionResponse build() {
      if (status != null || remoteError != null) {
        if (interactionResultProtoByteArray != null) {
          throw new RemoteProtocolException(
              "Instances can either be create from an status and "
                  + "optional remote error or an interaction result proto byte array but not both!");
        }
      }

      if (interactionResultProtoByteArray != null) {
        InteractionResultProto interactionResultProto;
        try {
          interactionResultProto =
              InteractionResultProto.parseFrom(interactionResultProtoByteArray);
        } catch (InvalidProtocolBufferException ipbe) {
          throw new RemoteProtocolException("Cannot parse interactionResultProto", ipbe);
        }
        return InteractionResponse.FROM.fromProto(interactionResultProto);
      }

      return new InteractionResponse(this);
    }
  }
}
