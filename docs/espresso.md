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

### Backward Compatibility

Espresso is supported on the following APIs:

|*Codename*|*API*|
|Froyo|8|
|Gingerbread|10|
|Ice Cream Sandwich|15|
|Jelly Bean|16, 17 ,18|
|KitKat|19|
|Lollipop|21|

#### Notes:

- We use the [platform versions dashboard](http://developer.android.com/about/dashboards/index.html#Platform) to decide which APIs are supported. As the number of users on older API levels falls off, we will deprecate support for those API levels (Froyo is almost there).
- Future versions of Android will be supported.
