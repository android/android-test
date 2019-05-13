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

package androidx.test.espresso.web.assertion;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.web.model.Atoms.script;
import static androidx.test.espresso.web.model.Atoms.transform;
import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.VisibleForTesting;
import android.webkit.WebView;
import androidx.test.espresso.remote.annotation.RemoteMsgConstructor;
import androidx.test.espresso.remote.annotation.RemoteMsgField;
import androidx.test.espresso.web.model.Atom;
import androidx.test.espresso.web.model.Evaluation;
import androidx.test.espresso.web.model.TransformingAtom;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/** A collection of {@link WebAssertion}s that assert on {@link WebView}s. */
public final class WebViewAssertions {

  private static final ResultDescriber<Object> TO_STRING_DESCRIBER = new ToStringResultDescriber();

  private WebViewAssertions() {}

  /**
   * A WebAssertion which asserts that the given Atom's result is accepted by the provided matcher.
   *
   * @param atom an atom to evaluate on the webview
   * @param resultMatcher a matcher to apply to the result of the atom.
   * @param resultDescriber a describer that converts the result to a string.
   */
  public static <E> WebAssertion<E> webMatches(
      Atom<E> atom,
      final Matcher<E> resultMatcher,
      final ResultDescriber<? super E> resultDescriber) {
    checkNotNull(resultMatcher);
    checkNotNull(resultDescriber);
    checkNotNull(atom);
    return new ResultCheckingWebAssertion<>(atom, resultMatcher, resultDescriber);
  }

  /**
   * A WebAssertion which asserts that the given Atom's result is accepted by the provided matcher.
   *
   * @param atom an atom to evaluate on the webview
   * @param resultMatcher a matcher to apply to the result of the atom.
   */
  public static <E> WebAssertion<E> webMatches(Atom<E> atom, final Matcher<E> resultMatcher) {
    return webMatches(atom, resultMatcher, TO_STRING_DESCRIBER);
  }

  /** A WebAssertion which asserts that the document is matched by the provided matcher. */
  public static WebAssertion<Document> webContent(final Matcher<Document> domMatcher) {
    checkNotNull(domMatcher);
    return webMatches(
        transform(
            script("function getHtml() {return document.documentElement.outerHTML;}"),
            new DocumentParserAtom()),
        domMatcher,
        new WebContentResultDescriber());
  }

  /**
   * Converts a result to a String.
   *
   * @param <E> The type of the result.
   */
  public interface ResultDescriber<E> {
    public String apply(E input);
  }

  @VisibleForTesting
  static final class ResultCheckingWebAssertion<E> extends WebAssertion<E> {
    @SuppressWarnings("unused") // called reflectively
    @RemoteMsgField(order = 0)
    private final Atom<E> atom;

    @RemoteMsgField(order = 1)
    private final Matcher<E> resultMatcher;

    @RemoteMsgField(order = 2)
    private final ResultDescriber<? super E> resultDescriber;

    @RemoteMsgConstructor
    ResultCheckingWebAssertion(
        Atom<E> atom, Matcher<E> resultMatcher, ResultDescriber<? super E> resultDescriber) {
      super(atom);
      this.atom = atom;
      this.resultMatcher = resultMatcher;
      this.resultDescriber = resultDescriber;
    }

    @Override
    protected void checkResult(WebView view, E result) {
      StringDescription description = new StringDescription();
      description.appendText("'");
      resultMatcher.describeTo(description);
      description.appendText("' doesn't match: ");
      description.appendText(null == result ? "null" : resultDescriber.apply(result));
      assertThat(description.toString(), result, resultMatcher);
    }
  }

  @VisibleForTesting
  static final class ToStringResultDescriber implements ResultDescriber<Object> {

    @RemoteMsgConstructor
    public ToStringResultDescriber() {}

    @Override
    public String apply(Object input) {
      return input.toString();
    }
  }

  @VisibleForTesting
  static final class WebContentResultDescriber implements ResultDescriber<Document> {
    @RemoteMsgConstructor
    public WebContentResultDescriber() {}

    @Override
    public String apply(Document document) {
      try {
        DOMSource docSource = new DOMSource(document);
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        StreamResult streamer = new StreamResult(writer);
        tf.transform(docSource, streamer);
        return writer.toString();
      } catch (TransformerException e) {
        return "Could not transform!!!" + e;
      }
    }
  }

  @VisibleForTesting
  static final class DocumentParserAtom
      implements TransformingAtom.Transformer<Evaluation, Document> {
    @RemoteMsgConstructor
    public DocumentParserAtom() {}

    @Override
    public Document apply(Evaluation eval) {
      if (eval.getValue() instanceof String) {
        try {
          return TagSoupDocumentParser.newInstance().parse((String) eval.getValue());
        } catch (SAXException se) {
          throw new RuntimeException("Parse failed: " + eval.getValue(), se);
        } catch (IOException ioe) {
          throw new RuntimeException("Parse failed: " + eval.getValue(), ioe);
        }
      }
      throw new RuntimeException("Value should have been a string: " + eval);
    }
  }
}
