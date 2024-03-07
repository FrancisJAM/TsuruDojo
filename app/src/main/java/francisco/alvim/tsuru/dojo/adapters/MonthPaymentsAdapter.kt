package francisco.alvim.tsuru.dojo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.entity.MonthPaymentEntity
import francisco.alvim.tsuru.dojo.entity.StudentEntity
import kotlinx.android.synthetic.main.card_month_payment.view.*
import java.text.SimpleDateFormat
import java.util.*

class MonthPaymentsAdapter(context: Context, list: List<Pair<StudentEntity,MonthPaymentEntity?>>, val viewModel: TsuruDojoViewModel): ArrayAdapter<Pair<StudentEntity,MonthPaymentEntity?>>(context,0,list)  {

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.card_month_payment, parent, false)
        if (pos >= count) return view
        val currentItem = getItem(pos)
        view.monthPaymentCardName.text = currentItem?.first?.studentName
        view.monthPaymentCardAmountPayed.text = (currentItem?.second?.paymentAmount ?: 0).toString() + " €"
        view.monthPaymentCardDate.visibility = if (currentItem?.second == null) View.INVISIBLE else View.VISIBLE
        view.monthPaymentCardDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(currentItem?.second?.paymentDate ?: 0) ?: ""

        return view
    }

}