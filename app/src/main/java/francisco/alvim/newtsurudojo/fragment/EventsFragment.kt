package francisco.alvim.newtsurudojo.fragment


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aigestudio.wheelpicker.WheelPicker

import francisco.alvim.newtsurudojo.R
import francisco.alvim.newtsurudojo.TsuruDojoViewModel
import francisco.alvim.newtsurudojo.adapters.EventsAdapter
import francisco.alvim.newtsurudojo.entity.EventEntity
import kotlinx.android.synthetic.main.date_picker_full.*
import kotlinx.android.synthetic.main.fragment_events.*
import java.util.*

class EventsFragment : Fragment() {
    lateinit var viewModel: TsuruDojoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        viewModel = activity?.let { ViewModelProviders.of(it).get(TsuruDojoViewModel::class.java)} ?: ViewModelProviders.of(this as Fragment).get(TsuruDojoViewModel::class.java)
        return view
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

        viewModel.allEvents.observe(this, Observer {
            eventList.adapter = EventsAdapter(context!!,it, viewModel)
            eventList.setOnItemClickListener { parent, v, pos, id ->
                viewModel.headToEventPage(it[pos])
            }
            eventList.setOnItemLongClickListener { parent, view, pos, id ->
                viewModel.onRemoveEventClick(it[pos])
                true
            }
        })

        viewModel.removeEventClick.observe(this, Observer {
            if (it.isFirstRun) {
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
        viewModel.newEventDateDay.observe(this, Observer {
            etNewEventDateDay.setText(it.toString())
        })
        viewModel.newEventDateMonth.observe(this, Observer {
            etNewEventDateMonth.setText(it.toString())
        })
        viewModel.newEventDateYear.observe(this,Observer {
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
                val dateYear = etNewEventDateYear.text.toString().toIntOrNull() ?: 2000
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
            val dialog = Dialog(context!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.date_picker_full)
            val dayWheelFrameLayout = dialog.datePickerDay
            val monthWheelFrameLayout = dialog.datePickerMonth
            val yearWheelFrameLayout = dialog.datePickerYear
            val flParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            flParams.gravity = Gravity.CENTER

            val dayWheelPicker = WheelPicker(context!!)
            dayWheelPicker.setAtmospheric(true)
            dayWheelPicker.visibleItemCount = 5
            dayWheelPicker.selectedItemTextColor = Color.parseColor("#000000")
            dayWheelPicker.itemTextColor = Color.parseColor("#50BBBBBB")
            val dayValues = mutableListOf<String>()
            for (i in 1..31){
                dayValues.add(i.toString())
            }
            dayWheelPicker.data = dayValues
            dayWheelFrameLayout.addView(dayWheelPicker, flParams)
            dayWheelFrameLayout.post{
                dayWheelPicker.setSelectedItemPosition((Integer.parseInt(etNewEventDateDay.text.toString()) ?: 0)-1)
            }

            val monthWheelPicker = WheelPicker(context!!)
            monthWheelPicker.setAtmospheric(true)
            monthWheelPicker.visibleItemCount = 5
            monthWheelPicker.selectedItemTextColor = Color.parseColor("#000000")
            monthWheelPicker.itemTextColor = Color.parseColor("#50BBBBBB")
            val monthValues = mutableListOf<String>()
            for (i in 1..12){
                monthValues.add(i.toString())
            }
            monthWheelPicker.data = monthValues


            val yearWheelPicker = WheelPicker(context!!)
            yearWheelPicker.setAtmospheric(true)
            yearWheelPicker.visibleItemCount = 5
            yearWheelPicker.selectedItemTextColor = Color.parseColor("#000000")
            yearWheelPicker.itemTextColor = Color.parseColor("#50BBBBBB")
            val yearValues = mutableListOf<String>()
            for (i in 2000..2050){
                yearValues.add(i.toString())
            }
            yearWheelPicker.data = yearValues

            monthWheelPicker.setOnItemSelectedListener(object: WheelPicker.OnItemSelectedListener{
                override fun onItemSelected(picker: WheelPicker?, data: Any?, pos: Int) {
                    val cal = Calendar.getInstance()
                    cal.set(2000 + yearWheelPicker.currentItemPosition,pos,1)
                    val dayData = mutableListOf<String>()
                    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    for (i in 1..maxDay){
                        dayData.add(i.toString())
                    }
                    var currentDaySelected = dayWheelPicker.currentItemPosition
                    if (currentDaySelected > maxDay) currentDaySelected = maxDay - 1
                    dayWheelPicker.setSelectedItemPosition(currentDaySelected)
                    dayWheelPicker.data = dayData
                }
            })
            monthWheelFrameLayout.addView(monthWheelPicker, flParams)
            monthWheelFrameLayout.post{
                monthWheelPicker.setSelectedItemPosition((Integer.parseInt(etNewEventDateMonth.text.toString()) ?: 0)-1)
            }
            yearWheelPicker.setOnItemSelectedListener(object: WheelPicker.OnItemSelectedListener{
                override fun onItemSelected(picker: WheelPicker?, data: Any?, pos: Int) {
                    val cal = Calendar.getInstance()
                    cal.set(2000 + pos,monthWheelPicker.currentItemPosition,1)
                    val dayData = mutableListOf<String>()
                    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    for (i in 1..maxDay){
                        dayData.add(i.toString())
                    }
                    var currentDaySelected = dayWheelPicker.currentItemPosition
                    if (currentDaySelected > maxDay) currentDaySelected = maxDay - 1
                    dayWheelPicker.setSelectedItemPosition(currentDaySelected)
                    dayWheelPicker.data = dayData
                }
            })
            yearWheelFrameLayout.addView(yearWheelPicker, flParams)
            yearWheelFrameLayout.post{
                yearWheelPicker.setSelectedItemPosition((Integer.parseInt(etNewEventDateYear.text.toString()) ?: 2000)-2000)
            }
            val acceptBtn = dialog.btnDatePickerAdd
            val cancelBtn = dialog.btnDatePickerCancel
            acceptBtn.setOnClickListener {
                val day = dayWheelPicker.currentItemPosition + 1
                val month = monthWheelPicker.currentItemPosition + 1
                val year = 2000 + yearWheelPicker.currentItemPosition
                viewModel.setDateOfEventInLayout(day,month,year)
                dialog.dismiss()
            }
            cancelBtn.setOnClickListener { dialog.dismiss() }
            dialog.show()
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
