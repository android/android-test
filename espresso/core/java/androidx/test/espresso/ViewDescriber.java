package androidx.test.espresso;

import android.view.View;

/** Transforms an arbitrary view into a string with (hopefully) enough debug info. */
public interface ViewDescriber {

  public String describeView(View view);
}
