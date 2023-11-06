package androidx.test.gradletests.espresso.web

import android.webkit.WebView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.web.assertion.WebViewAssertions.webContent
import androidx.test.espresso.web.matcher.DomMatchers.elementById
import androidx.test.espresso.web.matcher.DomMatchers.withTextContent
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EspressoWebTest {

  @Before
  fun setup() {
    ActivityScenario.launch(EspressoWebActivity::class.java)
  }

  @Test
  fun testWebContent() {
    onWebView(isAssignableFrom(WebView::class.java))
      .check(
        webContent(elementById("info", withTextContent("Enter input and click the Submit button.")))
      )
  }
}
