package androidx.test.eventto.fixtures;

import android.app.Activity;
import android.os.Bundle;

public class DelayedActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.delayed_activity);
  }
}
