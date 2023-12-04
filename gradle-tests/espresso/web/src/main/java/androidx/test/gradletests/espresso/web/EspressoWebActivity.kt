package androidx.test.gradletests.espresso.web

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView

class EspressoWebActivity : Activity() {
  private lateinit var webView: WebView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_espresso_web)
    webView = findViewById<WebView>(R.id.web_view)
    webView.loadData(
      "<html>" +
        "<body>" +
        "<script>" +
        "  function onSubmit() {" +
        "    value = document.getElementById('input').value;" +
        "    document.getElementById('info').innerHTML = 'Submitted: ' + value;" +
        "  }" +
        "</script>" +
        "<form action='javascript:onSubmit()'>" +
        "  Input: <input type='text' id='input' value='sample'>" +
        "  <input type='submit' id='submit' value='Submit'>" +
        "</form>" +
        "<p id='info'>Enter input and click the Submit button.</p>" +
        "</body>" +
        "</html>",
      "text/html",
      null
    )
    webView.getSettings().setJavaScriptEnabled(true)
  }
}
