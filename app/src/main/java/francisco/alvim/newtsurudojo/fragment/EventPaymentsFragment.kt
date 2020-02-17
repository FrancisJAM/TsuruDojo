package francisco.alvim.newtsurudojo.fragment


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aigestudio.wheelpicker.WheelPicker

import francisco.alvim.newtsurudojo.R
import francisco.alvim.newtsurudojo.TsuruDojoViewModel
import francisco.alvim.newtsurudojo.adapters.EventPaymentsAdapter
import francisco.alvim.newtsurudojo.adapters.StudentsMissingInEventPaymentsAdapter
import francisco.alvim.newtsurudojo.entity.EventEntity
import francisco.alvim.newtsurudojo.entity.EventPaymentEntity
import kotlinx.android.synthetic.main.card_picker_student.view.*
import kotlinx.android.synthetic.main.date_picker_full.*
import kotlinx.android.synthetic.main.fragment_event_payments.*
import kotlinx.android.synthetic.main.student_picker.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class EventPaymentsFragment : Fragment() {
    lateinit var viewModel: TsuruDojoViewModel
    private var isNewEventPayment = true
    private var currentEvent: EventEntity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_event_payments, container, false)
        viewModel = activity?.let { ViewModelProviders.of(it).get(TsuruDojoViewModel::class.java)} ?: ViewModelProviders.of(this as Fragment).get(TsuruDojoViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupUi()
        setupButtons()
    }

    private fun setupUi() {
        newEventPaymentLayout.visibility = View.GONE
        lblEventPaymentName.text = currentEvent?.eventName ?: ""
        clearPaymentData()
    }

    private fun setupObservers() {

        viewModel.currentEventPayments.observe(this, Observer {
            eventPaymentList.adapter = EventPaymentsAdapter(context!!,it, viewModel)
            eventPaymentList.setOnItemClickListener { parent, v, pos, id ->
                showEventPaymentForStudent(it[pos])
                viewModel.currentEventPayment = it[pos]
                isNewEventPayment = false
                updateNewEventPaymentButton()
            }
            eventPaymentList.setOnItemLongClickListener { parent, view, pos, id ->
                viewModel.onRemoveEventPaymentClick(it[pos])
                true
            }
        })

        viewModel.removeEventPaymentClick.observe(this, Observer {
            if (it.isFirstRun) {
                val eventPayment = it.getContent()
                val dialog = AlertDialog.Builder(context)
                dialog.apply {
                    setMessage("Apagar o estudante ${eventPayment.studentName} de pagamento?")
                    setCancelable(true)
                    setPositiveButton("Sim") { _, _ ->
                        viewModel.removeEventPayment(eventPayment)
                    }
                    setNegativeButton("Não") { _, _ -> }
                    show()
                }
            }
        })
        viewModel.newEventPaymentDateDay.observe(this, Observer {
            etNewEventPaymentDateDay.setText(it.toString())
        })
        viewModel.newEventPaymentDateMonth.observe(this, Observer {
            etNewEventPaymentDateMonth.setText(it.toString())
        })
        viewModel.newEventPaymentDateYear.observe(this,Observer {
            etNewEventPaymentDateYear.setText(it.toString())
        })
        viewModel.totalEventPayment.observe(this, Observer {
            eventPaymentsTotal.text = it
        })
        viewModel.studentsNotInEventPayment.observe(this, Observer {
            if (it.isFirstRun){
                val dialog = Dialog(context!!)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.student_picker)

                val adapter = StudentsMissingInEventPaymentsAdapter(context!!,it.getContent(),viewModel)
                val listView = dialog.studentPickerList
                listView.adapter = adapter
                listView.setOnItemClickListener { parent, view, position, id ->
                    val selection = view.imgStudentPickerCardSelected
                    if (selection.isActivated){
                        viewModel.removeSelectedStudentNameInPicker(position)
                    } else {
                        viewModel.addSelectedStudentNameInPicker(position)

                    }
                    selection.isActivated = !selection.isActivated
                }

                val acceptBtn = dialog.btnStudentPickerAdd
                val cancelBtn = dialog.btnStudentPickerCancel
                acceptBtn.setOnClickListener {
                    viewModel.addAllSelectedStudentNames()
                    dialog.dismiss()
                }
                cancelBtn.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }
        })
        viewModel.selectedNamesInPicker.observe(this, Observer {
            if (it.isFirstRun) {
                it.getContent().forEach { name ->
                    if (name.isNotBlank()) viewModel.addNewEventPaymentStudent(name)
                }
            }
        })
        viewModel.getEventPayments()
        currentEvent = viewModel.currentEvent
    }

    private fun setupButtons() {
        btnNewEventPaymentShow.setOnClickListener {
            if (isNewEventPayment) hideOrShowEventPaymentLayout()
            isNewEventPayment = true
            makeButtonNewEventPayment()
            clearPaymentData()
            updateNewEventPaymentButton()
            closeKeyboard(it)
        }
        btnEventPaymentAdd.setOnClickListener {
            val name = etNewEventPaymentName.text.toString()
            if (name.isNotBlank()) viewModel.addNewEventPaymentStudent(name)
            clearPaymentData()
            closeKeyboard(it)
        }
        btnEventPaymentPay.setOnClickListener {
            val name = etNewEventPaymentName.text.toString()
            val paymentAmount = etNewEventPaymentDefaultPayment.text.toString().toDoubleOrNull() ?: 0.0
            val payed = btnEventPaymentsPayed.isActivated
            val cal = Calendar.getInstance()
            val year = etNewEventPaymentDateYear.text.toString().toIntOrNull() ?: cal.get(Calendar.YEAR)
            val month = (etNewEventPaymentDateMonth.text.toString().toIntOrNull() ?: cal.get(Calendar.MONTH)) - 1
            val day = etNewEventPaymentDateDay.text.toString().toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)
            val date = GregorianCalendar(year,month,day)
            viewModel.updateEventPayment(name, paymentAmount, payed, date)
            isNewEventPayment = true
            hideOrShowEventPaymentLayout()
        }
        btnEventPaymentsPayed.setOnClickListener {
            btnEventPaymentsPayed.setActivated(!btnEventPaymentsPayed.isActivated)
        }
        btnNewEventPaymentChooseDate.setOnClickListener {
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
                dayWheelPicker.setSelectedItemPosition((Integer.parseInt(etNewEventPaymentDateDay.text.toString()) ?: 0)-1)
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
                monthWheelPicker.setSelectedItemPosition((Integer.parseInt(etNewEventPaymentDateMonth.text.toString()) ?: 0)-1)
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
                yearWheelPicker.setSelectedItemPosition((Integer.parseInt(etNewEventPaymentDateYear.text.toString()) ?: 2000)-2000)
            }
            val acceptBtn = dialog.btnDatePickerAdd
            val cancelBtn = dialog.btnDatePickerCancel
            acceptBtn.setOnClickListener {
                val day = dayWheelPicker.currentItemPosition + 1
                val month = monthWheelPicker.currentItemPosition + 1
                val year = 2000 + yearWheelPicker.currentItemPosition
                viewModel.setDateOfEventPaymentInLayout(day,month,year)
                dialog.dismiss()
            }
            cancelBtn.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
        btnEventPaymentsBack.setOnClickListener {
            viewModel.onBackClick()
        }
        btnEventPaymentGetStudent.setOnClickListener {
            viewModel.getStudentsForEventPaymentSelection()
        }
    }

    private fun showEventPaymentForStudent(eventPayment: EventPaymentEntity) {
        newEventPaymentLayout.visibility = View.VISIBLE

        makeButtonPayEventPayment()
        etNewEventPaymentName.setText(eventPayment.studentName)


        val payment = if (eventPayment.paymentAmount == 0.0 && !eventPayment.payed) (currentEvent?.defaultPayment ?: 0.0) else eventPayment.paymentAmount
        val paymentFormatter = DecimalFormat("0.##")
        etNewEventPaymentDefaultPayment.setText(paymentFormatter.format(payment))
        val calendar = Calendar.getInstance()
        if (eventPayment.datePayment != 0L) {
            calendar.timeInMillis = eventPayment.datePayment
        }
        etNewEventPaymentDateDay.setText(calendar.get(Calendar.DAY_OF_MONTH).toString())
        etNewEventPaymentDateMonth.setText((calendar.get(Calendar.MONTH) + 1).toString())
        etNewEventPaymentDateYear.setText(calendar.get(Calendar.YEAR).toString())

        //if the current event payment is 0€, it defaults to payed for more quickly add payed transactions
        val payed = eventPayment.payed || eventPayment.paymentAmount == 0.0
        btnEventPaymentsPayed.setActivated(payed)
    }

    private fun hideOrShowEventPaymentLayout() {
        if (newEventPaymentLayout.visibility == View.VISIBLE){
            clearPaymentData()
            newEventPaymentLayout.visibility = View.GONE
            updateNewEventPaymentButton()
        } else {
            newEventPaymentLayout.visibility = View.VISIBLE
            updateNewEventPaymentButton()
        }
    }

    private fun makeButtonNewEventPayment() {
        clNewEventPayment.visibility = View.VISIBLE
        clChangeEventPayment.visibility = View.GONE
    }

    private fun makeButtonPayEventPayment() {
        clNewEventPayment.visibility = View.GONE
        clChangeEventPayment.visibility = View.VISIBLE
    }

    private fun clearPaymentData() {
        etNewEventPaymentName.setText("")
        etNewEventPaymentDefaultPayment.setText((currentEvent?.defaultPayment ?: 0).toString())

        val calendar = Calendar.getInstance()
        etNewEventPaymentDateDay.setText(calendar.get(Calendar.DAY_OF_MONTH).toString())
        etNewEventPaymentDateMonth.setText((calendar.get(Calendar.MONTH) + 1).toString())
        etNewEventPaymentDateYear.setText(calendar.get(Calendar.YEAR).toString())

    }

    private fun updateNewEventPaymentButton(){
        if (!isNewEventPayment) {
            btnNewEventPaymentShow.setImageResource(R.drawable.ic_backup_gray)
            btnNewEventPaymentShow.rotation = 0F
        } else {
            if (newEventPaymentLayout.visibility == View.VISIBLE) {
                btnNewEventPaymentShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
                btnNewEventPaymentShow.rotation = 45F
            } else{
                btnNewEventPaymentShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
                btnNewEventPaymentShow.rotation = 0F
            }
        }
    }

    fun closeKeyboard(view : View) {
        view.clearFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        view.let{ imm?.hideSoftInputFromWindow(it.windowToken, 0)}
    }
}
