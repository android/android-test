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
import static androidx.test.internal.util.Checks.checkState;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import android.test.mock.MockContentResolver;
import android.text.TextUtils;
import android.util.Log;
import androidx.test.annotation.Beta;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} to test {@link ContentProvider}s, with additional APIs to enable easy
 * initialization such as restoring database from a file, running database commands passed in as a
 * String or a file. By default, all permissions are granted when they are checked in
 * ContentProviders under test, method {@link #revokePermission} can be used to revoke specific
 * permissions to test the cases when they are denied in ContentProviders.
 *
 * <p>Note: The database related methods {@link Builder#setDatabaseFile}, {@link
 * Builder#setDatabaseCommands}, {@link Builder#setDatabaseCommandsFile} and {@link
 * #runDatabaseCommands} should only be used when ContentProvider under test is implemented based on
 * {@link SQLiteDatabase}.
 *
 * <p>If more than one database related methods are used for a ContentProvider under test, the
 * execution order of restoring database from file and running database commands are independent of
 * the order those methods are called. If all methods are used, when setting up the ContentProvider
 * for test, the execution order is as follows:
 *
 * <ol>
 *   <li>Restore database from file passed in via {@link Builder#setDatabaseFile}
 *   <li>Run database commands passed in via {@link Builder#setDatabaseCommands}
 *   <li>Run database commands from file passed in via {@link Builder#setDatabaseCommandsFile}
 * </ol>
 *
 * <p>If the {@link ContentProvider} under test is not implemented based on {@link SQLiteDatabase},
 * or is implemented based on {@link SQLiteDatabase} but no extra database initialization workloads
 * are needed, the rule can be created by simply using {@link Builder Builder(Class,String)}.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#064;Rule
 * public ProviderTestRule mProviderRule =
 *     new ProviderTestRule.Builder(MyContentProvider.class, MyContentProvider.AUTHORITY).build();
 *
 * &#064;Test
 * public void verifyContentProviderContractWorks() {
 *     ContentResolver resolver = mProviderRule.getResolver();
 *     // perform some database (or other) operations
 *     Uri uri = resolver.insert(testUrl, testContentValues);
 *     // perform some assertions on the resulting URI
 *     assertNotNull(uri);
 * }
 * </pre>
 *
 * <p>Alternatively, if the {@link ContentProvider} under test is based on {@link SQLiteDatabase},
 * then all database related methods can be used. However, the database name argument passed in via
 * these methods must match the actual database name used by the {@link ContentProvider} under test.
 *
 * <p>Usage example:
 *
 * <pre>
 * &#064;Rule
 * public ProviderTestRule mProviderRule =
 *     new ProviderTestRule.Builder(MyContentProvider.class, MyContentProvider.AUTHORITY)
 *         .setDatabaseCommands(DATABASE_NAME, INSERT_ONE_ENTRY_CMD, INSERT_ANOTHER_ENTRY_CMD)
 *         .build();
 *
 * &#064;Test
 * public void verifyTwoEntriesInserted() {
 *     ContentResolver resolver = mProviderRule.getResolver();
 *     // two entries are already inserted by rule, we can directly perform assertions to verify
 *     Cursor c = null;
 *     try {
 *       c = resolver.query(URI_TO_QUERY_ALL, null, null, null, null);
 *       assertNotNull(c);
 *       assertEquals(2, c.getCount());
 *     } finally {
 *       if (c != null && !c.isClosed()) {
 *         c.close();
 *       }
 *     }
 * }
 * </pre>
 *
 * <p><b>This API is currently in beta.</b>
 */
@Beta
public class ProviderTestRule implements TestRule {

  private static final String TAG = "ProviderTestRule";

  private final Set<WeakReference<ContentProvider>> providersRef;
  private final Set<DatabaseArgs> databaseArgsSet;
  private final ContentResolver resolver;
  private final DelegatingContext context;

  @VisibleForTesting
  ProviderTestRule(
      Set<WeakReference<ContentProvider>> providersRef,
      Set<DatabaseArgs> databaseArgsSet,
      ContentResolver resolver,
      DelegatingContext context) {
    this.providersRef = providersRef;
    this.databaseArgsSet = databaseArgsSet;
    this.resolver = resolver;
    this.context = context;
  }

  /**
   * Get the isolated {@link ContentResolver} that should be used for testing of the
   * ContentProviders.
   *
   * @return the isolated {@link ContentResolver} created by this {@code ProviderTestRule}.
   */
  public ContentResolver getResolver() {
    return resolver;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new ProviderStatement(base);
  }

  /**
   * Run database commands anytime during the tests, after the rule is created.
   *
   * <p>
   *
   * @param dbName The name of the underlying database used by the ContentProvider under test.
   * @param dbCmds The SQL commands to run during tests. Each command will be passed to {@link
   *     SQLiteDatabase#execSQL(String)} to execute.
   */
  public void runDatabaseCommands(@NonNull String dbName, @NonNull String... dbCmds) {
    checkNotNull(dbName);
    checkNotNull(dbCmds);
    if (dbCmds.length > 0) {
      SQLiteDatabase database = context.openOrCreateDatabase(dbName, 0, null);
      for (String cmd : dbCmds) {
        if (!TextUtils.isEmpty(cmd)) {
          try {
            database.execSQL(cmd);
          } catch (SQLiteException e) {
            Log.e(
                TAG,
                String.format(
                    "Error executing sql command %s, possibly wrong "
                        + "or duplicated commands (e.g. same table insertion command without checking "
                        + "current table existence).",
                    cmd));
            throw e;
          }
        }
      }
    }
  }

  /**
   * Revoke permission anytime during the tests. The default return value of the following methods
   * is {@code PackageManager.PERMISSION_GRANTED}. After a specific permission is revoked, the value
   * returned becomes {@code PackageManager.PERMISSION_DENIED} when calling the methods with the
   * revoked permission. After a test, the return values are restored to {@code
   * PackageManager.PERMISSION_GRANTED}.
   *
   * <ul>
   *   <li>{@link Context#checkPermission}
   *   <li>{@link Context#checkCallingPermission}
   *   <li>{@link Context#checkSelfPermission}
   *   <li>{@link Context#checkCallingOrSelfPermission}
   * </ul>
   *
   * Consequentially, calling the following methods with the revoked permission will throw a {@link
   * SecurityException}.
   *
   * <ul>
   *   <li>{@link Context#enforcePermission}
   *   <li>{@link Context#enforceCallingPermission}
   *   <li>{@link Context#enforceCallingOrSelfPermission}
   * </ul>
   */
  public void revokePermission(@NonNull String permission) {
    checkArgument(!TextUtils.isEmpty(permission), "permission cannot be null or empty");
    context.addRevokedPermission(permission);
  }

  /**
   * Override this method to execute any code that should run before provider is set up. This method
   * is called before each test method, including any method annotated with <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/Before.html"><code>Before</code></a>.
   */
  protected void beforeProviderSetup() {
    // empty by default
  }

  /**
   * Override this method to execute any code that should run after provider is cleaned up. This
   * method is called after each test method, including any method annotated with <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/After.html"><code>After</code></a>.
   */
  protected void afterProviderCleanedUp() {
    // empty by default
  }

  private void setUpProviders() throws IOException {
    beforeProviderSetup();
    for (DatabaseArgs databaseArgs : databaseArgsSet) {
      setUpProvider(databaseArgs);
    }
  }

  private void setUpProvider(DatabaseArgs databaseArgs) throws IOException {
    if (databaseArgs.hasDBDataFile()) {
      restoreDBDataFromFile(databaseArgs);
    }
    if (databaseArgs.hasDBCmdFile()) {
      collectDBCmdsFromFile(databaseArgs);
    }
    if (databaseArgs.hasDBCmds()) {
      runDatabaseCommands(databaseArgs.getDBName(), databaseArgs.getDBCmds());
    }
  }

  private void restoreDBDataFromFile(DatabaseArgs databaseArgs) throws IOException {
    File dbDataFile = databaseArgs.getDBDataFile();
    checkState(
        dbDataFile.exists(), String.format("The database file %s doesn't exist!", dbDataFile));

    String dbName = databaseArgs.getDBName();
    copyFile(dbDataFile, context.getDatabasePath(dbName));
    // Add the restored database to the DelegatingContext
    context.addDatabase(dbName);
  }

  private void collectDBCmdsFromFile(DatabaseArgs databaseArgs) throws IOException {
    BufferedReader br = null;
    File dbCmdFile = databaseArgs.getDBCmdFile();
    List<String> cmdsToAdd = new ArrayList<>();

    try {
      br =
          new BufferedReader(
              new InputStreamReader(new FileInputStream(dbCmdFile), Charset.forName("UTF-8")));
      String currentLine;
      while ((currentLine = br.readLine()) != null) {
        if (!TextUtils.isEmpty(currentLine)) {
          cmdsToAdd.add(currentLine);
        }
      }
    } catch (IOException ioe) {
      Log.e(TAG, String.format("Cannot open command file %s to read", dbCmdFile));
      throw ioe;
    } finally {
      if (br != null) {
        br.close();
      }
    }
    databaseArgs.addDBCmds(cmdsToAdd.toArray(new String[cmdsToAdd.size()]));
  }

  private void copyFile(File src, File dest) throws IOException {
    File destParent = dest.getParentFile();
    if (!destParent.exists() && !destParent.mkdirs()) {
      String errorMessage = String.format("error happened creating parent dir for file %s", dest);
      Log.e(TAG, errorMessage);
      throw new IOException(errorMessage);
    }
    FileChannel in = new FileInputStream(src).getChannel();
    FileChannel out = new FileOutputStream(dest).getChannel();
    try {
      in.transferTo(0, in.size(), out);
    } catch (IOException ioe) {
      Log.e(TAG, String.format("error happened copying file from %s to %s", src, dest));
      throw ioe;
    } finally {
      in.close();
      out.close();
    }
  }

  private void cleanUpProviders() {
    // ContentProvider.shutdown() method is added in HONEYCOMB
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      for (WeakReference<ContentProvider> providerRef : providersRef) {
        ContentProvider provider = providerRef.get();
        if (provider != null) {
          provider.shutdown();
        }
      }
    }

    for (DatabaseArgs databaseArgs : databaseArgsSet) {
      String dbName = databaseArgs.getDBName();
      if (dbName != null) {
        context.deleteDatabase(dbName);
      }
    }

    afterProviderCleanedUp();
  }

  /**
   * A Builder to ease {@link ProviderTestRule} creation. Users can input one or more {@link
   * ContentProvider}s and their corresponding {@code authority} for tests. It also allows to
   * specify the prefix to use when renaming test files for isolation by {@link #setPrefix}. If the
   * ContentProvider under test is implemented based on {@link SQLiteDatabase}, users can also pass
   * in database file to restore or database commands to run before tests.
   */
  public static class Builder {

    private static final String DEFAULT_PREFIX = "test.";
    private final Map<String, Class<? extends ContentProvider>> providerClasses = new HashMap<>();
    private final Map<String, DatabaseArgs> databaseArgsMap = new HashMap<>();
    private String prefix = DEFAULT_PREFIX;

    /**
     * The basic builder to use when creating a {@code ProviderTestRule}, which allows to specify
     * one {@link ContentProvider} and the corresponding {@code authority} for tests.
     *
     * @param providerClass The class of ContentProvider under test.
     * @param providerAuth The authority defined for ContentProvider under test.
     */
    public <T extends ContentProvider> Builder(
        @NonNull Class<T> providerClass, @NonNull String providerAuth) {
      checkNotNull(providerClass);
      checkNotNull(providerAuth);
      providerClasses.put(providerAuth, providerClass);
    }

    /**
     * Enables users to specify the prefix to use when renaming test files for isolation. If not
     * used, this rule will use "{@code #DEFAULT_PREFIX}" by default.
     *
     * @param prefix The non-empty prefix to append to test files.
     */
    public Builder setPrefix(@NonNull String prefix) {
      checkArgument(!TextUtils.isEmpty(prefix), "The prefix cannot be null or empty");
      this.prefix = prefix;
      return this;
    }

    /**
     * Allows to pass in a SQLite database file containing the intended initial data to restore.
     *
     * <p>Note: Restoring the database from the file are executed before any sql commands execution
     * in {@code SQLiteOpenHelper}'s {@code onCreate} method defined in ContentProvider under test,
     * also before running the database commands, if any, passed in via {@link #setDatabaseCommands}
     * and {@link #setDatabaseCommandsFile}.
     *
     * <p>In the case of the database file with prefixed name already exists, the database
     * restoration will overwrite existing file.
     *
     * <p>
     *
     * @param dbName The name of the underlying database used by the ContentProvider under test.
     * @param dbDataFile The SQLite database file that contains the data to restore.
     */
    public Builder setDatabaseFile(@NonNull String dbName, @NonNull File dbDataFile) {
      checkNotNull(dbName);
      checkNotNull(dbDataFile);
      getDatabaseArgs(dbName).setDBDataFile(dbDataFile);
      return this;
    }

    /**
     * Allows to pass in specific SQL commands to run against the database with a given name.
     *
     * <p>Note: The passed in commands are executed before any sql commands execution in {@code
     * SQLiteOpenHelper}'s {@code onCreate} method defined in ContentProvider under test, also
     * before executing commands, if any, passed in via {@link #setDatabaseCommandsFile}, but after
     * restoring the database file, if any, passed in via {@link #setDatabaseFile}.
     *
     * <p>
     *
     * @param dbName The name of the underlying database used by the ContentProvider under test.
     * @param dbCmds The SQL commands to run. Each command will be passed to {@link
     *     SQLiteDatabase#execSQL(String)} to execute.
     */
    public Builder setDatabaseCommands(@NonNull String dbName, @NonNull String... dbCmds) {
      checkNotNull(dbName);
      checkNotNull(dbCmds);
      getDatabaseArgs(dbName).setDBCmds(dbCmds);
      return this;
    }

    /**
     * Allows to pass in a file containing commands to run against the database with a given name.
     *
     * <p>Note: Commands in the file are executed before any sql commands execution in {@code
     * SQLiteOpenHelper}'s {@code onCreate} method defined in ContentProvider under test, but after
     * restoring the database file, if any, passed in via {@link #setDatabaseFile} and executing
     * commands, if any, passed in via {@link #setDatabaseCommands}.
     *
     * <p>
     *
     * @param dbName The name of the underlying database used by the ContentProvider under test.
     * @param dbCmdFile The file that contains line separated database commands to run. Each line
     *     will be treated as a separate command and passed to {@link
     *     SQLiteDatabase#execSQL(String)} to execute.
     */
    public Builder setDatabaseCommandsFile(@NonNull String dbName, @NonNull File dbCmdFile) {
      checkNotNull(dbName);
      checkNotNull(dbCmdFile);
      getDatabaseArgs(dbName).setDBCmdFile(dbCmdFile);
      return this;
    }

    /**
     * Allows to add additional ContentProvider and the corresponding authority for testing.
     * Similarly, {@link #setDatabaseFile}, {@link #setDatabaseCommands}, {@link
     * #setDatabaseCommandsFile}, and {@link #runDatabaseCommands} can be used for this
     * ContentProvider.
     *
     * @param providerClass The class of the added ContentProvider under test.
     * @param providerAuth The authority defined for the added ContentProvider under test.
     */
    public <T extends ContentProvider> Builder addProvider(
        @NonNull Class<T> providerClass, @NonNull String providerAuth) {
      checkNotNull(providerClass);
      checkNotNull(providerAuth);
      checkState(providerClasses.size() > 0, "No existing provider yet while trying to add more");
      checkState(
          !providerClasses.containsKey(providerAuth),
          String.format("ContentProvider with authority %s already exists.", providerAuth));
      providerClasses.put(providerAuth, providerClass);
      return this;
    }

    public ProviderTestRule build() {
      Set<WeakReference<ContentProvider>> mProvidersRef = new HashSet<>();
      MockContentResolver resolver = new MockContentResolver();
      DelegatingContext context =
          new DelegatingContext(
              InstrumentationRegistry.getInstrumentation().getTargetContext(), prefix, resolver);

      for (Map.Entry<String, Class<? extends ContentProvider>> entry : providerClasses.entrySet()) {
        ContentProvider provider =
            createProvider(entry.getKey(), entry.getValue(), resolver, context);
        mProvidersRef.add(new WeakReference<>(provider));
      }

      return new ProviderTestRule(
          mProvidersRef, new HashSet<>(databaseArgsMap.values()), resolver, context);
    }

    private ContentProvider createProvider(
        String auth,
        Class<? extends ContentProvider> clazz,
        MockContentResolver resolver,
        Context context) {
      ContentProvider provider;

      try {
        provider = clazz.getConstructor().newInstance();
      } catch (NoSuchMethodException me) {
        Log.e(
            TAG,
            "NoSuchMethodException occurred when trying create new Instance for "
                + clazz.toString());
        throw new RuntimeException(me);
      } catch (InvocationTargetException ite) {
        Log.e(
            TAG,
            "InvocationTargetException occurred when trying create new Instance for "
                + clazz.toString());
        throw new RuntimeException(ite);
      } catch (IllegalAccessException iae) {
        Log.e(
            TAG,
            "IllegalAccessException occurred when trying create new Instance for "
                + clazz.toString());
        throw new RuntimeException(iae);
      } catch (InstantiationException ie) {
        Log.e(
            TAG,
            "InstantiationException occurred when trying create new Instance for "
                + clazz.toString());
        throw new RuntimeException(ie);
      }

      ProviderInfo providerInfo = new ProviderInfo();
      providerInfo.authority = auth;
      // attachInfo will call ContentProvider.onCreate(), so will refresh the context
      // used by ContentProvider.
      provider.attachInfo(context, providerInfo);
      resolver.addProvider(providerInfo.authority, provider);
      return provider;
    }

    private DatabaseArgs getDatabaseArgs(String dbName) {
      if (databaseArgsMap.containsKey(dbName)) {
        return databaseArgsMap.get(dbName);
      } else {
        DatabaseArgs databaseArgs = new DatabaseArgs(dbName);
        databaseArgsMap.put(dbName, databaseArgs);
        return databaseArgs;
      }
    }
  }

  private class ProviderStatement extends Statement {

    private final Statement base;

    public ProviderStatement(Statement base) {
      this.base = base;
    }

    @Override
    public void evaluate() throws Throwable {
      try {
        setUpProviders();
        base.evaluate();
      } finally {
        cleanUpProviders();
      }
    }
  }
}
