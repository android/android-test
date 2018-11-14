package androidx.test.internal.platform.content;

import android.os.Build;
import android.support.annotation.NonNull;

/**
 * Requests a runtime permission.
 *
 * <p>Note: This class should not be used directly, but through {@link
 * androidx.test.rule.GrantPermissionRule}.
 *
 */
public interface PermissionGranter {

  /**
   * Adds a permission to the list of permissions which will be requested when {@link
   * #requestPermissions()} is called.
   *
   * <p>Precondition: This method does nothing when called on an API level lower than {@link
   * Build.VERSION_CODES#M}.
   *
   * @param permissions a list of Android runtime permissions.
   */
  void addPermissions(@NonNull String... permissions);

  /**
   * Request all permissions previously added using {@link #addPermissions(String...)}
   *
   * <p>Precondition: This method does nothing when called on an API level lower than {@link
   * Build.VERSION_CODES#M}.
   */
  void requestPermissions();
}
