package androidx.test.gradletests.espresso.contrib

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(val dataSet: List<String>, val context: Context) :
  RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

  override fun getItemCount() = dataSet.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    if (position == dataSet.size / 2) {
      holder.isInTheMiddle = true
      holder.textView.text = context.resources.getString(R.string.middle)
    } else {
      holder.isInTheMiddle = false
      holder.textView.text = dataSet[position]
    }
  }

  override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
    val view =
      LayoutInflater.from(viewGroup.context).inflate(R.layout.text_row_item, viewGroup, false)
    return ViewHolder(view)
  }

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var textView: TextView
      private set

    var isInTheMiddle = false

    init {
      textView = view.findViewById(R.id.textView) as TextView
    }
  }
}
