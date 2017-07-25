---
layout: page
title: Documentation
permalink: /docs/index.html
site_nav_category: docs
is_site_nav_category: true
site_nav_category_order: 10
redirect_to:
 - https://developer.android.com/training/testing/index.html
---
{::options toc_levels="2"/}

* TOC
{:toc}

## Espresso

Use Espresso to write concise, beautiful, and reliable Android UI tests like this:

{% highlight java %}
@Test
public void greeterSaysHello() {
  onView(withId(R.id.name_field))
    .perform(typeText("Steve"));
  onView(withId(R.id.greet_button))
    .perform(click());
  onView(withText("Hello Steve!"))
    .check(matches(isDisplayed()));
}
{% endhighlight %}

[Read more about Espresso]({{ site.baseurl }}/docs/espresso/index.html).

## Android JUnit Runner

AndroidJUnitRunner is a new unbundled test runner for Android. It features:

  * JUnit4 support
  * Instrumentation Registry
  * Test Filters
  * Test timeouts
  * Sharding of tests
  * [RunListener](http://junit.sourceforge.net/javadoc/org/junit/runner/notification/RunListener.html) support to hook into the test run life-cycle
  * Activity and Application life-cycle monitoring
  * Intent Monitoring and Stubbing

[Read more about the Android JUnit Runner]({{ site.baseurl }}/docs/androidjunitrunner-guide/index.html).

## JUnit4 Rules

With the Android Testing Support Library we provide a set of JUnit rules to be used with the [Android JUnit Runner]({{ site.baseurl }}/atsl/androidjunitrunner-guide/index.html). JUnit rules provide more flexibility and reduce the boilerplate code required in tests.

[Read more about the JUnit4 Rules]({{ site.baseurl }}/docs/rules/index.html).

## UI Automator

UI Automator is a UI testing framework suitable for cross-app functional UI testing across system and installed apps.

[Read more about UI Automator](https://developer.android.com/training/testing/ui-testing/uiautomator-testing.html)
