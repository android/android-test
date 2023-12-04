package androidx.test.gradletests.espresso.accessibility

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View

class EspressoAccessibilityActivity : Activity(), View.OnClickListener {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_espresso_accessibility)
  }

  override fun onClick(view: View) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setData(Uri.parse("http://developer.android.com"))
    startActivity(intent)
  }
}
