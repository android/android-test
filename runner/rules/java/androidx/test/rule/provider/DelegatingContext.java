/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Lice`nse is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.rule.provider;

import static androidx.test.internal.util.Checks.checkArgument;
import static androidx.test.internal.util.Checks.checkNotNull;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * This {@code DelegatingContext} renames files with a configurable prefix for file and database
 * operations. It wraps in a {@link ContentResolver} and delegates the supported methods for the
 * {@link Context} passed in when creating the {@code DelegatingContext}. The supported methods
 * include:
 *
 * <ul>
 *   <li>{@link Context#getResources()}
 *   <li>{@link Context#getAssets()}
 * </ul>
 */
class DelegatingContext extends ContextWrapper {

  private static final String TAG = "DelegatingContext";
  private static final int NO_OP_UID = -1;
  private static final int NO_OP_PID = -1;

  private final String prefix;
  private final Context context;
  private final ContentResolver contentResolver;
  private Set<String> databases = new HashSet<>();
  private Set<String> files = new HashSet<>();
  private Set<String> revokedPermissions = new HashSet<>();

  /**
   * Constructor of the {@code DelegatingContext} with the {@link Context} to be delegated, a
   * specific prefix to use when renaming files and databases, and the {@link ContentResolver} to
   * wrap in.
   *
   * @param context The {@link Context} to be delegated by this {@code DelegatingContext}.
   * @param prefix The prefix to use when renaming files and databases.
   * @param contentResolver The {@link ContentResolver} made available in this {@code
   *     DelegatingContext}.
   */
  public DelegatingContext(
      @NonNull Context context, @NonNull String prefix, @NonNull ContentResolver contentResolver) {
    super(checkNotNull(context));
    this.context = context;
    this.prefix = checkNotNull(prefix);
    this.contentResolver = checkNotNull(contentResolver);
  }

  /**
   * @return The {@link ContentResolver} wrapped in when creating current {@code DelegatingContext}.
   */
  @Override
  public ContentResolver getContentResolver() {
    return contentResolver;
  }

  @Override
  public File getDir(@NonNull String name, int mode) {
    checkArgument(!TextUtils.isEmpty(name), "Directory name cannot be empty or null");
    return context.getDir(getPrefixName(name), mode);
  }

  /**
   * Open an existing database with a specific name, or create it if the database is not visible in
   * the current {@link DelegatingContext}. The database is renamed using the prefix.
   */
  @Override
  public SQLiteDatabase openOrCreateDatabase(
      @NonNull String name, int mode, CursorFactory factory) {
    checkArgument(!TextUtils.isEmpty(name), "Database name cannot be empty or null");
    if (!databases.contains(name)) {
      addDatabase(name);
      String prefixName = getPrefixName(name);
      if (context.getDatabasePath(prefixName).exists() && !context.deleteDatabase(prefixName)) {
        Log.w(
            TAG,
            "Database with prefixed name " + prefixName + " already exists but failed to delete.");
      }
    }
    return context.openOrCreateDatabase(getPrefixName(name), mode, factory);
  }

  /**
   * Similar to {@link #openOrCreateDatabase(String, int, CursorFactory)}, but also allows to pass
   * in a {@code DatabaseErrorHandler}.
   */
  @Override
  public SQLiteDatabase openOrCreateDatabase(
      @NonNull String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
    checkArgument(!TextUtils.isEmpty(name), "Database name cannot be empty or null");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      String prefixName = getPrefixName(name);
      if (!databases.contains(name)) {
        addDatabase(name);
        if (context.getDatabasePath(prefixName).exists() && !context.deleteDatabase(prefixName)) {
          Log.w(
              TAG,
              "Database with prefixed name "
                  + prefixName
                  + " already exists and cannot be deleted.");
        }
      }
      return context.openOrCreateDatabase(prefixName, mode, factory, errorHandler);
    }
    throw new UnsupportedOperationException(
        "For API level < 11, use openOrCreateDatabase(String, int, CursorFactory) instead");
  }

  /**
   * @return The list of database names (without prefix) visible to this {@link DelegatingContext}.
   */
  @Override
  public String[] databaseList() {
    return databases.toArray(new String[databases.size()]);
  }

  /**
   * Delete a database from this {@link DelegatingContext}.
   *
   * @param name The name (without prefix) of database to delete.
   * @return {@code true} if database was visible and successfully deleted, and {@code false}
   *     otherwise.
   */
  @Override
  public boolean deleteDatabase(@NonNull String name) {
    checkArgument(!TextUtils.isEmpty(name), "Database name cannot be empty or null");
    if (databases.contains(name)) {
      if (context.deleteDatabase(getPrefixName(name))) {
        databases.remove(name);
        return true;
      }
    }
    return false;
  }

  @Override
  public File getDatabasePath(@NonNull String name) {
    checkArgument(!TextUtils.isEmpty(name), "Database name cannot be empty or null");
    return context.getDatabasePath(getPrefixName(name));
  }

  /**
   * Create {@link FileInputStream} from a file name visible in current {@link DelegatingContext}.
   *
   * @param name The name of the file (without prefix) to open.
   * @return The created {@link FileInputStream}.
   * @throws FileNotFoundException if the name of file is not visible in current {@link
   *     DelegatingContext}.
   */
  @Override
  public FileInputStream openFileInput(@NonNull String name) throws FileNotFoundException {
    checkArgument(!TextUtils.isEmpty(name), "File name cannot be empty or null");
    if (!files.contains(name)) {
      throw new FileNotFoundException(
          String.format("File %s is not found in current context", name));
    }
    return context.openFileInput(getPrefixName(name));
  }

  /**
   * Create {@link FileOutputStream} from a file name visible in current {@link DelegatingContext}.
   * If the file was not visible, add the file name to the file lists visible in current {@link
   * DelegatingContext}.
   *
   * @param name The name of the file (without prefix) to open.
   * @param mode Same mode as that in {@link Context#openFileOutput}.
   * @return The created {@link FileOutputStream}.
   */
  @Override
  public FileOutputStream openFileOutput(@NonNull String name, int mode)
      throws FileNotFoundException {
    checkArgument(!TextUtils.isEmpty(name), "File name cannot be empty or null");
    FileOutputStream fos = context.openFileOutput(getPrefixName(name), mode);
    if (fos != null) {
      files.add(name);
    }
    return fos;
  }

  @Override
  public String[] fileList() {
    return files.toArray(new String[files.size()]);
  }

  @Override
  public File getFileStreamPath(@NonNull String name) {
    checkArgument(!TextUtils.isEmpty(name), "File name cannot be empty or null");
    return context.getFileStreamPath(getPrefixName(name));
  }

  /**
   * Delete a file from this {@link DelegatingContext}.
   *
   * @param name The name (without prefix) of database to delete.
   * @return {@code true} if file was visible and successfully deleted, and {@code false} otherwise.
   */
  @Override
  public boolean deleteFile(@NonNull String name) {
    checkArgument(!TextUtils.isEmpty(name), "File name cannot be empty or null");
    if (files.contains(name)) {
      if (context.deleteFile(getPrefixName(name))) {
        files.remove(name);
        return true;
      }
    }
    return false;
  }

  /**
   * This method only supports retrieving {@link android.app.AppOpsManager}, which is needed by
   * {@link android.content.ContentProvider#attachInfo}.
   */
  @Override
  public Object getSystemService(@NonNull String name) {
    checkArgument(!TextUtils.isEmpty(name), "name cannot be empty or null");
    // getSystemService(Context.APP_OPS_SERVICE) is only used in ContentProvider#attachInfo for
    // API level >= 19.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        && Context.APP_OPS_SERVICE.equals(name)) {
      return context.getSystemService(Context.APP_OPS_SERVICE);
    }
    throw new UnsupportedOperationException();
  }

  /**
   * The return value only depends on {@link #revokedPermissions}, and the argument {@code pid} and
   * {@code uid} are ignored.
   */
  @Override
  public int checkPermission(@NonNull String permission, int pid, int uid) {
    checkArgument(!TextUtils.isEmpty(permission), "permission cannot be null or empty");
    if (revokedPermissions.contains(permission)) {
      return PackageManager.PERMISSION_DENIED;
    }
    return PackageManager.PERMISSION_GRANTED;
  }

  @Override
  public int checkCallingPermission(@NonNull String permission) {
    return checkPermission(permission, NO_OP_PID, NO_OP_UID);
  }

  @Override
  public int checkCallingOrSelfPermission(@NonNull String permission) {
    return checkPermission(permission, NO_OP_PID, NO_OP_UID);
  }

  @Override
  public int checkSelfPermission(@NonNull String permission) {
    return checkPermission(permission, NO_OP_PID, NO_OP_UID);
  }

  @Override
  public void enforcePermission(@NonNull String permission, int pid, int uid, String message) {
    if (checkPermission(permission, pid, uid) != PackageManager.PERMISSION_GRANTED) {
      throw new SecurityException(
          (message != null ? (message + ": ") : "") + "No permission " + permission);
    }
  }

  @Override
  public void enforceCallingPermission(@NonNull String permission, String message) {
    enforcePermission(permission, NO_OP_PID, NO_OP_UID, message);
  }

  @Override
  public void enforceCallingOrSelfPermission(@NonNull String permission, String message) {
    enforcePermission(permission, NO_OP_PID, NO_OP_UID, message);
  }

  @Override
  public int checkUriPermission(@NonNull Uri uri, int pid, int uid, int modeFlags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int checkCallingUriPermission(@NonNull Uri uri, int modeFlags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int checkCallingOrSelfUriPermission(@NonNull Uri uri, int modeFlags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int checkUriPermission(
      @Nullable Uri uri,
      @Nullable String readPermission,
      @Nullable String writePermission,
      int pid,
      int uid,
      int modeFlags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void enforceUriPermission(
      @NonNull Uri uri, int pid, int uid, int modeFlags, String message) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void enforceUriPermission(
      Uri uri,
      String readPermission,
      String writePermission,
      int pid,
      int uid,
      int modeFlags,
      String message) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context getApplicationContext() {
    return this;
  }

  @Override
  public File getFilesDir() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File getNoBackupFilesDir() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File getExternalFilesDir(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public File getObbDir() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File[] getObbDirs() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File getCacheDir() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File getCodeCacheDir() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File getExternalCacheDir() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File[] getExternalCacheDirs() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File[] getExternalMediaDirs() {
    throw new UnsupportedOperationException();
  }

  @Override
  public File[] getExternalFilesDirs(String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PackageManager getPackageManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Looper getMainLooper() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTheme(int resID) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Resources.Theme getTheme() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ClassLoader getClassLoader() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPackageName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ApplicationInfo getApplicationInfo() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPackageResourcePath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getPackageCodePath() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendBroadcast(Intent intent, String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendBroadcast(Intent intent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendOrderedBroadcast(
      @NonNull Intent intent,
      String s,
      BroadcastReceiver broadcastReceiver,
      Handler handler,
      int i,
      String s1,
      Bundle bundle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendOrderedBroadcast(Intent intent, String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendOrderedBroadcastAsUser(
      Intent intent,
      UserHandle userHandle,
      String s,
      BroadcastReceiver broadcastReceiver,
      Handler handler,
      int i,
      String s1,
      Bundle bundle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendBroadcastAsUser(Intent intent, UserHandle userHandle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendBroadcastAsUser(Intent intent, UserHandle userHandle, String s) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendStickyBroadcast(Intent intent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendStickyOrderedBroadcast(
      Intent intent,
      BroadcastReceiver broadcastReceiver,
      Handler handler,
      int i,
      String s,
      Bundle bundle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sendStickyOrderedBroadcastAsUser(
      Intent intent,
      UserHandle userHandle,
      BroadcastReceiver broadcastReceiver,
      Handler handler,
      int i,
      String s,
      Bundle bundle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeStickyBroadcast(Intent intent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void revokeUriPermission(Uri uri, int modeFlags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SharedPreferences getSharedPreferences(String name, int mode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Drawable getWallpaper() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Drawable peekWallpaper() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getWallpaperDesiredMinimumHeight() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getWallpaperDesiredMinimumWidth() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setWallpaper(Bitmap bitmap) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setWallpaper(InputStream inputStream) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearWallpaper() throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void startActivity(Intent intent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void startActivity(Intent intent, Bundle bundle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void startActivities(Intent[] intents) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void startActivities(Intent[] intents, Bundle bundle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void startIntentSender(IntentSender intentSender, Intent intent, int i, int i1, int i2)
      throws SendIntentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void startIntentSender(
      IntentSender intentSender, Intent intent, int i, int i1, int i2, Bundle bundle)
      throws SendIntentException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Intent registerReceiver(
      BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, String s, Handler handler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unregisterReceiver(BroadcastReceiver receiver) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ComponentName startService(Intent intent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean stopService(Intent intent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean bindService(Intent service, @NonNull ServiceConnection conn, int flags) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unbindService(@NonNull ServiceConnection serviceConnection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean startInstrumentation(
      @NonNull ComponentName componentName, String s, Bundle bundle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getSystemServiceName(Class<?> aClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context createPackageContext(String packageName, int flags)
      throws PackageManager.NameNotFoundException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context createConfigurationContext(@NonNull Configuration overrideConfiguration) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context createDisplayContext(@NonNull Display display) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isRestricted() {
    throw new UnsupportedOperationException();
  }

  boolean addDatabase(@NonNull String name) {
    checkArgument(!TextUtils.isEmpty(name), "Database name cannot be empty or null");
    return databases.add(name);
  }

  void addRevokedPermission(@NonNull String permission) {
    checkArgument(!TextUtils.isEmpty(permission), "permission cannot be null or empty");
    revokedPermissions.add(permission);
  }

  private String getPrefixName(@NonNull String name) {
    checkArgument(!TextUtils.isEmpty(name), "Name cannot be empty or null");
    return prefix + name;
  }
}
