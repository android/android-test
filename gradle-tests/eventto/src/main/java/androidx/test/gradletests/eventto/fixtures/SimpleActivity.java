package androidx.test.gradletests.eventto.fixtures;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.gradletests.eventto.R;

import java.util.Random;

/** Fixture activity for {@link EventtoTest} */
public class SimpleActivity extends Activity {

  EditText editText;
  Button button;
  public boolean buttonClicked;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.simple_activity);

    editText = findViewById(R.id.edit_text);
    // Disable auto-correct for EditText to avoid typed text is changed
    // by these features when running tests.
    editText.setInputType(editText.getInputType() & (~InputType.TYPE_TEXT_FLAG_AUTO_CORRECT));

    button = findViewById(R.id.button);
    button.setOnClickListener(
        view -> {
          buttonClicked = true;
         // Intent delayedActivityIntent = new Intent(this, DelayedActivity.class);
         // this.startActivity(delayedActivityIntent);
        });

    int randomDelay = new Random().nextInt(200);
    Handler mainHandler = new Handler(Looper.getMainLooper());
    mainHandler.postDelayed(() -> {
      TextView textView = findViewById(R.id.text_view);
      textView.setText("Delayed update");
    }, 1000);
  }


}
