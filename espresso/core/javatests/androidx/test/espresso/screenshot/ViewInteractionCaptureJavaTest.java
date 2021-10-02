package androidx.test.espresso.screenshot;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Bitmap;
import androidx.test.core.graphics.BitmapStorage;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ui.app.MainActivity;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** A simple scuba test to ensure captureToImage works from hjava */
@RunWith(AndroidJUnit4.class)
public class ViewInteractionCaptureJavaTest {

  @Rule
  public ActivityScenarioRule<MainActivity> activityScenarioRule =
      new ActivityScenarioRule<>(MainActivity.class);

  @Test
  public void viewInteractionCapture() throws IOException {
    Bitmap bitmap = ViewInteractionCapture.captureToBitmap(onView(withId(R.id.layout)));

    assertThat(bitmap).isNotNull();

    BitmapStorage.writeToTestStorage(bitmap, "viewInteractionCapture");
  }
}
