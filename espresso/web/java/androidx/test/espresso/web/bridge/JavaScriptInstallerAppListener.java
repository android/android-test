package androidx.test.espresso.web.bridge;

import android.app.Application;
import androidx.test.runner.lifecycle.ApplicationLifecycleCallback;
import androidx.test.runner.lifecycle.ApplicationStage;

/**
 * An {@link ApplicationLifecycleCallback}, registered via Manifest, that installs the
 * JavaScriptBridge.
 */
public class JavaScriptInstallerAppListener implements ApplicationLifecycleCallback {

  @Override
  public void onApplicationLifecycleChanged(Application app, ApplicationStage stage) {
    if (stage.equals(ApplicationStage.CREATED)) {
      JavaScriptBridge.installBridge();
    }
  }
}
