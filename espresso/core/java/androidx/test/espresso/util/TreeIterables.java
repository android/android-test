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
import static kotlin.collections.CollectionsKt.mutableListOf;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.VisibleForTesting;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import kotlin.collections.AbstractIterator;
import kotlin.collections.CollectionsKt;

/**
 * Utility methods for iterating over tree structured items.
 *
 * <p>Since the view hierarchy is a tree - having a method of iterating over its contents is useful.
 *
 * <p>This is generalized for any object which can display tree like qualities - but this
 * generalization was done for testability concerns (since creating View hierarchies is a pain).
 *
 * <p>Only public methods of this utility class are considered public API of the test framework.
 */
public final class TreeIterables {
  private static final TreeViewer<View> VIEW_TREE_VIEWER = new ViewTreeViewer();

  private TreeIterables() {}

  /**
   * Creates an iterable that traverses the tree formed by the given root.
   *
   * <p>Along with iteration order, the distance from the root element is also tracked.
   *
   * @param root the root view to track from.
   * @return An iterable of ViewAndDistance containing the view tree in a depth first order with the
   *     distance of a given node from the root.
   */
  public static Iterable<ViewAndDistance> depthFirstViewTraversalWithDistance(View root) {
    final DistanceRecordingTreeViewer<View> distanceRecorder =
        new DistanceRecordingTreeViewer<View>(root, VIEW_TREE_VIEWER);

    return CollectionsKt.map(
        depthFirstTraversal(root, distanceRecorder),
        view -> new ViewAndDistance(view, distanceRecorder.getDistance(view)));
  }

  /**
   * Returns an iterable which iterates thru the provided view and its children in a depth-first,
   * in-order traversal. That is to say that for a view such as: Root / | \ A R U /| |\ B D G N Will
   * be iterated: Root, A, B, D, R, G, N, U.
   *
   * @param root the non-null, root view.
   */
  public static Iterable<View> depthFirstViewTraversal(View root) {
    return depthFirstTraversal(root, VIEW_TREE_VIEWER);
  }

  /**
   * Returns an iterable which iterates thru the provided view and its children in a breadth-first,
   * row-level-order traversal. That is to say that for a view such as: Root / | \ A R U /| |\ B D G
   * N Will be iterated: Root, A, R, U, B, D, G, N
   *
   * @param root the non-null, root view.
   */
  public static Iterable<View> breadthFirstViewTraversal(View root) {
    return breadthFirstTraversal(root, VIEW_TREE_VIEWER);
  }

  /**
   * Creates a depth first traversing iterator of the tree rooted at root.
   *
   * @param root the root of the tree
   * @param viewer a TreeViewer which can determine leafiness of any instance of T and generate
   *     Iterables for the direct children of any instance of T.
   */
  @VisibleForTesting
  static <T> Iterable<T> depthFirstTraversal(final T root, final TreeViewer<T> viewer) {
    checkNotNull(root);
    checkNotNull(viewer);
    return new TreeTraversalIterable<>(root, TraversalStrategy.DEPTH_FIRST, viewer);
  }

  /**
   * Creates a breadth first traversing iterator of the tree rooted at root.
   *
   * @param root the root of the tree
   * @param viewer a TreeViewer which can determine leafiness of any instance of T and generate
   *     Iterables for the direct children of any instance of T.
   */
  @VisibleForTesting
  static <T> Iterable<T> breadthFirstTraversal(final T root, final TreeViewer<T> viewer) {
    checkNotNull(root);
    checkNotNull(viewer);
    return new TreeTraversalIterable<T>(root, TraversalStrategy.BREADTH_FIRST, viewer);
  }

  /** Converts a tree into an Iterable of the tree's nodes presented in a given traversal order. */
  private static class TreeTraversalIterable<T> implements Iterable<T> {
    private final T root;
    private final TraversalStrategy traversalStrategy;
    private final TreeViewer<T> treeViewer;

    private TreeTraversalIterable(
        T root, TraversalStrategy traversalStrategy, TreeViewer<T> treeViewer) {
      this.root = checkNotNull(root);
      this.traversalStrategy = checkNotNull(traversalStrategy);
      this.treeViewer = checkNotNull(treeViewer);
    }

    @Override
    public Iterator<T> iterator() {
      final LinkedList<T> nodes = new LinkedList<>();
      nodes.add(root);
      return new AbstractIterator<T>() {
        @Override
        protected void computeNext() {
          if (nodes.isEmpty()) {
            done();
          } else {
            T nextItem = checkNotNull(traversalStrategy.next(nodes), "Null items not allowed!");
            traversalStrategy.combineNewChildren(nodes, treeViewer.children(nextItem));
            setNext(nextItem);
          }
        }
      };
    }
  }

  private enum TraversalStrategy {
    BREADTH_FIRST() {
      @Override
      <T> void combineNewChildren(LinkedList<T> nodes, Collection<T> newChildren) {
        nodes.addAll(newChildren);
      }
    },
    DEPTH_FIRST() {
      @Override
      <T> void combineNewChildren(LinkedList<T> nodes, Collection<T> newChildren) {
        nodes.addAll(0, newChildren);
      }
    };

    abstract <T> void combineNewChildren(LinkedList<T> nodes, Collection<T> newChildren);

    <T> T next(LinkedList<T> nodes) {
      return nodes.removeFirst();
    }
  }

  /**
   * A TreeView providing access to the children of a given View.
   *
   * <p>The only way views can have children is if they are a subclass of ViewGroup.
   */
  @VisibleForTesting
  static class ViewTreeViewer implements TreeViewer<View> {
    @Override
    public Collection<View> children(View view) {
      checkNotNull(view);
      if (view instanceof ViewGroup) {
        ViewGroup group = (ViewGroup) view;
        int childCount = group.getChildCount();
        List<View> children = mutableListOf();
        for (int i = 0; i < childCount; i++) {
          children.add(group.getChildAt(i));
        }
        return children;
      } else {
        return Collections.<View>emptyList();
      }
    }
  }

  /**
   * Provides a tree view of items of instance T and records their distance from a well known root.
   *
   * <p>It is assumed that this TreeViewer will only be called with nodes that it has processed via
   * its children method, or the root node itself. Otherwise it will not be able to determine
   * distance from the root and an exception will be thrown.
   *
   * <p>This class is stateful and only provides the correct distances after the underlying tree has
   * been iterated over.
   */
  @VisibleForTesting
  static class DistanceRecordingTreeViewer<T> implements TreeViewer<T> {
    private final T root;
    private final Map<T, Integer> nodeToDistance = new HashMap<>();
    private final TreeViewer<T> delegateViewer;

    DistanceRecordingTreeViewer(T root, TreeViewer<T> delegateViewer) {
      this.root = checkNotNull(root);
      this.delegateViewer = checkNotNull(delegateViewer);
    }

    int getDistance(T node) {
      return checkNotNull(nodeToDistance.get(node), "Never seen %s before", node);
    }

    @Override
    public Collection<T> children(final T node) {
      if (node == root) {
        // base case.
        nodeToDistance.put(node, 0);
      }

      int myDistance = getDistance(node);
      final int childDistance = myDistance + 1;
      Collection<T> children = delegateViewer.children(node);

      for (T child : children) {
        nodeToDistance.put(child, childDistance);
      }
      return children;
    }
  }

  /**
   * Provides a way of viewing any instance of T as a tree so long as there exists a method for
   * converting the instance of T into a Collection of that instance's direct children.
   *
   * <p>This nice, sensible abstraction for dealing with objects with treelike properties was stolen
   * from Guava's bug tracker. The Guava team is still working out the way trees should be exposed
   * as Guava collections - so we have to provide our own.
   */
  @VisibleForTesting
  interface TreeViewer<T> {

    /** Returns a collection view of the children of this node. */
    Collection<T> children(T instance);
  }

  /** Represents the distance a given view is from the root view. */
  public static class ViewAndDistance {
    private final View view;
    private final int distanceFromRoot;

    private ViewAndDistance(View view, int distanceFromRoot) {
      this.view = view;
      this.distanceFromRoot = distanceFromRoot;
    }

    public View getView() {
      return view;
    }

    public int getDistanceFromRoot() {
      return distanceFromRoot;
    }
  }
}
