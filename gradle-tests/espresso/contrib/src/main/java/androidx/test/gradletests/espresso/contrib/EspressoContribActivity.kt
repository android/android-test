package androidx.test.gradletests.espresso.contrib

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EspressoContribActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_espresso_contrib)

    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
    val linearLayoutManager = LinearLayoutManager(getApplicationContext())
    recyclerView.setLayoutManager(linearLayoutManager)

    val dataSet: List<String> =
      0.until(DATASET_COUNT).map { i -> getString(R.string.item_element_text) + i }

    val adapter = CustomAdapter(dataSet, getApplicationContext())
    recyclerView.setAdapter(adapter)
  }

  companion object {
    const val DATASET_COUNT = 50
  }
}
