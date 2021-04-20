package androidx.test.espresso.base;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewHierarchyRenderer;
import androidx.test.espresso.util.DefaultViewHierarchyRenderer;
import javax.inject.Inject;
import javax.inject.Singleton;

import static androidx.test.internal.platform.ServiceLoaderWrapper.loadSingleService;

/**
 * @see Espresso#getViewHierarchyRenderer()
 */
@Singleton
public class UpdatableViewHierarchyRenderer implements ViewHierarchyRenderer {

  private final Object updateLock = new Object();

  private volatile ViewHierarchyRenderer currentRenderer;

  @Inject UpdatableViewHierarchyRenderer() {
  }

  public void updateViewHierarchyRenderer(ViewHierarchyRenderer renderer) {
    currentRenderer = renderer;
  }

  @Override public String render(@NonNull View rootView, @Nullable ProblemViews problemViews) {
    if (currentRenderer == null) {
      synchronized (updateLock) {
        if (currentRenderer == null) {
          currentRenderer = loadSingleService(
              ViewHierarchyRenderer.class,
              DefaultViewHierarchyRenderer::new
          );
        }
      }
    }
    return currentRenderer.render(rootView, problemViews);
  }
}
