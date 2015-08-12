---
layout: page
title: Espresso setup instructions
permalink: /docs/espresso/setup/index.html
site_nav_category: docs
site_nav_category2: espresso
site_nav_category_order: 101
---
{::options toc_levels="2"/}

* TOC
{:toc}

This guide covers installing Espresso using the SDK Manager and building it using Gradle. Android Studio is recommended.

## Setup your test environment

To avoid flakiness, we highly recommend that you *turn off system animations* on the virtual or physical device(s) used for testing.
- On your device, under *Settings*->*Developer options* disable the following 3 settings:
  - Window animation scale
  - Transition animation scale
  - Animator duration scale

## Download Espresso

* Make sure you have installed the latest *Android Support Repository* under *Extras* (see [instructions]({{ site.baseurl }}/downloads/index.html)).
* Open your app's `build.gradle` file. This is usually not the top-level `build.gradle` file but `app/build.gradle`.
* Add the following lines inside dependencies:
{% highlight groovy %}
androidTestCompile 'com.android.support.test.espresso:espresso-core:{{ site.espressoVersion }}'
androidTestCompile 'com.android.support.test:runner:{{ site.atslVersion }}'
{% endhighlight %}

  * See the [downloads]({{ site.baseurl }}/downloads/index.html) section for more artifacts (espresso-contrib, espresso-web, etc.)

## Set the instrumentation runner

  * Add to the same build.gradle file the following line in `android.defaultConfig`:
{% highlight groovy %}
testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
{% endhighlight %}

## Example build.gradle file

{% highlight groovy %}

apply plugin: 'com.android.application'

android {
    compileSdkVersion 22
    buildToolsVersion "22"

    defaultConfig {
        applicationId "com.my.awesome.app"
        minSdkVersion 10
        targetSdkVersion 22.0.1
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    // App's dependencies, including test
    compile 'com.android.support:support-annotations:22.2.0'

    // Testing-only dependencies
    androidTestCompile 'com.android.support.test:runner:{{ site.atslVersion }}'
    androidTestCompile 'com.android.support.test.espresso:espresso-core:{{ site.espressoVersion }}'
}

{% endhighlight %}

## Analytics

In order to make sure we are on the right track with each new release, the test runner collects analytics. More specifically, it uploads a hash of the package name of the application under test for each invocation. This allows us to measure both the count of unique packages using Espresso as well as the volume of usage.

If you do not wish to upload this data, you can opt out by passing the following argument to the test runner: `disableAnalytics "true"` (see [how to pass custom arguments](https://github.com/googlesamples/android-testing-templates/tree/master/AndroidTestingBlueprint#custom-gradle-command-line-arguments)).

## Add the first test

Android Studio creates tests by default in `src/androidTest/java/com.example.package/`

Example JUnit4 test using Rules:

{% highlight groovy %}
@RunWith(AndroidJUnit4.class)
@LargeTest
public class HelloWorldEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule(MainActivity.class);

    public void listGoesOverTheFold() {
        onView(withText("Hello world!")).check(matches(isDisplayed()));
    }
}
{% endhighlight %}

## Running tests

### In Android Studio

Create a test configuration

In Android Studio:

* Open *Run menu* -> *Edit Configurations*
* Add a new *Android Tests* configuration
* Choose a module
* Add a specific instrumentation runner:

  {% highlight groovy %}
  android.support.test.runner.AndroidJUnitRunner
  {% endhighlight %}

Run the newly created configuration.

### From command-line via Gradle

Execute

{% highlight bash %}
./gradlew connectedAndroidTest
{% endhighlight %}
