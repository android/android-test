---
layout: page
title: JUnit4 Rules with the ATSL
permalink: /docs/rules/index.html
site_nav_category: docs
is_site_nav_category2: true
site_nav_category_order: 120
redirect_to:
 - https://developer.android.com/training/testing/junit-rules.html
---
{::options toc_levels="2"/}

* TOC
{:toc}

With the Android Testing Support Library we are providing a set of JUnit rules to be used with the `AndroidJUnitRunner`. JUnit rules provide more flexibility and reduce the boilerplate code required in tests.

`TestCase`s like `ActivityInstrumentationTestCase2` or `ServiceTestCase` are deprecated in favor of `ActivityTestRule` or `ServiceTestRule`.

## ActivityTestRule
This rule provides functional testing of a single activity. The activity under test will be launched before each test annotated with `@Test` and before any method annotated with `@Before`. It will be terminated after the test is completed and all methods annotated with `@After` are finished. The Activity under Test can be accessed during your test by calling `ActivityTestRule#getActivity()`.

{% highlight java %}
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MyClassTest {

    @Rule
    public ActivityTestRule<MyClass> mActivityRule = new ActivityTestRule(MyClass.class);

    @Test
    public void myClassMethod_ReturnsTrue() { ... }
}
{% endhighlight %}

## ServiceTestRule

This rule provides a simplified mechanism to start and shutdown your service before and after the duration of your test. It also guarantees that the service is successfully connected when starting (or binding to) a service. The service can be started (or bound) using one of the helper methods. It will automatically be stopped (or unbound) after the test completes and any methods annotated with `@After` are finished.

Note: This rule doesn't support `IntentService` because it's automatically destroyed when `IntentService#onHandleIntent(android.content.Intent)` finishes all outstanding commands. So there is no guarantee to establish a successful connection in a timely manner.

{% highlight java %}
@RunWith(AndroidJUnit4.class)
@MediumTest
public class MyServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testWithStartedService() {
        mServiceRule.startService(
            new Intent(InstrumentationRegistry.getTargetContext(), MyService.class));
        // test code
    }

    @Test
    public void testWithBoundService() {
        IBinder binder = mServiceRule.bindService(
            new Intent(InstrumentationRegistry.getTargetContext(), MyService.class));
        MyService service = ((MyService.LocalBinder) binder).getService();
        assertTrue("True wasn't returned", service.doSomethingToReturnTrue());
    }
}
{% endhighlight %}
