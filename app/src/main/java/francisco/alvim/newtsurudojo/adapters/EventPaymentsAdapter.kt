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
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class EventPaymentsAdapter(context: Context, list: List<EventPaymentEntity>, val viewModel: TsuruDojoViewModel): ArrayAdapter<EventPaymentEntity>(context,0,list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.card_event_payment, parent, false)
        val currentItem = getItem(position)
        view.eventPaymentCardName.text = currentItem?.studentName ?: ""
        val paymentFormatter = DecimalFormat("0.## €")
        val payment = currentItem?.paymentAmount ?: 0.0
        view.eventPaymentCardAmountPayed.text = paymentFormatter.format(payment)
        val payed = currentItem?.payed ?: false
        view.eventPaymentCardPayed.setActivated(payed)
        val date = currentItem?.datePayment
        if (date != null && date != 0L && (payment != 0.0 || payed)) {
            view.eventPaymentCardDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentItem.datePayment)
        } else {
            view.eventPaymentCardDate.text = ""
        }

        return view
    }


}