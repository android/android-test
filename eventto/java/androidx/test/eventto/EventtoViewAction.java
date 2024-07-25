package androidx.test.eventto;

import android.view.View;
import org.hamcrest.Matcher;

public interface EventtoViewAction {

  /**
   * A mechanism for ViewActions to specify what type of views they can operate on.
   *
   * <p>A ViewAction can demand that the view passed to perform meets certain constraints. For
   * example it may want to ensure the view is already in the viewable physical screen of the device
   * or is of a certain type.
   *
   * @return a <a href="http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/Matcher.html">
   *     <code>Matcher</code></a> that will be tested prior to calling perform.
   */
  public Matcher<View> getConstraints();

  /**
   * Returns a description of the view action. The description should not be overly long and should
   * fit nicely in a sentence like: "performing %description% action on view with id ..."
   */
  public String getDescription();

  /**
   * Performs this action on the given view.
   *
   * @param view the view to act upon. never null.
   */
  public void perform(View view);
}
