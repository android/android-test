package androidx.test.ext.junit.rules;

import android.app.Activity;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.testing.RecreationRecordingActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

/** Tests for {@link ActivityScenarioRule} using explicit launch to start activity. */
@RunWith(AndroidJUnit4.class)
public final class ActivityScenarioRuleWithExplicitLaunchTest {

  @Rule
  public ActivityScenarioRule<RecreationRecordingActivity> activityScenarioRule =
          new ActivityScenarioRule<>(RecreationRecordingActivity.class, null, false);

  @Test
  public void activityShouldBeResumedAutomatically() {
    activityScenarioRule
            .getScenario()
            .onActivity(
                    activity -> {
                      assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
                      assertThat(activity.getNumberOfRecreations()).isEqualTo(0);
                    });
  }

  @Test
  public void recreateActivityShouldWork() {
    activityScenarioRule.getScenario().recreate();
    activityScenarioRule
            .getScenario()
            .onActivity(
                    activity -> {
                      assertThat(lastLifeCycleTransition(activity)).isEqualTo(Stage.RESUMED);
                      assertThat(activity.getNumberOfRecreations()).isEqualTo(1);
                    });
  }

  @Test
  public void activityCanBeDestroyedManually() {
    activityScenarioRule.getScenario().moveToState(Lifecycle.State.DESTROYED);
  }

  @SuppressWarnings("EmptyTryBlock")
  @Test
  public void ruleWithoutGetScenarioCallShouldNotThrow() {
    try {

    } catch(Throwable e) {
      fail("ActivityScenarioRule should not throw an exception when used without calling getScenario()");
    }
  }

  private static Stage lastLifeCycleTransition(Activity activity) {
    return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity);
  }
}
