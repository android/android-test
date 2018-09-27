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
 *
 */

package androidx.test.multiprocess.app;

import android.app.Activity;

/**
 * Activity running in a global process which is manually defined in the AndroidManifest.xml
 * using the android:process attribute.
 * <p/>
 * From D.A.C: "If the process name begins with a lowercase character, the activity will run in a
 * global process of that name, provided that it has permission to do so."
 */
public class GlobalProcessActivity extends Activity {}
