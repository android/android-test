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

package androidx.test.espresso.web.model;

import static kotlin.collections.CollectionsKt.listOf;
import static kotlin.collections.CollectionsKt.toMutableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link SimpleAtom}. */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class SimpleAtomTest {

  @Test
  public void testArgumentOrdering_noArgs() {
    SimpleAtom atom = new SimpleAtom("return 1;");
    assertTrue(atom.getArguments(null).isEmpty());

    ElementReference elementReference = new ElementReference("dog");
    assertEquals(elementReference, atom.getArguments(elementReference).get(0));
  }

  @Test
  public void testArgumentOrdering_nonContextualArgsSupplied() {
    final List<Object> vals = listOf(1, 2, 3, 4, 5);
    SimpleAtom atom =
        new SimpleAtom("return 1;") {
          @Override
          public List<Object> getNonContextualArguments() {
            return vals;
          }
        };
    assertEquals(vals, atom.getArguments(null));

    ElementReference elementReference = new ElementReference("dog");
    List<Object> withElement = atom.getArguments(elementReference);
    List<Object> expected = toMutableList(vals);
    expected.add(0, elementReference);
    assertEquals(expected, withElement);
  }

  @Test
  public void testArgumentOrdering_lastPlacement() {
    final List<Object> vals = listOf(1, 2, 3, 4, 5);
    SimpleAtom atom =
        new SimpleAtom("return 1;", SimpleAtom.ElementReferencePlacement.LAST) {
          @Override
          public List<Object> getNonContextualArguments() {
            return vals;
          }
        };
    assertEquals(vals, atom.getArguments(null));

    ElementReference elementReference = new ElementReference("dog");
    List<Object> withElement = atom.getArguments(elementReference);
    List<Object> expected = toMutableList(vals);
    expected.add(elementReference);
    assertEquals(expected, withElement);
  }

  @Test
  public void testContract_handleBadEvalution() {
    final boolean[] called = new boolean[1];
    called[0] = false;
    new SimpleAtom("return 1;") {
      @Override
      public Evaluation handleBadEvaluation(Evaluation e) {
        called[0] = true;
        return e;
      }
    }.transform(new Evaluation.Builder().setStatus(-1).setMessage("bad").build());
    assertTrue(called[0]);

    called[0] = false;
    new SimpleAtom("return 1;") {
      @Override
      public Evaluation handleBadEvaluation(Evaluation e) {
        called[0] = true;
        return e;
      }
    }.transform(new Evaluation.Builder().setStatus(0).setValue("nothing here").build());
    assertFalse(called[0]);
  }

  @Test
  public void testContract_getNonContextualArguments() {
    final boolean[] called = new boolean[1];
    called[0] = false;
    new SimpleAtom("return 1;") {
      @Override
      public List<Object> getNonContextualArguments() {
        called[0] = true;
        return super.getNonContextualArguments();
      }
    }.getArguments(null);
    assertTrue(called[0]);
  }

  @Test
  public void testContract_noElementReference() {
    final boolean[] called = new boolean[1];
    called[0] = false;
    new SimpleAtom("return 1;") {
      @Override
      public void handleNoElementReference() {
        called[0] = true;
      }
    }.getArguments(null);
    assertTrue(called[0]);
    called[0] = false;
    new SimpleAtom("return 1;") {
      @Override
      public void handleNoElementReference() {
        called[0] = true;
      }
    }.getArguments(new ElementReference("foo"));
    assertFalse(called[0]);
  }
}
