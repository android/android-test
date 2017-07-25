---
layout: page
title: Espresso Web
permalink: /docs/espresso/web/index.html
site_nav_category: docs
site_nav_category2: espresso
site_nav_category_order: 106
redirect_to:
 - https://developer.android.com/training/testing/espresso/web.html
---
{::options toc_levels="2"/}

* TOC
{:toc}

Espresso-web is an entry point to work with WebViews on Android. It uses Atoms from the popular WebDriver API to introspect into and control the behavior of a WebView.

Similar to onData, WebView interactions are actually composed of several View Atoms. An Atom can be seen as a ViewAction, a self contained unit which performs an action in your UI. However, they need to be properly orchestrated and are quite verbose. Web and WebInteraction wrap this boilerplate and give an Espresso-like feel to interacting with WebViews.

WebView interactions constantly cross the Java/Javascript boundary to do their work, since there is no chance of introducing race conditions by exposing data from the Javascript environment (everything Espresso sees on the Java side is an isolated copy), returning data from WebInteractions is fully supported.

### Common WebInteractions

* `withElement(ElementReference)` will supply the `ElementReference` to the Atom.
Example:

{% highlight java %}
    onWebView().withElement(findElement(Locator.ID, "teacher"))
{% endhighlight %}
* `withContextualElement(Atom<ElementReference>)` will supply the ElementReference to the Atom.
Example:

{% highlight java %}
onWebView()
  .withElement(findElement(Locator.ID, "teacher"))
  .withContextualElement(findElement(Locator.ID, "person_name"))
{% endhighlight %}

* `check(WebAssertion)` will evaluate an assertion.
Example:
{% highlight java %}
    onWebView()
      .withElement(findElement(Locator.ID, "teacher"))
      .withContextualElement(findElement(Locator.ID, "person_name"))
      .check(webMatches(getText(), containsString("Socrates")));
{% endhighlight %}
* `perform(Atom)` will execute the provided atom within the current context.
Example:

{% highlight java %}
    onWebView()
      .withElement(findElement(Locator.ID, "teacher"))
      .perform(webClick());
{% endhighlight %}

* `reset()` is necessary when a prior action (for example a click) introduces a navigation change that invalidates the ElementReference and WindowReference pointers.

### WebView example
Javascript has to be enabled for Espresso web to control the WebView. You can force it by overriding afterActivityLaunched in the ActivityTestRule:

{% highlight java %}
@Rule
public ActivityTestRule<WebViewActivity> mActivityRule = new ActivityTestRule<WebViewActivity>(WebViewActivity.class, false, false) {
    @Override
    protected void afterActivityLaunched() {
        // Enable JS!
        onWebView().forceJavascriptEnabled();
    }
}

@Test
public void typeTextInInput_clickButton_SubmitsForm() {
   // Lazily launch the Activity with a custom start Intent per test
   mActivityRule.launchActivity(withWebFormIntent());

   // Selects the WebView in your layout. If you have multiple WebViews you can also use a
   // matcher to select a given WebView, onWebView(withId(R.id.web_view)).
   onWebView()
           // Find the input element by ID
           .withElement(findElement(Locator.ID, "text_input"))
           // Clear previous input
           .perform(clearElement())
           // Enter text into the input element
           .perform(DriverAtoms.webKeys(MACCHIATO))
           // Find the submit button
           .withElement(findElement(Locator.ID, "submitBtn"))
           // Simulate a click via javascript
           .perform(webClick())
           // Find the response element by ID
           .withElement(findElement(Locator.ID, "response"))
           // Verify that the response page contains the entered text
           .check(webMatches(getText(), containsString(MACCHIATO)));
}

{% endhighlight %}

See the [Espresso Web sample](https://github.com/googlesamples/android-testing/tree/master/ui/espresso/WebBasicSample) on GitHub.

## Download Espresso-Web ##
 * Make sure you have installed the *Android Support Repository* (see [instructions]({{ site.baseurl }}/downloads/index.html)).
 * Open your app's `build.gradle` file. This is usually not the top-level `build.gradle` file but `app/build.gradle`.

Add the following line inside dependencies:

{% highlight groovy %}
androidTestCompile 'com.android.support.test.espresso:espresso-web:{{ site.espressoVersion }}'
{% endhighlight %}

Espresso-Web is only compatible with Espresso 2.2+ and the testing support library 0.3+ so make sure you update those lines as well:

{% highlight groovy %}
androidTestCompile 'com.android.support.test:runner:{{ site.atslVersion }}'
androidTestCompile 'com.android.support.test:rules:{{ site.atslVersion }}'
androidTestCompile 'com.android.support.test.espresso:espresso-core:{{ site.espressoVersion }}'
{% endhighlight %}
