---
layout: page
title: Espresso Intents
permalink: /docs/espresso/intents/index.html
site_nav_category: docs
site_nav_category2: espresso
site_nav_category_order: 104
---
{::options toc_levels="2"/}

* TOC
{:toc}

Espresso-Intents is an extension to Espresso, which enables validation and stubbing of Intents sent out by the application under test. It’s like [Mockito](http://mockito.org/), but for Android Intents.

## Download Espresso-Intents ##
 * Make sure you have installed the *Android Support Repository* (see [instructions]({{ site.baseurl }}/downloads/index.html)).
 * Open your app's `build.gradle` file. This is usually not the top-level `build.gradle` file but `app/build.gradle`.

Add the following line inside dependencies:

{% highlight groovy %}
androidTestCompile 'com.android.support.test.espresso:espresso-intents:{{ site.espressoVersion }}'
{% endhighlight %}

Espresso-Intents is only compatible with Espresso 2.1+ and the testing support library 0.3 so make sure you update those lines as well:

{% highlight groovy %}
androidTestCompile 'com.android.support.test:runner:{{ site.atslVersion }}'
androidTestCompile 'com.android.support.test:rules:{{ site.atslVersion }}'
androidTestCompile 'com.android.support.test.espresso:espresso-core:{{ site.espressoVersion }}'
{% endhighlight %}

## IntentsTestRule ##
Use `IntentsTestRule` instead of `ActivityTestRule` when using Espresso-Intents.
`IntentsTestRule` makes it easy to use Espresso-Intents APIs in functional UI tests. This class is an extension of `ActivityTestRule`, which initializes Espresso-Intents before each test annotated with `@Test` and releases Espresso-Intents after each test run. The activity will be terminated after each test and this rule can be used in the same way as `ActivityTestRule`.

## Intent validation ##
Espresso-Intents records all intents that attempt to launch activities from the application under test. Using the intended API (cousin of *`Mockito.verify`*), you can assert that a given intent has been seen.

An example test that simply validates an outgoing intent:

{% highlight java %}
@Test
public void validateIntentSentToPackage() {
    // User action that results in an external "phone" activity being launched.
    user.clickOnView(system.getView(R.id.callButton));

    // Using a canned RecordedIntentMatcher to validate that an intent resolving
    // to the "phone" activity has been sent.
    intended(toPackage("com.android.phone"));
}
{% endhighlight %}

## Intent stubbing ##
Using the intending API (cousin of *`Mockito.when`*), you can provide a response for activities that are launched with startActivityForResult (this is particularly useful for external activities since we cannot manipulate the user interface of an external activity nor control the `ActivityResult` returned to the activity under test):

An example test with intent stubbing:

{% highlight java %}
@Test
public void activityResult_IsHandledProperly() {
    // Build a result to return when a particular activity is launched.
    Intent resultData = new Intent();
    String phoneNumber = "123-345-6789";
    resultData.putExtra("phone", phoneNumber);
    ActivityResult result = new ActivityResult(Activity.RESULT_OK, resultData);

    // Set up result stubbing when an intent sent to "contacts" is seen.
    intending(toPackage("com.android.contacts")).respondWith(result));

    // User action that results in "contacts" activity being launched.
    // Launching activity expects phoneNumber to be returned and displays it on the screen.
    onView(withId(R.id.pickButton)).perform(click());

    // Assert that data we set up above is shown.
    onView(withId(R.id.phoneNumber).check(matches(withText(phoneNumber)));
}
{% endhighlight %}

## Intent matchers ##
`intending` and `intended` methods take a hamcrest `Matcher<Intent>` as an argument. Hamcrest is library of matcher objects (also known as constraints or predicates). You have these options:

 * Use an existing intent matcher: Easiest option, which should almost always be preferred.
 * Implement your own intent matcher: Most flexible option (see the section entitled "Writing custom matchers" in the [Hamcrest tutorial](http://code.google.com/p/hamcrest/wiki/Tutorial))

An example of intent validation with existing Intent matchers:

{% highlight java %}

intended(allOf(
    hasAction(equalTo(Intent.ACTION_VIEW)),
    hasCategories(hasItem(equalTo(Intent.CATEGORY_BROWSABLE))),
    hasData(hasHost(equalTo("www.google.com"))),
    hasExtras(allOf(
        hasEntry(equalTo("key1"), equalTo("value1")),
        hasEntry(equalTo("key2"), equalTo("value2")))),
        toPackage("com.android.browser")));
{% endhighlight %}
