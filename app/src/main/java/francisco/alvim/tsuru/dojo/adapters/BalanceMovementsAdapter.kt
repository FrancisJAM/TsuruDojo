package francisco.alvim.tsuru.dojo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.entity.BalanceEntity
import kotlinx.android.synthetic.main.card_balance_movement.view.*
import java.text.SimpleDateFormat
import java.util.*

class BalanceMovementsAdapter(context: Context, list: List<BalanceEntity>, val viewModel: TsuruDojoViewModel): ArrayAdapter<BalanceEntity>(context,0,list)  {

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.card_balance_movement, parent, false)
        if (pos >= count) return view
        val currentItem = getItem(pos)
        view.movementCardName.text = currentItem?.movementName
        view.movementCardAmount.text = (currentItem?.movementAmount ?: 0).toString() + " €"
        view.movementCardDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(currentItem?.movementDate ?: 0) ?: ""

        return view
    }

}