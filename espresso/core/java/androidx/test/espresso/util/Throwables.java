package androidx.test.espresso.util;

import static androidx.test.internal.util.Checks.checkNotNull;

import androidx.annotation.RestrictTo;

/**
 * Re-implementation of needed methods from Guava's Throwables to avoid the direct dependency
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Throwables {

  private Throwables() {}

  /**
   * Re-throw the exception if its a {@link RuntimeException} or {@link Error}. Otherwise ignore.
   *
   * @param throwable the exception
   */
  public static void throwIfUnchecked(Throwable throwable) {
    checkNotNull(throwable);
    if (throwable instanceof RuntimeException) {
      throw (RuntimeException) throwable;
    }
    if (throwable instanceof Error) {
      throw (Error) throwable;
    }
  }
}
