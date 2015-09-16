---
layout: page
title: Espresso Advanced Samples
permalink: /docs/espresso/advanced/index.html
site_nav_category: docs
site_nav_category2: espresso
site_nav_category_order: 105
---
{::options toc_levels="3"/}

* TOC
{:toc}

## ViewMatchers

### Matching a view next to another view

A layout could contain certain views that are not unique by themselves (e.g. a repeating call button in a table of contacts could have the same R.id, contain the same text and have the same properties as other call buttons within the view hierarchy).

For example, in this activity, the view with text "7" repeats across multiple rows:


![has sibling]({{ site.baseurl }}/docs/images/hasSibling.png)

Often, the non-unique view will be paired with some unique label that's located next to it (e.g. a name of the contact next to the call button). In this case, you can use the hasSibling matcher to narrow down your selection:

{% highlight java %}
onView(allOf(withText("7"), hasSibling(withText("item: 0"))))
  .perform(click());
{% endhighlight %}

### Matching data using onData and a custom ViewMatcher

The activity below contains a ListView, which is backed by a [SimpleAdapter](http://developer.android.com/reference/android/widget/SimpleAdapter.html) that holds data for each row in a `Map<String, Object>`. Each map has an entry with key `"STR"` that contains the content (string, "item: x") and a key `"LEN"` that contains an Integer, the length of the content.

![]({{ site.baseurl }}/docs/images/list_activity.png)

The code for a click on the row with "item: 50" looks like this:

{% highlight java %}
onData(allOf(is(instanceOf(Map.class)), hasEntry(equalTo("STR"), is("item: 50")))
  .perform(click());
{% endhighlight %}

Let's take apart the `Matcher<Object>` inside `onData`:

{% highlight java %}
is(instanceOf(Map.class))
{% endhighlight %}

narrows the search to any item of the AdapterView, which is a Map.

In our case, this is every row of the list view, but we want to click specifically on "item: 50", so we narrow the search further with:

{% highlight java %}
hasEntry(equalTo("STR"), is("item: 50"))
{% endhighlight %}

This Matcher<String, Object> will match any Map that contains an entry with any key and value = "item: 50". As the code to look up this is long and we want to reuse it in other locations - let us write a custom "withItemContent" matcher for that.

{% highlight java %}
  return new BoundedMatcher<Object, Map>(Map.class) {
    @Override
    public boolean matchesSafely(Map map) {
      return hasEntry(equalTo("STR"), itemTextMatcher).matches(map);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with item content: ");
      itemTextMatcher.describeTo(description);
    }
  };
}
{% endhighlight %}

We use a `BoundedMatcher` as a base because we want to be able to only match on objects of class `Map`. We override the matchesSafely method, put in the matcher we found earlier and match it against a `Matcher<String>` that can be passed as an argument. This allows us to do `withItemContent(equalTo("foo"))`. For code brevity, we create another matcher that already does the equalTo for us and accepts a String.

{% highlight java %}
public static Matcher<Object> withItemContent(String expectedText) {
  checkNotNull(expectedText);
  return withItemContent(equalTo(expectedText));
}
{% endhighlight %}

Now the code to click on the item is simple:

{% highlight java %}
onData(withItemContent("item: 50")) .perform(click());
{% endhighlight %}

For the full code of this test, take a look at [AdapterViewTest#testClickOnItem50]({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/AdapterViewTest.java) and the [custom matcher]({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/LongListMatchers.java).

### Matching a specific child view of a view

The sample above issues a click in the middle of the entire row of a ListView. But what if we want to operate on a specific child of the row? For example, we would like to click on the second column of the row of the LongListActivity, which displays the `String.length` of the first row (to make this less abstract, you can imagine the G+ app that shows a list of comments and each comment has a +1 button next to it):

![]({{ site.baseurl }}/docs/images/item50.png)

Just add an `onChildView` specification to your `DataInteraction`:

{% highlight java %}
onData(withItemContent("item: 60"))
  .onChildView(withId(R.id.item_size))
  .perform(click());
{% endhighlight %}

**Note**: This sample uses the ``withItemContent` matcher from the sample above it! Take a look at [ApdaterViewTest#testClickOnSpecificChildOfRow60]({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/AdapterViewTest.java)!

### Matching a view that is a footer/header in a ListView

Headers and footers are added to ListViews via the addHeaderView/addFooterView APIs. To load them using Espresso.onData, make sure to set the data object (second param) to a preset value. For example:

{% highlight java %}
public static final String FOOTER = "FOOTER";
...
View footerView = layoutInflater.inflate(R.layout.list_item, listView, false);
((TextView) footerView.findViewById(R.id.item_content)).setText("count:");
((TextView) footerView.findViewById(R.id.item_size)).setText(String.valueOf(data.size()));
listView.addFooterView(footerView, FOOTER, true);
{% endhighlight %}

Then, you can write a matcher that matches this object:

{% highlight java %}
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("unchecked")
public static Matcher<Object> isFooter() {
  return allOf(is(instanceOf(String.class)), is(LongListActivity.FOOTER));
}
{% endhighlight %}

And loading the view in a test is trivial:

{% highlight java %}
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.sample.LongListMatchers.isFooter;

public void testClickFooter() {
  onData(isFooter())
    .perform(click());
  ...
}
{% endhighlight %}

Take a look at the full code sample at: [AdapterViewtest#testClickFooter](({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/AdapterViewTest.java)

### Matching a view that is inside an ActionBar

The [ActionBarTestActivity](({{ site.sourceUrl }}espresso/sample/src/main/java/android/support/test/testapp/ActionBarTestActivity.java) has two different action bars: a normal ActionBar and a contextual action bar that is created from a [options menu](http://developer.android.com/guide/topics/ui/menus.html#options-menu). Both action bars have one item that is always visible and two items that are only visible in overflow menu. When an item is clicked, it changes a TextView to the content of the clicked item.

Matching visible icons on both of the action bars is easy:

{% highlight java %}
public void testClickActionBarItem() {
  // We make sure the contextual action bar is hidden.
  onView(withId(R.id.hide_contextual_action_bar))
    .perform(click());

  // Click on the icon - we can find it by the r.Id.
  onView(withId(R.id.action_save))
    .perform(click());

  // Verify that we have really clicked on the icon by checking the TextView content.
  onView(withId(R.id.text_action_bar_result))
    .check(matches(withText("Save")));
}
{% endhighlight %}

![]({{ site.baseurl }}/docs/images/actionbar_normal_icon.png)

The code looks identical for the contextual action bar:

{% highlight java %}
public void testClickActionModeItem() {
  // Make sure we show the contextual action bar.
  onView(withId(R.id.show_contextual_action_bar))
    .perform(click());

  // Click on the icon.
  onView((withId(R.id.action_lock)))
    .perform(click());

  // Verify that we have really clicked on the icon by checking the TextView content.
  onView(withId(R.id.text_action_bar_result))
    .check(matches(withText("Lock")));
}
{% endhighlight %}

![]({{ site.baseurl }}/docs/images/actionbar_contextual_icon.png)

Clicking on items in the overflow menu is a bit trickier for the normal action bar as some devices have a hardware overflow menu button (they will open the overflowing items in an options menu) and some devices have a software overflow menu button (they will open a normal overflow menu). Luckily, Espresso handles that for us.

For the normal action bar:



{% highlight java %}
public void testActionBarOverflow() {
  // Make sure we hide the contextual action bar.
  onView(withId(R.id.hide_contextual_action_bar))
    .perform(click());

  // Open the overflow menu OR open the options menu,
  // depending on if the device has a hardware or software overflow menu button.
  openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

  // Click the item.
  onView(withText("World"))
    .perform(click());

  // Verify that we have really clicked on the icon by checking the TextView content.
  onView(withId(R.id.text_action_bar_result))
    .check(matches(withText("World")));
}
{% endhighlight %}

![]({{ site.baseurl }}/docs/images/actionbar_normal_hidden_overflow.png)

This is how this looks on devices with a hardware overflow menu button:

![]({{ site.baseurl }}/docs/images/actionbar_normal_hidden_no_overflow.png)

For the contextual action bar it is really easy again:

{% highlight java %}
public void testActionModeOverflow() {
  // Show the contextual action bar.
  onView(withId(R.id.show_contextual_action_bar))
    .perform(click());

  // Open the overflow menu from contextual action mode.
  openContextualActionModeOverflowMenu();

  // Click on the item.
  onView(withText("Key"))
    .perform(click());

  // Verify that we have really clicked on the icon by checking the TextView content.
  onView(withId(R.id.text_action_bar_result))
    .check(matches(withText("Key")));
  }
{% endhighlight %}

![]({{ site.baseurl }}/docs/images/actionbar_contextual_hidden.png)

See the full code for these samples: [ActionBarTest.java](({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/ActionBarTest.java).

## ViewAssertions

### Asserting that a view is not displayed

After performing a series of actions, you will certainly want to assert the state of the UI under test. Sometimes, this may be a negative case (for example, something is not happening). Keep in mind that you can turn any hamcrest view matcher into a ViewAssertion by using ViewAssertions.matches.

In the example below, we take the isDisplayed matcher and reverse it using the standard "not" matcher:

{% highlight java %}
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

onView(withId(R.id.bottom_left))
  .check(matches(not(isDisplayed())));
{% endhighlight %}

The above approach works if the view is still part of the hierarchy. If it is not, you will get a `NoMatchingViewException` and you need to use `ViewAssertions.doesNotExist` (see below).

### Asserting that a view is not present

If the view is gone from the view hierarchy (e.g. this may happen if an action caused a transition to another activity), you should use `ViewAssertions.doesNotExist`:

{% highlight java %}
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.doesNotExist;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

onView(withId(R.id.bottom_left))
  .check(doesNotExist());
{% endhighlight %}

### Asserting that a data item is not in an adapter

To prove a particular data item is not within an AdapterView you have to do things a little differently. We have to find the AdapterView we're interested in and interrogate the data its holding. We don't need to use onData(). Instead, we use onView to find the AdapterView and then use another matcher to work on the data inside the view.

First the matcher:

{% highlight java %}
private static Matcher<View> withAdaptedData(final Matcher<Object> dataMatcher) {
  return new TypeSafeMatcher<View>() {

    @Override
    public void describeTo(Description description) {
      description.appendText("with class name: ");
      dataMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
      if (!(view instanceof AdapterView)) {
        return false;
      }
      @SuppressWarnings("rawtypes")
      Adapter adapter = ((AdapterView) view).getAdapter();
      for (int i = 0; i < adapter.getCount(); i++) {
        if (dataMatcher.matches(adapter.getItem(i))) {
          return true;
        }
      }
      return false;
    }
  };
}
{% endhighlight %}

Then the all we need is an onView that finds the AdapterView:

{% highlight java %}
@SuppressWarnings("unchecked")
public void testDataItemNotInAdapter(){
  onView(withId(R.id.list))
      .check(matches(not(withAdaptedData(withItemContent("item: 168")))));
  }
{% endhighlight %}

And we have an assertion that will fail if an item that is equal to "item: 168" exists in an adapter view with the id list.

For the full sample look at [AdapterViewTest#testDataItemNotInAdapter](({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/AdapterViewTest.java).

## Idling resources

### Using registerIdlingResource to synchronize with custom resources

The centerpiece of Espresso is its ability to seamlessly synchronize all test operations with the application under test. By default, Espresso waits for UI events in the current message queue to process and default AsyncTasks* to complete before it moves on to the next test operation. This should address the majority of application/test synchronization in your application.

However, there are instances where applications perform background operations (such as communicating with web services) via non-standard means; for example: creation and management of threads directly and the use of custom services.

In such cases, the first thing we suggest is to put on your testability hat and ask whether the user of non-standard background operations is warranted. In some cases, it may have happened due to poor understanding of Android and the application could benefit from refactoring (for example, by converting custom creation of threads to AsyncTasks). However, sometimes refactoring is not possible. The good news? Espresso can still synchronize test operations with your custom resources.

Here's what you need to do:

* Implement the [IdlingResource](({{ site.sourceUrl }}espresso/idling-resource/src/main/java/android/support/test/espresso/IdlingResource.java) interface and expose it to your test.
* Register one or more of your IdlingResource(s) with Espresso by calling [Espresso.registerIdlingResource](({{ site.sourceUrl }}core/src/main/java/android/support/test/espresso/Espresso.java) in test setup.

To see how IdlingResource can be used take a look at the [AdvancedSynchronizationTest](({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/AdvancedSynchronizationTest.java) and the [CountingIdlingResource](({{ site.sourceUrl }}espresso/contrib/src/main/java/android/support/test/espresso/contrib/CountingIdlingResource.java) class.

Note that the IdlingResource interface is implemented in your app under test so you need to add dependencies carefully:

{% highlight groovy %}
// IdlingResource is used in the app under test
compile 'com.android.support.test.espresso:espresso-idling-resource:{{ site.espressoVersion }}'

// For CountingIdlingResource:
compile 'com.android.support.test.espresso:espresso-contrib:{{ site.espressoVersion }}'
{% endhighlight %}


## Customization

### Using a custom failure handler

Replacing the default FailureHandler of Espresso with a custom one allows for additional (or different) error handling - e.g. taking a screenshot or dumping extra debug information.

The [CustomFailureHandlerTest](({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/CustomFailureHandlerTest.java?autodive=0%2F%2F%2F%2F) example demonstrates how to implement a custom failure handler:

{% highlight java %}
private static class CustomFailureHandler implements FailureHandler {
  private final FailureHandler delegate;

  public CustomFailureHandler(Context targetContext) {
    delegate = new DefaultFailureHandler(targetContext);
  }

  @Override
  public void handle(Throwable error, Matcher<View> viewMatcher) {
    try {
      delegate.handle(error, viewMatcher);
    } catch (NoMatchingViewException e) {
      throw new MySpecialException(e);
    }
  }
}
{% endhighlight %}

This failure handler throws a MySpecialException instead of a NoMatchingViewException and delegates all other failures to the DefaultFailureHandler. The CustomFailureHandler can be registered with Espresso in the setUp() of the test:

{% highlight java %}
@Override
public void setUp() throws Exception {
  super.setUp();
  getActivity();
  setFailureHandler(new CustomFailureHandler(getInstrumentation().getTargetContext()));
}
{% endhighlight %}

For more information see the [FailureHandler](({{ site.sourceUrl }}espresso/core/src/main/java/android/support/test/espresso/FailureHandler.java) interface and [Espresso.setFailureHandler](({{ site.sourceUrl }}espresso/core/src/main/java/android/support/test/espresso/Espresso.java).

## inRoot

### Using inRoot to target non-default windows

Surprising, but true - Android supports multiple [windows](http://developer.android.com/reference/android/view/Window.html). Normally, this is transparent (pun intended) to the users and the app developer, yet in certain cases multiple windows are visible (e.g. an auto-complete window gets drawn over the main application window in the search widget). To simplify your life, by default Espresso uses a heuristic to guess which Window you intend to interact with. This heuristic is almost always "good enough"; however, in rare cases, you'll need to specify which window an interaction should target. You can do this by providing your own root window (aka [Root](({{ site.sourceUrl }}espresso/core/src/main/java/android/support/test/espresso/Root.java)) matcher:

{% highlight java %}
onView(withText("South China Sea"))
  .inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView()))))
  .perform(click());
{% endhighlight %}

As is the case with [ViewMatchers](({{ site.sourceUrl }}espresso/core/src/main/java/android/support/test/espresso/matcher/ViewMatchers.java), we provide a set of pre-canned [RootMatchers](({{ site.sourceUrl }}espresso/core/src/main/java/android/support/test/espresso/matcher/RootMatchers.java). Of course, you can always implement your own Matcher<Root>.

Take a look at the [sample](({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/MultipleWindowTest.java) or the [sample on GitHub](https://github.com/googlesamples/android-testing/tree/master/ui/espresso/MultiWindowSample).
