package androidx.test.espresso;

import android.view.View;

/**
 * DO NOT SUBMIT
 */
public interface ViewAssertionWithUiController extends ViewAssertion {

  void check(View view, UiController uiController, NoMatchingViewException noViewFoundException);
}
