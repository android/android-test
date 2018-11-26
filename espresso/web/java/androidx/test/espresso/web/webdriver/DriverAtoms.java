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

import static androidx.test.espresso.web.model.Atoms.castOrDie;
import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.VisibleForTesting;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.web.model.Atom;
import androidx.test.espresso.web.model.ElementReference;
import androidx.test.espresso.web.model.Evaluation;
import androidx.test.espresso.web.model.SimpleAtom;
import androidx.test.espresso.web.model.TransformingAtom;
import androidx.test.espresso.web.model.WindowReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

/** A collection of Javascript Atoms from the WebDriver project. */
public final class DriverAtoms {

  private DriverAtoms() {}

  /** Simulates the javascript events to click on a particular element. */
  public static Atom<Evaluation> webClick() {
    return new WebClickSimpleAtom();
  }

  /** Clears content from an editable element. */
  public static Atom<Evaluation> clearElement() {
    return new ClearElementSimpleAtom();
  }

  /** Simulates javascript key events sent to a certain element. */
  public static Atom<Evaluation> webKeys(final String text) {
    return new WebKeysSimpleAtom(checkNotNull(text));
  }

  /** Finds an element using the provided locatorType strategy. */
  public static Atom<ElementReference> findElement(final Locator locator, final String value) {
    return new FindElementTransformingAtom(
        new FindElementSimpleAtom(locator.getType(), value), castOrDie(ElementReference.class));
  }

  /** Finds the currently active element in the document. */
  public static Atom<ElementReference> selectActiveElement() {
    return new SelectActiveElementTransformingAtom(
        new ActiveElementSimpleAtom(), castOrDie(ElementReference.class));
  }

  /** Selects a subframe of the currently selected window by it's index. */
  public static Atom<WindowReference> selectFrameByIndex(int index) {
    return new SelectFrameByIndexTransformingAtom(
        new FrameByIndexSimpleAtom(index), castOrDie(WindowReference.class));
  }

  /** Selects a subframe of the given window by it's index. */
  public static Atom<WindowReference> selectFrameByIndex(int index, WindowReference root) {
    return new SelectFrameByIndexTransformingAtom(
        new FrameByIndexWithRootSimpleAtom(index, checkNotNull(root)),
        castOrDie(WindowReference.class));
  }

  /** Selects a subframe of the given window by it's name or id. */
  public static Atom<WindowReference> selectFrameByIdOrName(String idOrName, WindowReference root) {
    return new SelectFrameByIdOrNameTransformingAtom(
        new FrameByIdOrNameWithRootSimpleAtom(checkNotNull(idOrName), checkNotNull(root)),
        castOrDie(WindowReference.class));
  }

  /** Selects a subframe of the current window by it's name or id. */
  public static Atom<WindowReference> selectFrameByIdOrName(String idOrName) {
    return new SelectFrameByIdOrNameTransformingAtom(
        new FrameByIdOrNameSimpleAtom(checkNotNull(idOrName)), castOrDie(WindowReference.class));
  }

  /** Returns the visible text beneath a given DOM element. */
  public static Atom<String> getText() {
    return new GetTextTransformingAtom(new GetVisibleTextSimpleAtom(), castOrDie(String.class));
  }

  /** Returns {@code true} if the desired element is in view after scrolling. */
  public static Atom<Boolean> webScrollIntoView() {
    return new WebScrollIntoViewAtom(new WebScrollIntoViewSimpleAtom(), castOrDie(Boolean.class));
  }

  /** Finds multiple elements given a locator strategy. */
  public static Atom<List<ElementReference>> findMultipleElements(
      final Locator locator, final String value) {

    SimpleAtom findElementsScriptSimpleAtom =
        new FindElementsScriptSimpleAtom(locator.getType(), value);
    TransformingAtom.Transformer<Evaluation, List<ElementReference>> elementReferenceListAtom =
        new ElementReferenceListAtom(locator.getType(), value);

    return new FindMultipleElementsTransformingAtom(
        findElementsScriptSimpleAtom, elementReferenceListAtom);
  }

  private static Map<String, String> makeLocatorJSON(Locator locator, String value) {
    checkNotNull(locator);
    checkNotNull(value);
    Map<String, String> map = Maps.newHashMap();
    map.put(locator.getType(), value);
    return map;
  }

  @VisibleForTesting
  static final class FindElementSimpleAtom extends SimpleAtom {
    @RemoteMsgField(order = 0)
    final String locatorType;

    @RemoteMsgField(order = 1)
    final String value;

    @RemoteMsgConstructor
    FindElementSimpleAtom(String locatorType, String value) {
      super(WebDriverAtomScripts.FIND_ELEMENT_ANDROID, SimpleAtom.ElementReferencePlacement.LAST);
      this.locatorType = locatorType;
      this.value = value;
    }

    @Override
    protected List<Object> getNonContextualArguments() {
      final Map<String, String> locatorJson = makeLocatorJSON(Locator.forType(locatorType), value);
      return Lists.newArrayList((Object) locatorJson);
    }
  }

  @VisibleForTesting
  static final class FindElementTransformingAtom
      extends TransformingAtom<Evaluation, ElementReference> {
    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 0)
    private final Atom<Evaluation> findElementSimpleAtom;

    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 1)
    private final TransformingAtom.Transformer<Evaluation, ElementReference> castOrDieAtom;

    @RemoteMsgConstructor
    private FindElementTransformingAtom(
        Atom<Evaluation> findElementSimpleAtom,
        TransformingAtom.Transformer<Evaluation, ElementReference> castOrDieAtom) {
      super(findElementSimpleAtom, castOrDieAtom);
      this.findElementSimpleAtom = findElementSimpleAtom;
      this.castOrDieAtom = castOrDieAtom;
    }
  }

  @VisibleForTesting
  static final class ClearElementSimpleAtom extends SimpleAtom {
    @RemoteMsgConstructor
    private ClearElementSimpleAtom() {
      super(WebDriverAtomScripts.CLEAR_ANDROID);
    }

    @Override
    public void handleNoElementReference() {
      throw new RuntimeException("clearElement: Need an element to clear!");
    }
  }

  @VisibleForTesting
  static final class WebKeysSimpleAtom extends SimpleAtom {
    @RemoteMsgField(order = 0)
    private final String text;

    @RemoteMsgConstructor
    private WebKeysSimpleAtom(String text) {
      super(WebDriverAtomScripts.SEND_KEYS_ANDROID);
      this.text = text;
    }

    @Override
    public void handleNoElementReference() {
      throw new RuntimeException("webKeys: Need an element to type on!");
    }

    @Override
    public List<Object> getNonContextualArguments() {
      return Lists.newArrayList((Object) text);
    }
  }

  @VisibleForTesting
  static final class WebScrollIntoViewAtom extends TransformingAtom<Evaluation, Boolean> {
    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 0)
    private final Atom<Evaluation> scrollIntoViewSimpleAtom;

    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 1)
    private final TransformingAtom.Transformer<Evaluation, Boolean> castOrDieAtom;

    @RemoteMsgConstructor
    private WebScrollIntoViewAtom(
        Atom<Evaluation> scrollIntoViewSimpleAtom,
        TransformingAtom.Transformer<Evaluation, Boolean> castOrDieAtom) {
      super(scrollIntoViewSimpleAtom, castOrDieAtom);
      this.scrollIntoViewSimpleAtom = scrollIntoViewSimpleAtom;
      this.castOrDieAtom = castOrDieAtom;
    }
  }

  static final class WebScrollIntoViewSimpleAtom extends SimpleAtom {
    @RemoteMsgConstructor
    private WebScrollIntoViewSimpleAtom() {
      super(WebDriverAtomScripts.SCROLL_INTO_VIEW_ANDROID);
    }

    @Override
    public void handleNoElementReference() {
      throw new RuntimeException("scrollIntoView: need an element to scroll to");
    }
  }

  @VisibleForTesting
  static final class WebClickSimpleAtom extends SimpleAtom {
    @RemoteMsgConstructor
    private WebClickSimpleAtom() {
      super(WebDriverAtomScripts.CLICK_ANDROID);
    }

    @Override
    public void handleNoElementReference() {
      throw new RuntimeException("webClick: Need an element to click on!");
    }
  }

  @VisibleForTesting
  static final class GetTextTransformingAtom extends TransformingAtom<Evaluation, String> {
    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 0)
    private final Atom<Evaluation> getTextSimpleAtom;

    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 1)
    private final TransformingAtom.Transformer<Evaluation, String> castOrDieAtom;

    @RemoteMsgConstructor
    private GetTextTransformingAtom(
        Atom<Evaluation> findElementSimpleAtom,
        TransformingAtom.Transformer<Evaluation, String> castOrDieAtom) {
      super(findElementSimpleAtom, castOrDieAtom);
      this.getTextSimpleAtom = findElementSimpleAtom;
      this.castOrDieAtom = castOrDieAtom;
    }
  }

  @VisibleForTesting
  static final class GetVisibleTextSimpleAtom extends SimpleAtom {
    @RemoteMsgConstructor
    private GetVisibleTextSimpleAtom() {
      super(WebDriverAtomScripts.GET_VISIBLE_TEXT_ANDROID);
    }
  }

  @VisibleForTesting
  static final class ActiveElementSimpleAtom extends SimpleAtom {
    @RemoteMsgConstructor
    private ActiveElementSimpleAtom() {
      super(WebDriverAtomScripts.ACTIVE_ELEMENT_ANDROID);
    }
  }

  @VisibleForTesting
  static final class SelectActiveElementTransformingAtom
      extends TransformingAtom<Evaluation, ElementReference> {
    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 0)
    private final Atom<Evaluation> selectActiveElementSimpleAtom;

    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 1)
    private final TransformingAtom.Transformer<Evaluation, ElementReference> castOrDieAtom;

    @RemoteMsgConstructor
    private SelectActiveElementTransformingAtom(
        Atom<Evaluation> selectActiveElementSimpleAtom,
        TransformingAtom.Transformer<Evaluation, ElementReference> castOrDieAtom) {
      super(selectActiveElementSimpleAtom, castOrDieAtom);
      this.selectActiveElementSimpleAtom = selectActiveElementSimpleAtom;
      this.castOrDieAtom = castOrDieAtom;
    }
  }

  @VisibleForTesting
  static final class FrameByIndexSimpleAtom extends SimpleAtom {
    @RemoteMsgField(order = 0)
    private final int index;

    @RemoteMsgConstructor
    private FrameByIndexSimpleAtom(int index) {
      super(WebDriverAtomScripts.FRAME_BY_INDEX_ANDROID);
      this.index = index;
    }

    @Override
    public List<Object> getNonContextualArguments() {
      return Lists.newArrayList((Object) index);
    }
  }

  @VisibleForTesting
  static final class FrameByIndexWithRootSimpleAtom extends SimpleAtom {
    @RemoteMsgField(order = 0)
    private final int index;

    @RemoteMsgField(order = 1)
    private final WindowReference root;

    @RemoteMsgConstructor
    private FrameByIndexWithRootSimpleAtom(int index, WindowReference root) {
      super(WebDriverAtomScripts.FRAME_BY_INDEX_ANDROID);
      this.index = index;
      this.root = root;
    }

    @Override
    public List<Object> getNonContextualArguments() {
      List<Object> args = Lists.newArrayList((Object) index);
      args.add(root);
      return args;
    }
  }

  @VisibleForTesting
  static final class SelectFrameByIndexTransformingAtom
      extends TransformingAtom<Evaluation, WindowReference> {
    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 0)
    private final Atom<Evaluation> frameByIndexSimpleAtom;

    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 1)
    private final TransformingAtom.Transformer<Evaluation, WindowReference> castOrDieAtom;

    @RemoteMsgConstructor
    private SelectFrameByIndexTransformingAtom(
        Atom<Evaluation> selectActiveElementSimpleAtom,
        TransformingAtom.Transformer<Evaluation, WindowReference> castOrDieAtom) {
      super(selectActiveElementSimpleAtom, castOrDieAtom);
      this.frameByIndexSimpleAtom = selectActiveElementSimpleAtom;
      this.castOrDieAtom = castOrDieAtom;
    }
  }

  @VisibleForTesting
  static final class FrameByIdOrNameSimpleAtom extends SimpleAtom {
    @RemoteMsgField(order = 0)
    private final String idOrName;

    @RemoteMsgConstructor
    private FrameByIdOrNameSimpleAtom(String idOrName) {
      super(WebDriverAtomScripts.FRAME_BY_ID_OR_NAME_ANDROID);
      this.idOrName = idOrName;
    }

    @Override
    public List<Object> getNonContextualArguments() {
      return Lists.newArrayList((Object) idOrName);
    }
  }

  @VisibleForTesting
  static final class FrameByIdOrNameWithRootSimpleAtom extends SimpleAtom {
    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 0)
    private final String idOrName;

    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 1)
    private final WindowReference root;

    @RemoteMsgConstructor
    private FrameByIdOrNameWithRootSimpleAtom(String idOrName, WindowReference root) {
      super(WebDriverAtomScripts.FRAME_BY_ID_OR_NAME_ANDROID);
      this.idOrName = idOrName;
      this.root = root;
    }

    @Override
    public List<Object> getNonContextualArguments() {
      List<Object> args = Lists.newArrayList((Object) idOrName);
      args.add(root);
      return args;
    }
  }

  @VisibleForTesting
  static final class SelectFrameByIdOrNameTransformingAtom
      extends TransformingAtom<Evaluation, WindowReference> {
    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 0)
    private final Atom<Evaluation> frameByIndexOrNameSimpleAtom;

    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 1)
    private final TransformingAtom.Transformer<Evaluation, WindowReference> castOrDieAtom;

    @RemoteMsgConstructor
    private SelectFrameByIdOrNameTransformingAtom(
        Atom<Evaluation> selectActiveElementSimpleAtom,
        TransformingAtom.Transformer<Evaluation, WindowReference> castOrDieAtom) {
      super(selectActiveElementSimpleAtom, castOrDieAtom);
      this.frameByIndexOrNameSimpleAtom = selectActiveElementSimpleAtom;
      this.castOrDieAtom = castOrDieAtom;
    }
  }

  @VisibleForTesting
  static final class FindElementsScriptSimpleAtom extends SimpleAtom {
    @RemoteMsgField(order = 0)
    final String locatorType;

    @RemoteMsgField(order = 1)
    final String value;

    @RemoteMsgConstructor
    private FindElementsScriptSimpleAtom(String locatorType, String value) {
      super(WebDriverAtomScripts.FIND_ELEMENTS_ANDROID);
      this.locatorType = locatorType;
      this.value = value;
    }

    @Override
    public List<Object> getNonContextualArguments() {
      return Lists.newArrayList((Object) makeLocatorJSON(Locator.forType(locatorType), value));
    }
  }

  @VisibleForTesting
  static final class FindMultipleElementsTransformingAtom
      extends TransformingAtom<Evaluation, List<ElementReference>> {
    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 0)
    private final Atom<Evaluation> findElementsScriptSimpleAtom;

    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 1)
    private final TransformingAtom.Transformer<Evaluation, List<ElementReference>>
        elementReferenceListAtom;

    @RemoteMsgConstructor
    private FindMultipleElementsTransformingAtom(
        Atom<Evaluation> findElementsScriptSimpleAtom,
        TransformingAtom.Transformer<Evaluation, List<ElementReference>> elementReferenceListAtom) {
      super(findElementsScriptSimpleAtom, elementReferenceListAtom);
      this.findElementsScriptSimpleAtom = findElementsScriptSimpleAtom;
      this.elementReferenceListAtom = elementReferenceListAtom;
    }
  }

  static final class ElementReferenceListAtom
      implements TransformingAtom.Transformer<Evaluation, List<ElementReference>> {

    @RemoteMsgField(order = 0)
    final String locatorType;

    @RemoteMsgField(order = 1)
    final String value;

    @RemoteMsgConstructor
    private ElementReferenceListAtom(String locatorType, String value) {
      this.locatorType = locatorType;
      this.value = value;
    }

    @Override
    public List<ElementReference> apply(Evaluation e) {
      Object rawValues = e.getValue();
      if (null == rawValues) {
        return Lists.newArrayList();
      }
      if (rawValues instanceof Iterable) {
        List<ElementReference> references = Lists.newArrayList();
        for (Object rawValue : ((Iterable) rawValues)) {
          if (rawValue instanceof ElementReference) {
            references.add((ElementReference) rawValue);
          } else {
            throw new RuntimeException(
                String.format(
                    "Unexpected non-elementReference in findMultipleElements(%s, %s): "
                        + "(%s) all: %s ",
                    Locator.forType(locatorType).name(), value, rawValue, e));
          }
        }
        return references;
      } else {
        throw new RuntimeException(
            String.format(
                "Unexpected non-iterableType in findMultipleElements(%s, %s): "
                    + "return evaluation: %s ",
                Locator.forType(locatorType).name(), value, e));
      }
    }
  }
}
