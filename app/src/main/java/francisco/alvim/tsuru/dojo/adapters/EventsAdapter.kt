package francisco.alvim.tsuru.dojo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.entity.EventEntity
import kotlinx.android.synthetic.main.card_event.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class EventsAdapter(context: Context, list: List<EventEntity>, val viewModel: TsuruDojoViewModel): ArrayAdapter<EventEntity>(context,0,list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.card_event, parent, false)
        val currentItem = getItem(position)
        view.eventCardName.text = currentItem?.eventName ?: ""
        view.eventCardDate.text = SimpleDateFormat("d/MM/yyyy", Locale.getDefault()).format(currentItem?.eventDate ?: "")

        return view
    }


}