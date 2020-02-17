package francisco.alvim.newtsurudojo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import francisco.alvim.newtsurudojo.R
import francisco.alvim.newtsurudojo.TsuruDojoViewModel
import francisco.alvim.newtsurudojo.entity.EventPaymentEntity
import kotlinx.android.synthetic.main.card_event_payment.view.*
import kotlinx.android.synthetic.main.card_picker_student.view.*
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class StudentsMissingInEventPaymentsAdapter(context: Context, list: List<String>, val viewModel: TsuruDojoViewModel): ArrayAdapter<String>(context,0,list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
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