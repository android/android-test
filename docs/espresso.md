---
layout: page
title: Espresso
permalink: /docs/espresso/index.html
site_nav_category_order: 100
is_site_nav_category2: true
site_nav_category: docs
---

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

The core API is small, predictable, and easy to learn and yet remains open for customization. Espresso tests state expectations, interactions, and assertions clearly without the distraction of boilerplate content, custom infrastructure, or messy implementation details getting in the way.

Espresso tests run optimally fast! It lets you leave your waits, syncs, sleeps, and polls behind while it manipulates and asserts on the application UI when it is at rest.

### Target Audience

Espresso is targeted at developers, who believe that automated testing is an integral part of the development lifecycle. While it can be used for black-box testing, Espresso's full power is unlocked by those who are familiar with the codebase under test.

### Packages

* **espresso-core** - Contains core and basic View matchers, actions, and assertions. See [Basics]({{ site.baseurl }}/docs/espresso/basics/index.html) and [Advanced Samples]({{ site.baseurl }}/docs/espresso/advanced/index.html).
* **[espresso-web]({{ site.baseurl }}/docs/espresso/web/index.html)** - Contains resources for WebView support.
* **[espresso-idling-resource]({{ site.baseurl }}/docs/espresso/idling-resource/index.html)** - Espresso's mechanism for synchronization with background jobs.
* **espresso-contrib** - External contributions that contain DatePicker, [RecyclerView]({{ site.baseurl }}/docs/espresso/lists/index.html#recyclerviews) and Drawer actions, Accessibility checks, and CountingIdlingResource.
* **[espresso-intents]({{ site.baseurl }}/docs/espresso/intents/index.html)** - Extention to validate and stub Intents, for hermetic testing.

The latest versions and release notes can be found in [Downloads]({{ site.baseurl }}/downloads/index.html).
