/*
 * Copyright (C) 2016 The Android Open Source Project
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

package androidx.test.ui.app;

import static com.google.common.base.Preconditions.checkNotNull;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Runtime Permissions Activity that request {@link Manifest.permission#READ_PHONE_STATE
 * READ_PHONE_STATE}
 */
public class RuntimePermissionsActivity extends AppCompatActivity {

  private static final int REQUEST_CODE_READ_PHONE_STATE = 1;
  private static final int REQUEST_CODE_GET_ACCOUNTS = 2;

  private TextView mPhoneStatePermissionStatus;
  private TextView mGetAccountsPermissionStatus;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_runtime_permissions);

    // Set up the toolbar.
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitleTextColor(Color.WHITE);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mPhoneStatePermissionStatus = (TextView) findViewById(
        R.id.phone_state_permission_permission_status);
    mGetAccountsPermissionStatus = (TextView) findViewById(R.id.get_accounts_permission_status);
  }

  public void requestPhoneStatePermission(View view) {
    checkPermissionOrRequest(Manifest.permission.READ_PHONE_STATE, REQUEST_CODE_READ_PHONE_STATE,
        new Runnable() {
          @Override
          public void run() {
            updateReadPhoneStatePermissionsGranted();
          }
        });
  }

  public void requestAccountsPermission(View view) {
    checkPermissionOrRequest(Manifest.permission.GET_ACCOUNTS, REQUEST_CODE_GET_ACCOUNTS,
        new Runnable() {
          @Override
          public void run() {
            updateGetAccountsPermissionsGranted();
          }
        });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (REQUEST_CODE_READ_PHONE_STATE == requestCode) {
      if (1 == grantResults.length && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
        // Read phone state permission has been granted
        updateReadPhoneStatePermissionsGranted();
      } else {
        updateReadPhoneStatePermissionsRevoked();
      }
    } else if (REQUEST_CODE_GET_ACCOUNTS == requestCode) {
      if (1 == grantResults.length && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
        // Read phone state permission has been granted
        updateGetAccountsPermissionsGranted();
      } else {
        updateGetAccountsPermissionsRevoked();
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  private boolean checkPermissionOrRequest(String permission, int requestCode, Runnable
      requestFunction) {
    if (PackageManager.PERMISSION_GRANTED
        == ContextCompat.checkSelfPermission(this, permission)) {
      requestFunction.run();
      return true;
    } else {
      ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
      return false;
    }
  }

  /**
   * This method purely serves the purpose of using an API which is guarded by a dangerous runtime
   * permission. In this case {@link TelephonyManager#getDeviceId()} requires {@link
   * Manifest.permission#READ_PHONE_STATE}
   */
  private void getDeviceId() {
    ((TelephonyManager) getSystemService(RuntimePermissionsActivity.TELEPHONY_SERVICE))
        .getDeviceId();
  }

  private void updateReadPhoneStatePermissionsGranted() {
    getDeviceId();
    updateGrantedUi(mPhoneStatePermissionStatus);
  }

  private void updateReadPhoneStatePermissionsRevoked() {
    updateRevokedUi(mPhoneStatePermissionStatus);
  }

  /**
   * This method purely serves the purpose of using an API which is guarded by a dangerous runtime
   * permission. In this case {@link AccountManager#getAccounts()} requires {@link
   * Manifest.permission#GET_ACCOUNTS}
   */
  private void getAccounts() {
    ((AccountManager) getSystemService(RuntimePermissionsActivity.ACCOUNT_SERVICE))
        .getAccounts();
  }

  private void updateGetAccountsPermissionsGranted() {
    getAccounts();
    updateGrantedUi(mGetAccountsPermissionStatus);
  }

  private void updateGetAccountsPermissionsRevoked() {
    updateRevokedUi(mGetAccountsPermissionStatus);
  }

  private void updateGrantedUi(@NonNull TextView textView) {
    checkNotNull(textView, "textView cannot be null!");
    textView.setVisibility(View.VISIBLE);
    textView.setText(getString(R.string.permissionGranted));
  }

  private void updateRevokedUi(@NonNull TextView textView) {
    checkNotNull(textView, "textView cannot be null!");
    textView.setVisibility(View.VISIBLE);
    textView.setText(getString(R.string.permissionRevoked));
  }
}
