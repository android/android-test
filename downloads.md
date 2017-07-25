---
layout: page
title: Downloads
permalink: /downloads/index.html
site_nav_category_order: 200
is_site_nav_category: true
site_nav_category: downloads
redirect_to:
 - https://developer.android.com/topic/libraries/testing-support-library/packages.html
---

## Download the Android Support Repository

First, make sure the latest version of the **Android Support Repository** is downloaded via the SDK Manager or Android Studio.

![Android Support Repository screenshot]({{ site.baseurl }}/assets/sdk.png)

*Android SDK Manager in Android Studio*

## Gradle dependencies

Add these to your app's `build.gradle` file. Usually in `app/build.gradle`.

{% highlight groovy %}
// Android JUnit Runner
androidTestCompile 'com.android.support.test:runner:{{ site.atslVersion }}'

// JUnit4 Rules
androidTestCompile 'com.android.support.test:rules:{{ site.atslVersion }}'

// Espresso core
androidTestCompile 'com.android.support.test.espresso:espresso-core:{{ site.espressoVersion }}'
// Espresso-contrib for DatePicker, RecyclerView, Drawer actions, Accessibility checks, CountingIdlingResource
androidTestCompile 'com.android.support.test.espresso:espresso-contrib:{{ site.espressoVersion }}'
// Espresso-web for WebView support
androidTestCompile 'com.android.support.test.espresso:espresso-web:{{ site.espressoVersion }}'
// Espresso-idling-resource for synchronization with background jobs
androidTestCompile 'com.android.support.test.espresso:espresso-idling-resource:{{ site.espressoVersion }}'
// Espresso-intents for validation and stubbing of Intents
androidTestCompile 'com.android.support.test.espresso:espresso-intents:{{ site.espressoVersion }}'

// UiAutomator
androidTestCompile 'com.android.support.test.uiautomator:uiautomator-v18:{{ site.uiautomatorVersion }}'

{% endhighlight %}


## Static jars

For projects that don't use the Gradle build system:

- [ATSL JAR files with dependencies](https://github.com/googlesamples/android-testing/tree/master/ui/espresso/BasicSampleBundled/libs)
