package francisco.alvim.tsuru.dojo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import kotlinx.android.synthetic.main.card_picker_student.view.*

class StudentsMissingInEventPaymentsAdapter(context: Context, list: List<String>, val viewModel: TsuruDojoViewModel): ArrayAdapter<String>(context,0,list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.card_picker_student, parent, false)
        val currentItem = getItem(position)
        view.tvStudentPickerCardName.text = currentItem

        return view
    }

    override fun getViewTypeCount(): Int {
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}