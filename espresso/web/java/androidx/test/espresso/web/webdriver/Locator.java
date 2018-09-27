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

package androidx.test.espresso.web.webdriver;

/** Valid WebDriver locatorType types. */
public enum Locator {
  CLASS_NAME("className"),
  CSS_SELECTOR("css"),
  ID("id"),
  LINK_TEXT("linkText"),
  NAME("name"),
  PARTIAL_LINK_TEXT("partialLinkText"),
  TAG_NAME("tagName"),
  XPATH("xpath");

  private final String type;

  Locator(String type) {
    this.type = type;
  }

  static Locator forType(String type) {
    if (CLASS_NAME.getType().equals(type)) {
      return CLASS_NAME;
    }
    if (CSS_SELECTOR.getType().equals(type)) {
      return CSS_SELECTOR;
    }
    if (ID.getType().equals(type)) {
      return ID;
    }
    if (LINK_TEXT.getType().equals(type)) {
      return LINK_TEXT;
    }
    if (NAME.getType().equals(type)) {
      return NAME;
    }
    if (PARTIAL_LINK_TEXT.getType().equals(type)) {
      return PARTIAL_LINK_TEXT;
    }
    if (TAG_NAME.getType().equals(type)) {
      return TAG_NAME;
    }
    if (XPATH.getType().equals(type)) {
      return XPATH;
    }
    throw new IllegalStateException("No Locator enum found for a given type: " + type);
  }

  public String getType() {
    return type;
  }
}
