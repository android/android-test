---
layout: page
title: Espresso Idling Resource
permalink: /docs/espresso/idling-resource/index.html
site_nav_category: docs
site_nav_category2: espresso
site_nav_category_order: 107
redirect_to:
 - https://developer.android.com/training/testing/espresso/idling-resource.html
---
{::options toc_levels="2"/}

* TOC
{:toc}

The centerpiece of Espresso is its ability to seamlessly synchronize all test operations with the application being tested. By default, Espresso waits for UI events in the current message queue to be handled and for default AsyncTasks to complete before it moves on to the next test operation.

However, there are instances where applications perform background operations (such as communicating with web services) via non-standard means; for example: direct creation and management of threads.

In such cases, you have to use **Idling Resources** to inform Espresso of the app's long-running operations.

## Creating and registering Idling Resources

You can implement the [IdlingResource]({{ site.sourceUrl }}espresso/idling-resource/src/main/java/android/support/test/espresso/IdlingResource.java) interface yourself or use an already-existing implementation, such as [CountingIdlingResource]({{ site.sourceUrl }}espresso/contrib/src/main/java/android/support/test/espresso/contrib/CountingIdlingResource.java), included in the `espresso-idling-resource` package.

This interface needs to be exposed to the test or created within it and injected into the application. Then, register one or more of your Idling Resource(s) with Espresso by calling [Espresso.registerIdlingResource()]({{ site.sourceUrl }}espresso/core/src/main/java/android/support/test/espresso/Espresso.java) in the test setup.

Check out the [Espresso Idling Resource sample](https://github.com/googlesamples/android-testing/tree/master/ui/espresso/IdlingResourceSample).

## Idling Resource approaches

There are two common ways to implement Idling Resources:

* **Counting running jobs**: When a job starts, increment a counter. When it finishes, decrement it. The app is idle if the counter is zero. This approach is very simple and accounts for most situations. [CountingIdlingResource]({{ site.sourceUrl }}espresso/contrib/src/main/java/android/support/test/espresso/contrib/CountingIdlingResource.java) does exactly this.
* **Querying state**: It might be more reliable to ask a work queue or an HTTP client (or whatever is doing the background work) if it is busy. If the state is exposed, implementing an Idling Resource is trivial.

Note that there are Idling Resource implementations for popular libraries, like [okhttp-idling-resource](https://github.com/JakeWharton/okhttp-idling-resource).
