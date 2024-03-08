package francisco.alvim.tsuru.dojo.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aigestudio.wheelpicker.WheelPicker

import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.adapters.EventsAdapter
import francisco.alvim.tsuru.dojo.data.Constants
import francisco.alvim.tsuru.dojo.data.Constants.FIRST_YEAR
import francisco.alvim.tsuru.dojo.data.Utils
import francisco.alvim.tsuru.dojo.data.Utils.createDateDialog
import francisco.alvim.tsuru.dojo.data.WheelType
import francisco.alvim.tsuru.dojo.entity.EventEntity
import kotlinx.android.synthetic.main.date_picker_full.*
import kotlinx.android.synthetic.main.fragment_balance.*
import kotlinx.android.synthetic.main.fragment_events.*
import java.util.*

class EventsFragment : Fragment() {
    val viewModel: TsuruDojoViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
        setupButtons()
    }

    private fun setupUi() {
        newEventLayout.visibility = View.GONE
        clearNewEventData()
    }

    private fun setupObservers() {

        viewModel.allEvents.observe(viewLifecycleOwner, Observer {
            eventList.adapter = EventsAdapter(requireContext(),it, viewModel)
            eventList.setOnItemClickListener { _, _, pos, _ ->
                viewModel.headToEventPage(it[pos])
            }
            eventList.setOnItemLongClickListener { _, _, pos, _ ->
                viewModel.onRemoveEventClick(it[pos])
                true
            }
        })

        viewModel.removeEventClick.observe(viewLifecycleOwner, Observer {
            it.onFirstRun {
                val event = it.getContent()
                val dialog = AlertDialog.Builder(context)
                dialog.apply {
                    setMessage("Apagar o evento ${event.eventName}?")
                    setCancelable(true)
                    setPositiveButton("Sim") { _, _ ->
                        viewModel.removeEvent(event)
                    }
                    setNegativeButton("Não") { _, _ -> }
                    show()
                }
            }
        })
        viewModel.newEventDateDay.observe(viewLifecycleOwner, Observer {
            etNewEventDateDay.setText(it.toString())
        })
        viewModel.newEventDateMonth.observe(viewLifecycleOwner, Observer {
            etNewEventDateMonth.setText(it.toString())
        })
        viewModel.newEventDateYear.observe(viewLifecycleOwner,Observer {
            etNewEventDateYear.setText(it.toString())
        })

        viewModel.getEvents()
    }

    private fun setupButtons() {
        btnNewEventShow.setOnClickListener {
            hideOrShowNewEvent()
            closeKeyboard(it)
        }
        btnEventAdd.setOnClickListener {
            val name =  etNewEventName.text.toString()
            if (name.isNotBlank()) {
                val dateYear = etNewEventDateYear.text.toString().toIntOrNull() ?: FIRST_YEAR
                val dateMonth = (etNewEventDateMonth.text.toString().toIntOrNull() ?: 1) - 1
                val dateDay = etNewEventDateDay.text.toString().toIntOrNull() ?: 1
                val event = EventEntity(
                    null,
                    name,
                    etNewEventDefaultPayment.text.toString().toDoubleOrNull() ?: 0.0,
                    eventDate = GregorianCalendar(dateYear, dateMonth, dateDay).timeInMillis
                )
                viewModel.addNewEvent(event)
                hideOrShowNewEvent()
            }
            closeKeyboard(it)
        }
        btnNewEventChooseDate.setOnClickListener {
            createDateDialog(etNewEventDateDay, etNewEventDateMonth, etNewEventDateYear, viewModel, WheelType.EVENTS_DATE_PICK, requireContext())
        }
    }

    private fun hideOrShowNewEvent() {
        if (newEventLayout.visibility == View.VISIBLE){
            clearNewEventData()
            newEventLayout.visibility = View.GONE
        } else {
            newEventLayout.visibility = View.VISIBLE
        }
        updateShowNewLayoutButton()
    }

    private fun clearNewEventData() {
        etNewEventName.setText("")
        etNewEventDefaultPayment.setText("")
        val calendar = Calendar.getInstance()
        etNewEventDateDay.setText(calendar.get(Calendar.DAY_OF_MONTH).toString())
        etNewEventDateMonth.setText((calendar.get(Calendar.MONTH) + 1).toString())
        etNewEventDateYear.setText(calendar.get(Calendar.YEAR).toString())
    }

    private fun updateShowNewLayoutButton(){
        if (newEventLayout.visibility == View.VISIBLE) {
            btnNewEventShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
            btnNewEventShow.rotation = 45F
        } else{
            btnNewEventShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
            btnNewEventShow.rotation = 0F
        }
    }

    fun closeKeyboard(view : View) {
        view.clearFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        view.let{ imm?.hideSoftInputFromWindow(it.windowToken, 0)}
    }

}
