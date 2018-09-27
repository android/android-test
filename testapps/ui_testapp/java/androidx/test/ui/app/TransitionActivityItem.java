/*
 * Copyright (C) 2014 The Android Open Source Project
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

/**
 * Represents an Item in our application. Each item has a name, id, full size image url and
 * thumbnail url.
 */
public class TransitionActivityItem {

    public static final TransitionActivityItem[] ITEMS = new TransitionActivityItem[] {
            new TransitionActivityItem("Flying in the Light", "Romain Guy"),
            new TransitionActivityItem("Caterpillar", "Romain Guy"),
            new TransitionActivityItem("Look Me in the Eye", "Romain Guy"),
            new TransitionActivityItem("Flamingo", "Romain Guy"),
            new TransitionActivityItem("Rainbow", "Romain Guy"),
            new TransitionActivityItem("Over there", "Romain Guy"),
            new TransitionActivityItem("Jelly Fish 2", "Romain Guy"),
            new TransitionActivityItem("Lone Pine Sunset", "Romain Guy"),
    };

    public static TransitionActivityItem getItem(int id) {
        for (TransitionActivityItem item : ITEMS) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    private final String mName;
    private final String mAuthor;

    TransitionActivityItem(String name, String author) {
        mName = name;
        mAuthor = author;
    }

    public int getId() {
        return mName.hashCode() + mAuthor.hashCode();
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getName() {
        return mName;
    }


}
