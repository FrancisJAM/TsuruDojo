package francisco.alvim.tsuru.dojo.fragment


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.adapters.EventPaymentsAdapter
import francisco.alvim.tsuru.dojo.adapters.StudentsMissingInEventPaymentsAdapter
import francisco.alvim.tsuru.dojo.data.Constants.DAY_VALUES
import francisco.alvim.tsuru.dojo.data.Constants.FIRST_YEAR
import francisco.alvim.tsuru.dojo.data.Constants.MONTH_VALUES
import francisco.alvim.tsuru.dojo.data.Constants.YEAR_VALUES
import francisco.alvim.tsuru.dojo.data.Utils
import francisco.alvim.tsuru.dojo.data.Utils.createDateDialog
import francisco.alvim.tsuru.dojo.data.WheelType
import francisco.alvim.tsuru.dojo.entity.EventEntity
import francisco.alvim.tsuru.dojo.entity.EventPaymentEntity
import kotlinx.android.synthetic.main.card_picker_student.view.*
import kotlinx.android.synthetic.main.date_picker_full.*
import kotlinx.android.synthetic.main.fragment_event_payments.*
import kotlinx.android.synthetic.main.student_picker.*
import java.text.DecimalFormat
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
        viewModel.currentEventPayments.observe(viewLifecycleOwner, Observer { eventPayments ->
            eventPaymentList.apply {
                adapter = EventPaymentsAdapter(context!!, eventPayments, viewModel)
                setOnItemClickListener { _, _, pos, _ ->
                    showEventPaymentForStudent(eventPayments[pos])
                    viewModel.currentEventPayment = eventPayments[pos]
                    isNewEventPayment = false
                    updateNewEventPaymentButton()
                }
                setOnItemLongClickListener { _, _, pos, _ ->
                    viewModel.onRemoveEventPaymentClick(eventPayments[pos])
                    true
                }
            }
        })

        viewModel.removeEventPaymentClick.observe(viewLifecycleOwner, Observer {
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
        viewModel.newEventPaymentDateDay.observe(viewLifecycleOwner, Observer {
            etNewEventPaymentDateDay.setText(it.toString())
        })
        viewModel.newEventPaymentDateMonth.observe(viewLifecycleOwner, Observer {
            etNewEventPaymentDateMonth.setText(it.toString())
        })
        viewModel.newEventPaymentDateYear.observe(viewLifecycleOwner,Observer {
            etNewEventPaymentDateYear.setText(it.toString())
        })
        viewModel.totalEventPayment.observe(viewLifecycleOwner, Observer {
            eventPaymentsTotal.text = it
        })
        viewModel.studentsNotInEventPayment.observe(viewLifecycleOwner, Observer {
            if (it.isFirstRun) {
                Dialog(context!!).apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setCancelable(false)
                    setContentView(R.layout.student_picker)

                    studentPickerList.apply {
                        adapter = StudentsMissingInEventPaymentsAdapter(
                            context!!,
                            it.getContent(),
                            viewModel
                        )
                        setOnItemClickListener { _, view, position, _ ->
                            val selection = view.imgStudentPickerCardSelected
                            if (selection.isActivated) {
                                viewModel.removeSelectedStudentNameInPicker(position)
                            } else {
                                viewModel.addSelectedStudentNameInPicker(position)
                            }
                            selection.isActivated = !selection.isActivated
                        }
                    }

                    btnStudentPickerAdd.setOnClickListener {
                        viewModel.addAllSelectedStudentNames()
                        dismiss()
                    }
                    btnStudentPickerCancel.setOnClickListener { dismiss() }
                    show()
                }
            }
        })
        viewModel.selectedNamesInPicker.observe(viewLifecycleOwner, Observer {
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
            createDateDialog(etNewEventPaymentDateDay, etNewEventPaymentDateMonth, etNewEventPaymentDateYear, viewModel, WheelType.EVENT_PAYMENTS_DATE_PICK, context!!)
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
