package androidx.test.internal.platform;

import androidx.annotation.RestrictTo;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type loaded by ServiceLoadWrapper is intended as a replacement for
 * another type.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Supersedes {

  /** The type that is superseded by the annotated type. */
  Class<?> value();
}
