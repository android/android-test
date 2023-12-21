/*
 * Copyright (C) 2014 The Android Open Source Project
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

package androidx.test.espresso.util;

import static androidx.test.internal.util.Checks.checkNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.rules.ExpectedException.none;

import androidx.test.espresso.util.TreeIterables.DistanceRecordingTreeViewer;
import androidx.test.espresso.util.TreeIterables.TreeViewer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.functions.Function1;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/** Unit tests for {@link TreeIterables}. */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class TreeIterablesTest {

  @Rule public ExpectedException expectedException = none();

  private static class TestElement {
    private final String data;
    private final List<TestElement> children;

    public TestElement(String data, TestElement... children) {
      this.data = checkNotNull(data);
      this.children = Arrays.asList(children);
    }
  }

  private static class TestElementTreeViewer implements TreeViewer<TestElement> {
    @Override
    public Collection<TestElement> children(TestElement element) {
      return element.children;
    }
  }

  private static class TestElementStringConvertor implements Function1<TestElement, String> {
    @Override
    public String invoke(TestElement e) {
      return e.data;
    }
  }

  private static final TestElement trivialTree =
      new TestElement("a", new TestElement("b", new TestElement("c", new TestElement("d"))));

  private static final TestElement complexTree =
      new TestElement(
          "a",
          new TestElement(
              "b",
              new TestElement(
                  "c", new TestElement("d"), new TestElement("e", new TestElement("f"))),
              new TestElement("g"),
              new TestElement(
                  "h", new TestElement("i", new TestElement("j", new TestElement("k"))))),
          new TestElement("l"),
          new TestElement("m"),
          new TestElement("n", new TestElement("o", new TestElement("p"), new TestElement("q"))));

  @Test
  public void distanceRecorder_unknownItemThrowsException() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(complexTree, new TestElementTreeViewer());
    expectedException.expect(RuntimeException.class);
    distanceRecorder.getDistance(new TestElement("hello"));
  }

  @Test
  public void distanceRecorder_unprocessedChildThrowsException() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(complexTree, new TestElementTreeViewer());

    expectedException.expect(RuntimeException.class);
    distanceRecorder.getDistance(complexTree.children.get(0));
  }

  @Test
  public void distanceRecorder_distanceKnownAfterChildrenCall() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(complexTree, new TestElementTreeViewer());

    @SuppressWarnings("unused")
    List<TestElement> createdForSideEffect =
        new ArrayList<>(distanceRecorder.children(complexTree));

    assertThat(distanceRecorder.getDistance(complexTree), is(0));
    assertThat(distanceRecorder.getDistance(complexTree.children.iterator().next()), is(1));
  }

  @Test
  public void complexTree_Distances() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(complexTree, new TestElementTreeViewer());
    Iterable<TestElement> complexIterable =
        TreeIterables.depthFirstTraversal(complexTree, distanceRecorder);
    Set<TestElement> complexSet = new LinkedHashSet<>();
    for (TestElement e : complexIterable) {
      complexSet.add(e);
    }
    Map<String, Integer> distancesByData = new LinkedHashMap<>();
    for (TestElement e : complexSet) {
      distancesByData.put(e.data, distanceRecorder.getDistance(e));
    }

    assertThat(
        distancesByData,
        allOf(
            hasEntry("a", 0),
            hasEntry("b", 1),
            hasEntry("c", 2),
            hasEntry("d", 3),
            hasEntry("e", 3),
            hasEntry("f", 4),
            hasEntry("g", 2),
            hasEntry("h", 2),
            hasEntry("i", 3),
            hasEntry("j", 4),
            hasEntry("k", 5),
            hasEntry("l", 1),
            hasEntry("m", 1),
            hasEntry("n", 1),
            hasEntry("o", 2),
            hasEntry("p", 3),
            hasEntry("q", 3)));
    assertThat(distancesByData.size(), is(17));

    List<String> traversalOrder = map(complexIterable, new TestElementStringConvertor());

    // should be depth first if forwarding correctly.
    assertThat(
        traversalOrder,
        is(
            Arrays.asList(
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                "q")));
  }

  @Test
  public void complexTraversal_depthFirst() {
    List<String> breadthFirst =
        map(
            TreeIterables.depthFirstTraversal(complexTree, new TestElementTreeViewer()),
            new TestElementStringConvertor());
    assertThat(
        breadthFirst,
        is(
            (Iterable<String>)
                Arrays.asList(
                    "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                    "q")));
  }

  @Test
  public void complexTraversal_breadthFirst() {
    List<String> breadthFirst =
        map(
            TreeIterables.breadthFirstTraversal(complexTree, new TestElementTreeViewer()),
            new TestElementStringConvertor());
    assertThat(
        breadthFirst,
        is(
            Arrays.asList(
                "a", // root
                "b", "l", "m", "n", // L1
                "c", "g", "h", "o", // L2
                "d", "e", "i", "p", "q", // L3
                "f", "j", // L4
                "k"))); // L5
  }

  @Test
  public void trivialTraversal_breadthFirst() {
    // essentially the same as depth first.
    List<String> breadthFirst =
        map(
            TreeIterables.breadthFirstTraversal(trivialTree, new TestElementTreeViewer()),
            new TestElementStringConvertor());
    assertThat(breadthFirst, is(Arrays.asList("a", "b", "c", "d")));
  }

  @Test
  public void trivialTraversal_depthFirst() {
    List<String> depthFirst =
        map(
            TreeIterables.depthFirstTraversal(trivialTree, new TestElementTreeViewer()),
            new TestElementStringConvertor());
    assertThat(depthFirst, is(Arrays.asList("a", "b", "c", "d")));
  }

  @Test
  public void trivial_distance() {
    final DistanceRecordingTreeViewer<TestElement> distanceRecorder =
        new DistanceRecordingTreeViewer<TestElement>(trivialTree, new TestElementTreeViewer());

    Iterable<TestElement> trivialIterable =
        TreeIterables.depthFirstTraversal(trivialTree, distanceRecorder);
    Set<TestElement> trivialSet = new LinkedHashSet<>();
    for (TestElement e : trivialIterable) {
      trivialSet.add(e);
    }
    Map<String, Integer> distancesByData = new LinkedHashMap<>();
    for (TestElement e : trivialSet) {
      distancesByData.put(e.data, distanceRecorder.getDistance(e));
    }

    assertThat(
        distancesByData,
        allOf(hasEntry("a", 0), hasEntry("b", 1), hasEntry("c", 2), hasEntry("d", 3)));
    assertThat(distancesByData.size(), is(4));
  }

  private static <T, U> List<U> map(Iterable<T> iterable, Function1<T, U> mapper) {
    List<U> values = new ArrayList<>();
    for (T t : iterable) {
      values.add(mapper.invoke(t));
    }
    return values;
  }
}
