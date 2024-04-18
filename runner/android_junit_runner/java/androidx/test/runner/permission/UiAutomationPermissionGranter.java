package androidx.test.runner.permission;

import android.app.UiAutomation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A {@link PermissionGranter} that uses {@link
 * android.app.UiAutomation#grantRuntimePermission(String, String)}
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
@RequiresApi(28)
public class UiAutomationPermissionGranter implements PermissionGranter {
  private static final String TAG = "UiAutomationPermGranter";
  private final Set<String> permissionsToGrant = new LinkedHashSet<>();

  @Override
  public void addPermissions(@NonNull String... permissions) {
    Collections.addAll(permissionsToGrant, permissions);
  }

  @Override
  public void requestPermissions() {
    Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    for (String permission : permissionsToGrant) {
      if (!isPermissionGranted(targetContext, permission)) {
        Log.i(
            TAG,
            String.format(
                "Attempting to grant %s to %s", permission, targetContext.getPackageName()));
        uiAutomation.grantRuntimePermission(targetContext.getPackageName(), permission);
      } else {
        Log.i(TAG, "Permission: " + permission + " is already granted!");
      }
    }
  }

  private boolean isPermissionGranted(Context targetContext, String permission) {
    return targetContext.checkCallingOrSelfPermission(permission)
        == PackageManager.PERMISSION_GRANTED;
  }
}
