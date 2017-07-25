---
layout: page
title: Accessibility Checking
permalink: /docs/accesibility-checking/index.html
site_nav_category: docs
is_site_nav_category2: true
site_nav_category_order: 140
redirect_to:
 - https://developer.android.com/training/accessibility/testing.html
---
{::options toc_levels="2"/}

* TOC
{:toc}

The [AccessibilityChecks](http://developer.android.com/reference/android/support/test/espresso/contrib/AccessibilityChecks.html) class allows you to use your existing test code to test for accessibility issues. As a `View` is acted upon in tests, checks from the [accessibility test framework](https://code.google.com/p/eyes-free/source/browse/trunk/devtools/accessibility-test-framework/src/main/java/com/google/android/apps/common/testing/accessibility/framework) will be run automatically before proceeding. Simply import the class and add the following line of code to your setup methods annotated with `@Before`:


{% highlight java %}
import android.support.test.espresso.contrib.AccessibilityChecks;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccessibilityChecksIntegrationTest {
    @BeforeClass
    public static void enableAccessibilityChecks() {
        AccessibilityChecks.enable();
    }
}
{% endhighlight %}

This will cause accessibility checks to run on a given view every time a `ViewAction` from the ViewActions class is used. To instead run these checks on all views in the hierarchy, use:

{% highlight java %}
AccessibilityChecks.enable()
        .setRunChecksFromRootView(true);
{% endhighlight %}

When first turning on checks, you may encounter a number of issues you may not be willing or able to deal with immediately. You can suppress these errors by setting a matcher for the results that you would like to suppress. Matchers for [AccessibilityCheckResults](https://code.google.com/p/eyes-free/source/browse/trunk/devtools/accessibility-test-framework/src/main/java/com/google/android/apps/common/testing/accessibility/framework/AccessibilityCheckResultUtils.java) are provided in `AccessibilityCheckResultUtils` in the accessibility test framework. For example, to suppress all errors for a view with id `R.id.example_view`:

{% highlight java %}
AccessibilityChecks.enable()
        .setSuppressingResultMatcher(matchingViews(withId(R.id.example_view)));
{% endhighlight %}

For more advanced configuration of accessibility checking, see the [AccessibilityValidator](https://code.google.com/p/eyes-free/source/browse/trunk/devtools/accessibility-test-framework/src/main/java/com/google/android/apps/common/testing/accessibility/framework/integrations/espresso/AccessibilityValidator.java) class in the accessibility test framework.
