---
layout: page
title: Contribute
permalink: /contribute/index.html
site_nav_category: contribute
site_nav_category_order: 400
is_site_nav_category: true
---

Contributions to this website and the *Android Testing Support Library* are welcome and appreciated!

## Contributing to this website
To contribute to this website, feel free to create pull requests for small fixes. For bigger contributions we recommend to start an issue on the [ATSL issue tracker](https://github.com/google/android-testing-support-library/issues) first.

## Contributing to the Android Testing Support Library
Contributions to the *Android Testing Support Library* are very important to us. They are a great way for you to work on an actual Android product and have an impact on tens of thousands of Android applications. Contributions will usually be available in the next ATSL release.

### One time setup

 * [Initialize your build environment](https://source.android.com/source/initializing.html)

 * [Install Repo tool](https://source.android.com/source/downloading.html)

### Getting the latest source (Command Line)

If you haven’t done so already create a working directory:
{% highlight java %}
  mkdir android-support-test; cd android-support-test
{% endhighlight %}

Checkout *android-support-test* branch:
{% highlight java %}
  repo init -u https://android.googlesource.com/platform/manifest -b android-support-test
{% endhighlight %}

Synchronize the files for all available projects:
{% highlight java %}
  repo sync -j8
{% endhighlight %}

Source locations:
{% highlight java %}
frameworks/testing/runner
frameworks/testing/rules
frameworks/testing/espresso
frameworks/uiautomator
{% endhighlight %}

### Building and Testing

#### Via Android Studio: (recommended)

In *Android Studio* go to `File -> Open…` and select the `build.gradle` file located in the root folder:
{% highlight java %}
  ~/android-support-test/build.gradle
{% endhighlight %}

At this point all *Android Test Support Library* projects should be loaded into *Android Studio*. You should be able to build and run the tests as you would run in your app.
More details can be found [here](http://developer.android.com/tools/building/building-studio.html)

#### Via Command Line:

From the root directory:
{% highlight java %}
  ~/android-support-test/
{% endhighlight %}

Build debug build type for all projects:
{% highlight java %}
  ./gradlew assembleDebug
{% endhighlight %}

Run tests for all projects:
{% highlight java %}
  ./gradlew connectedAndroidTest
{% endhighlight %}

**Note:** When running `connectedAndroidTest` you might get an “Unable to locate adb” error. To run your tests you need to add the following line to your `local.properties` file:

Run tests for all projects:
{% highlight java %}
  sdk.dir=/path/to/sdk/dir
{% endhighlight %}

You can also build, test or execute any *Gradle* task for a specific project by prepending the project name in front of the desired task. For instance:
{% highlight java %}
./gradlew runner:connectedAndroidTest
./gradlew rules:connectedAndroidTest
./gradlew espresso:connectedAndroidTest
./gradlew uiautomator-v18:connectedAndroidTest
{% endhighlight %}

For a list of available *Gradle* commands type:
{% highlight java %}
  ./gradlew tasks
{% endhighlight %}

### Contributing your work

Read the following instructions on how to contribute to an open source project:

* [Code Style Guidelines for Contributors](https://source.android.com/source/code-style.html)
* [Life of a Patch](https://source.android.com/source/life-of-a-patch.html)
* [Submitting Patches](https://source.android.com/source/submit-patches.html)

**We are looking forward to all of your contributions!**
