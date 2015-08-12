---
layout: page
title: Release notes
permalink: /downloads/release-notes/index.html
site_nav_category_order: 201
is_site_nav_category2: true
site_nav_category: downloads
---

### Espresso 2.2.1, Runner/Rules 0.4 (2015-09-14)

--------

#### New Features
  * *runner*:
    * Added special case multidex installation for API <= 15
    * Added exclude filters to class and package:
      * Running all tests except those in a particular class: `adb shell am instrument -w -e notClass com.android.foo.FooTest`
      * Running all but a single test: `adb shell am instrument -w -e notClass com.android.foo.FooTest#testFoo`
      * Running all tests except a particular package: `adb shell am instrument -w -e notPackage com.android.foo.bar`

  * *rules*:
    * Added new `IntentsTestRule` constructor to be fully compatible with `ActivityTestRule`

#### External contributions
* *espresso*
  * [157911](https://android-review.googlesource.com/#/c/157911/): Add view matcher for input type on an `EditText`
  * [157912](https://android-review.googlesource.com/#/c/157912/): Add view matcher for matching error text on an `EditText`
  * [150674](https://android-review.googlesource.com/#/c/150674/): Add `DrawerActions` support for drawers with arbitrary Gravity
  * [150744](https://android-review.googlesource.com/#/c/150744/): `DrawerActions`: don't leak parentListener
  * [153303](https://android-review.googlesource.com/#/c/153303/): Specify gravity on the "is the drawer open/closed" checks
  * [157910](https://android-review.googlesource.com/#/c/157910/): Add `DrawerLayout` open/close action factories

#### Bug Fixes
* *espresso*
  * [79]( https://code.google.com/p/android-test-kit/issues/detail?id=79): `ViewActions#closeSoftKeyboard()` does ensure that soft keyboard is completely gone
  * Fixed synchronization issue with Espresso's `Espresso#pressBack()` method on API 21+
  * Fixed synchronization for keyboard closure animations on API 23

* *rules*
  * Fixed `ServiceTestRule` on API 23, `startService()` must always be called with an explicit `Intent`

* *runner*
  * Fixed broken gradle `JaCoCo` support
  * Fixed broken test sharding support
  * Fixed inconsistent state in test runner after `JUnit3` style test timeouts

#### Other notable changes
  * Javadoc fixes and error message improvements
  * Ignore `suite()`'s and don't ignore init errors when using method filters

### Espresso 2.2 / ATSL 0.3 (2015-05-28)
--------

#### espresso-web 2.2
  * New `WebView` support

#### espresso-core 2.2
  * Migrated to use `dagger v2`
  * Migrated to use `hamcrest v1.3`

#### espresso-contrib 2.2
  * `Accessibility Checks`
  *  `DrawerActions` Gravity support

#### runner 0.3
  * Upgrade from JUnit v4.10 to  `JUnit v4.12 `
  * Migrated to use  `Hamcrest v1.3 `

#### rules 0.3
  * `DisableOnAndroidDebug ` Rule

#### Bugfixes
  * Fixed  `DrawerActions ` leaking  `ParentListener `
  * Assumption failure is now treated as an ignore test rather than a failing test
  * Fixed  `MonitoringInstrumentation ` leaking activity instances through  `ExecutorService `
  * Fixed for orphan Activities being stuck in stopped stage
  * Update  `Until.scrollFinished(..) ` to return true if no scroll events were generated. Guard against potential NPE in  `UiObject2#setText(..) `.

### Espresso 2.1, Test Runner/Rules 0.2 and UIAutomator 2.1.0 (2015-04-21)

--------

#### Breaking Changes
  * Test runner artifact split into two and the name changed from `com.android.support.test:testing-support-lib:0.1` to `com.android.support.test:runner:0.2` + `com.android.support.test:rules:0.2`.

#### New Features
  * *espresso-intents*: a Mockito-like API that enables hermetic inter-activity testing by allowing test authors to verify and stub outgoing intents.
    * `IntentsTestRule` -  Extends `ActivityTestRule`,  initialized and releases Espresso-Intents in functional UI tests.

  * *espresso-core*:
    * `ViewActions`: Added ability to run global assertions prior to running actions. This is useful for other frameworks that build on top of espresso to validate state of the the view hierarchy while existing espresso test suite is executed.
    * `ViewMatchers.withContentDescription resId` overload ([https://code.google.com/p/android-test-kit/issues/detail?id=120 issue 120])

  * *rules*:
    * `ActivityTestRule` - This rule provides functional testing of a single activity.
    * `UiThreadRule` + `UiThreadTest` annotation - This rule allows the test method annotated with `UiThreadTest` to execute on the application's main thread (or UI thread).
    * `ServiceTestRule` - This rule provides functional testing of a service.

  * *runner*:
    * `ApplicationLifecycleCallback` - Callback for monitoring application lifecycle events.
    * All runner arguments can now be also specified in the in the `AndroidManifest` via a meta-data tag.

 * *UIAutomator*:
    * `UiDevice#dumpWindowHierarchy()` can now accept a `File` or an `OutputStream`.

#### Bug Fixes
* *espresso*
  * [110](https://code.google.com/p/android-test-kit/issues/detail?id=110): Cursor matcher should return false if the column wasn't found so Hamcrest can proceed to the next cursor.
  * [113](https://code.google.com/p/android-test-kit/issues/detail?id=113): `NullPointerException` with `PreferenceMatchers` `withTitle`.
  * [135](https://code.google.com/p/android-test-kit/issues/detail?id=135): Unregistering idling resource causes the Espresso to think we have busy idling resources.
  * [136](https://code.google.com/p/android-test-kit/issues/detail?id=136): Update Support Annotations version used by Espresso Contrib.

* *UIAutomator*
  * Run watchers to prevent `StaleObjectException`

* *runner*
  * [126](https://code.google.com/p/android-test-kit/issues/detail?id=126): AndroidJUnit4 should skip tests with failing assumptions

#### Other notable changes
  * Add better error message when we can't typeText with a non-latin string. (related to issue: [49](https://code.google.com/p/android-test-kit/issues/detail?id=49))


### Version 2.0 (Released on: 2014.12.19)
--------

#### Breaking Changes

  * Espresso has moved to a new namespace: *com.google.android.apps.common.testing.ui*.espresso -> *android.support.test*.espresso
  * Espresso artifacts have been renamed
    * *espresso*-1.1.jar -> *espresso-core*-release-2.0.jar
    * `IdlingResource` interface has been moved into a separate lib: *espresso-idling-resource*-release-2.0.jar
    * `CountingIdlingResource` now lives in *espresso-contrib*-release-2.0.jar (as it always should have)
  * Optional (a guava dependency) has been removed from the public API in order to support repackaging the guava dependency and avoid dex collision (a major source of pain). Affected methods:
    * `ViewAssertion.check`
    * `HumanReadables.getViewHierarchyErrorMessage`


#### New Features

  * Actions
    * `ViewActions`
      * `replaceText`
      * `openLink`
      * swipe up/down
    * espresso-contrib
      * `RecyclerViewActions`: handles interactions with RecyclerViews
      * `PickerActions`: handles interactions with Date and Time pickers

  * Matchers
    * `RootMatchers`
      * `isPlatformPopup`
    * `ViewMatchers`
      * `isJavascriptEnabled`
      * `withSpinnerText`
      * `withHint`
      * `isSelected`
      * `hasLinks`
    * `LayoutMatchers`: matchers for i18n-related layout testing
    * `CursorMatchers`: a collection of matchers for Cursors

  * Assertions
    * `PositionAssertions` (`isLeftOf`, `isAbove`, etc): collection of `ViewAssertions` for checking relative position of elements on the screen
    * `LayoutAssertions`: assertions for i18n-related layout testing^2^

  * Testapp: Many new sample activities/tests

  * Other
    * `Espresso.unregisterIdlingResources` and `Espresso.getIdlingResources`: provides additional flexibility for working with `IdlingResources`
    * `ViewInteraction.withFailureHandler`: allows overriding the failure handler from `onView`
    * `onData` support for `AdapterViews` backed by `CursorAdapters`


  * Bug fixes
    * `ViewMatchers.isDisplayed` matches views that take up the entire screen, but are less than 90% displayed
    * Performing swipe action call to `DrawerActions.openDrawer()` results in `IdlingResourceTimeoutException`
    * And [many more](https://code.google.com/p/android-test-kit/issues/list?can=1&q=status%3AFixed)


  * Other notable changes
    * Switched from building with Maven to Gradle
    * Moved espresso dependencies (Guava, Dagger, Hamcrest) out of the way to avoid DEX collisions
    * Changed to return success or failure when registering and unregistering idling resources
    * Lollipop support: Place message.recycle() behind an interface to account for version related changes
    * Switched target SDK 21 (mostly affects the testapp)


[1]: What about WebView support? While testing internally, we discovered some issues, which required major changes. We are putting the finishing touches on a new and improved API - it is coming shortly and it promises to be awesome.

[2]: The Google Accessibility team is planning additional work in this area, which will be integrated in future version of Espresso


### Version 1.1 (Released on: 2014.1.8)
--------

#### Espresso
  * New swipeLeft and swipeRight ViewActions. [change](https://code.google.com/p/android-test-kit/source/detail?r=c4e4da01ca8d0fab31129c87f525f6e9ba1ecc02)
  * Multi-window support - an advanced feature that enables picking the target window on which Espresso should run the operation. [change](https://code.google.com/p/android-test-kit/source/detail?r=1e5ee056231f7feb8e2a9704872a4520197f9ba2)
  * Improvements to TypeTextAction - allows typing text into a pre-focused view, which makes it easier to append text. [change](https://code.google.com/p/android-test-kit/source/detail?r=390ecfe1e41ab9b1a5bada2e6893d665c8d2d7d8)
  * Numerous bug fixes

#### Espresso Contrib Library
  * This new library contains features that supplement Espresso, but are not part of the core library.
  * New DrawerActions for operating on DrawerLayout - has a dependency on android support library, hence we are keeping it outside of the core Espresso lib. [change](https://code.google.com/p/android-test-kit/source/detail?r=cf47b3d1c9bcd7e0f1af1f748f2c4c52cfe7e5cd)

#### Sample Tests
  * ... have been relocated to live in the same package as the testapp. [change](https://code.google.com/p/android-test-kit/source/detail?r=353c1c8f67cfdf27e2b8dad1a83fdca1e07b0113)
  * maven POMs have been fixed to remove duplicate guava deps (mvn install should work now)
