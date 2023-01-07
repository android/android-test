/*
 * Copyright (C) 2015 The Android Open Source Project
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
 */

package androidx.test.espresso.web.model;

import static androidx.test.internal.util.Checks.checkState;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

/** Represents the results of a Javascript execution. */
public final class Evaluation implements JSONAble, Parcelable {
  private static final String STATUS_KEY = "status";
  private static final String MESSAGE_KEY = "message";
  private static final String VALUE_KEY = "value";

  private int status;
  private boolean hasMessage;
  private String message;
  private Object value;

  private Evaluation(Builder b) {
    this.status = b.status;
    this.value = b.value;
    this.hasMessage = b.hasMessage;
    this.message = b.message;
  }

  protected Evaluation(Parcel in) {
    readFromParcel(in);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, value, hasMessage, message);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Evaluation) {
      Evaluation other = (Evaluation) o;
      if (other.status == status) {
        if (hasMessage == other.hasMessage) {
          if (hasMessage) {
            return message.equals(other.message);
          } else {
            if (value == null) {
              return other.value == null;
            } else {
              return value.equals(other.value);
            }
          }
        }
      }
    }
    return false;
  }

  public int getStatus() {
    return status;
  }

  @Nullable
  public Object getValue() {
    return value;
  }

  public boolean hasMessage() {
    return hasMessage;
  }

  public String getMessage() {
    checkState(hasMessage);
    return message;
  }

  @Override
  public String toJSONString() {
    try {
      JSONStringer stringer =
          new JSONStringer().object().key(STATUS_KEY).value(status).key(VALUE_KEY);
      if ((value instanceof String)
          || (value instanceof Number)
          || (value instanceof Boolean)
          || (value == null)) {
        stringer.value(value);
      } else {
        String jsonValue = ModelCodec.encode(value);
        stringer.value(new JSONTokener(jsonValue).nextValue());
      }
      stringer.endObject();
      return stringer.toString();
    } catch (JSONException je) {
      throw new RuntimeException(je);
    }
  }

  static final JSONAble.DeJSONFactory DEJSONIZER =
      new JSONAble.DeJSONFactory() {
        @Override
        public Object attemptDeJSONize(Map<String, Object> map) {
          if (map.size() == 2) {
            Object maybeStatus = map.get(STATUS_KEY);
            if (maybeStatus instanceof Integer) {
              Object maybeValue = map.get(VALUE_KEY);
              if (null != maybeValue) {
                Evaluation.Builder builder =
                    new Evaluation.Builder()
                        .setStatus((Integer) maybeStatus)
                        .setValue(maybeValue == JSONObject.NULL ? null : maybeValue);
                if (maybeValue instanceof Map) {
                  Map mapValue = (Map) maybeValue;
                  if (mapValue.size() == 1) {
                    Object maybeMessage = mapValue.get(MESSAGE_KEY);
                    if (maybeMessage instanceof String) {
                      builder.setMessage((String) maybeMessage);
                    } else if (maybeMessage == JSONObject.NULL) {
                      builder.setMessage(null);
                    }
                  }
                }
                return builder.build();
              }
            }
          }
          return null;
        }
      };

  @Override
  public String toString() {
    return String.format(
        Locale.US,
        "Evaluation: status: %d value: %s hasMessage: %s message: %s",
        status,
        value,
        hasMessage,
        message);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeString(ModelCodec.encode(this));
  }

  public void readFromParcel(Parcel in) {
    Evaluation evaluation = ModelCodec.decodeEvaluation(in.readString());
    status = evaluation.status;
    hasMessage = evaluation.hasMessage;
    message = evaluation.message;
    value = evaluation.value;
  }

  public static final Creator<Evaluation> CREATOR =
      new Parcelable.Creator<Evaluation>() {
        @Override
        public Evaluation createFromParcel(Parcel in) {
          return new Evaluation(in);
        }

        @Override
        public Evaluation[] newArray(int size) {
          return new Evaluation[size];
        }
      };

  static class Builder {
    private Object value;
    private int status;
    private boolean hasMessage;
    private String message;

    public Builder setMessage(String message) {
      this.message = message;
      hasMessage = true;
      return this;
    }

    public Builder setValue(Object value) {
      this.value = value;
      return this;
    }

    public Builder setStatus(int status) {
      this.status = status;
      return this;
    }

    public Evaluation build() {
      return new Evaluation(this);
    }
  }
}
