---
layout: page
title: Espresso and lists
permalink: /docs/espresso/lists/index.html
site_nav_category: docs
site_nav_category2: espresso
site_nav_category_order: 108
---
{::options toc_levels="2"/}

* TOC
{:toc}

When dealing with lists (those created with a `RecyclerView` or an `AdapterView`), the view that you’re interested in might not even be on the screen, because only a small number of children are displayed and are recycled as you scroll. The `scrollTo()` method can't be used in this case because it requires an existing view.

Espresso offers mechanisms to scroll to or act on a particular item for two types of lists: `AdapterView`s and `RecyclerView`s:

## AdapterViews like ListView, GridView, Spinner, etc.

Instead of using the `onView()` method, start your search with `onData()` and provide a matcher against the data that is backing the view you’d like to match. Espresso will do all the legwork of finding the row in the Adapter and bringing it into the viewport.


### Matching data using onData() and a custom ViewMatcher

The activity below contains a ListView, which is backed by a [SimpleAdapter](http://developer.android.com/reference/android/widget/SimpleAdapter.html) that holds data for each row in a `Map<String, Object>`.

![]({{ site.baseurl }}/docs/images/list_activity.png)

Each map has two entries: a key `"STR"` that contains a String (`"item: x"`) and a key `"LEN"` that contains an Integer, the length of the content. For example:

```
{"STR" : "item: 0", "LEN": 7}
```

The code for a click on the row with "item: 50" looks like this:

{% highlight java %}
onData(allOf(is(instanceOf(Map.class)), hasEntry(equalTo("STR"), is("item: 50")))
  .perform(click());
{% endhighlight %}

Note that Espresso will scroll automatically.

Let's take apart the `Matcher<Object>` inside `onData()`:

{% highlight java %}
is(instanceOf(Map.class))
{% endhighlight %}

narrows the search to any item of the AdapterView, which is backed by a Map.

In our case, this aspect of the query matches every row of the list view, but we want to click specifically on an item, so we narrow the search further with:

{% highlight java %}
hasEntry(equalTo("STR"), is("item: 50"))
{% endhighlight %}

This `Matcher<String, Object>` will match any `Map` that contains an entry with the key `"STR"` and the value `"item: 50"`. Because the code to look up this is long and we want to reuse it in other locations, let's write a custom "withItemContent" matcher for that.

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

We use a `BoundedMatcher` as a base because we want to be able to only match objects of the class `Map`. We override the `matchesSafely()` method, put in the matcher we found earlier, and match it against a `Matcher<String>` that can be passed as an argument. This allows us to call `withItemContent(equalTo("foo"))`. For code brevity, we create another matcher that already calls the `equalTo()` for us and accepts a String.

{% highlight java %}
public static Matcher<Object> withItemContent(String expectedText) {
  checkNotNull(expectedText);
  return withItemContent(equalTo(expectedText));
}
{% endhighlight %}

Now the code to click on the item is simple:

{% highlight java %}
onData(withItemContent("item: 50")).perform(click());
{% endhighlight %}

For the full code of this test, take a look at [AdapterViewTest#testClickOnItem50]({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/AdapterViewTest.java) and the [custom matcher]({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/LongListMatchers.java).

### Matching a specific child view of a view

The sample above issues a click in the middle of the entire row of a ListView. But what if we want to operate on a specific child of the row? For example, we would like to click on the second column of the row of the `LongListActivity`, which displays the `String.length` of the first row:

![]({{ site.baseurl }}/docs/images/item50.png)

Just add an `onChildView()` specification to your `DataInteraction`:

{% highlight java %}
onData(withItemContent("item: 60"))
  .onChildView(withId(R.id.item_size))
  .perform(click());
{% endhighlight %}

**Note**: This sample uses the `withItemContent()` matcher from the sample above. Take a look at [ApdaterViewTest#testClickOnSpecificChildOfRow60]({{ site.sourceUrl }}espresso/sample/src/androidTest/java/android/support/test/testapp/AdapterViewTest.java)!


## RecyclerViews

RecyclerViews work differently than AdapterViews so `onData()` cannot be used to interact with them.

In order to interact with RecyclerViews using Espresso, the `espresso-contrib` package has a collection of [RecyclerViewActions](https://developer.android.com/reference/android/support/test/espresso/contrib/RecyclerViewActions.html) that can be used to scroll to positions or to perform actions on items:


* ``scrollTo`` - Scrolls to the matched View.
* ``scrollToHolder`` - Scrolls to the matched View Holder.
* ``scrollToPosition`` - Scrolls to a specific position.
* ``actionOnHolderItem`` - Performs a View Action on a matched View Holder.
* ``actionOnItem``  - Performs a View Action on a matched View.
* ``actionOnItemAtPosition`` - Performs a ViewAction on a view at a specific position.

Some examples from Espresso's [RecyclerViewSample](https://github.com/googlesamples/android-testing/blob/master/ui/espresso/RecyclerViewSample):

{% highlight java %}
    @Test
    public void scrollToItemBelowFold_checkItsText() {
        // First scroll to the position that needs to be matched and click on it.
        onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(ITEM_BELOW_THE_FOLD, click()));

        // Match the text in an item below the fold and check that it's displayed.
        String itemElementText = mActivityRule.getActivity().getResources().getString(
                R.string.item_element_text) + String.valueOf(ITEM_BELOW_THE_FOLD);
        onView(withText(itemElementText)).check(matches(isDisplayed()));
    }
{% endhighlight %}


{% highlight java %}
    @Test
    public void itemInMiddleOfList_hasSpecialText() {
        // First, scroll to the view holder using the isInTheMiddle matcher.
        onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToHolder(isInTheMiddle()));

        // Check that the item has the special text.
        String middleElementText =
                mActivityRule.getActivity().getResources().getString(R.string.middle);
        onView(withText(middleElementText)).check(matches(isDisplayed()));
    }
{% endhighlight %}
