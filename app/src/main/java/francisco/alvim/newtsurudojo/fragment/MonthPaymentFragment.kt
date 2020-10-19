package francisco.alvim.newtsurudojo.fragment


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aigestudio.wheelpicker.WheelPicker

import francisco.alvim.newtsurudojo.R
import francisco.alvim.newtsurudojo.TsuruDojoViewModel
import francisco.alvim.newtsurudojo.adapters.MonthPaymentsAdapter
import kotlinx.android.synthetic.main.date_picker_full.*
import kotlinx.android.synthetic.main.date_picker_month.*
import kotlinx.android.synthetic.main.fragment_month_payment.*
import java.text.SimpleDateFormat
import java.util.*

class MonthPaymentFragment : Fragment() {

    lateinit var viewModel: TsuruDojoViewModel
    private val dayValues = mutableListOf<String>().apply { for (i in 1..31) add(i.toString()) }
    private val monthValues = mutableListOf<String>().apply { for (i in 1..12) add(i.toString()) }
    private val yearValues = mutableListOf<String>().apply { for (i in 2000..2050) add(i.toString()) }
    private val flParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply{ gravity = Gravity.CENTER }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_month_payment, container, false)
        viewModel = activity?.let {ViewModelProviders.of(it).get(TsuruDojoViewModel::class.java)} ?: ViewModelProviders.of(this as Fragment).get(TsuruDojoViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
        setupButtons()
    }

    private fun setupButtons() {
        btnNewMonthlyPaymentShow.setOnClickListener {
            hideOrShowPaymentLayout()
            closeKeyboard(it)
        }
        btnMonthPaymentMonthNext.setOnClickListener {
            viewModel.changeMonth(1)
            viewModel.updateMonthInTop()
        }
        btnMonthPaymentMonthPrev.setOnClickListener {
            viewModel.changeMonth(-1)
            viewModel.updateMonthInTop()
        }
        btnNewMonthlyPaymentAdd.setOnClickListener {
            viewModel.addNewPayment(
                spinnerNewMontlyPaymentStudent.selectedItemPosition,
                etNewMontlyPaymentAmount.text.toString(),
                Integer.parseInt(etNewMontlyPaymentMonth.text.toString()),
                Integer.parseInt(etNewMontlyPaymentYear.text.toString()),
                Integer.parseInt(etNewMontlyPaymentDateDay.text.toString()),
                Integer.parseInt(etNewMontlyPaymentDateMonth.text.toString()) - 1,
                Integer.parseInt(etNewMontlyPaymentDateYear.text.toString())
            )
            hidePaymentLayout()
            closeKeyboard(it)
        }

        btnNewMonthyPaymentChooseMonth.setOnClickListener {
            val monthEditText = etNewMontlyPaymentMonth
            val yearEditText = etNewMontlyPaymentYear
            Dialog(context!!).apply{
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(R.layout.date_picker_month)

                val monthWheelPicker = getDefaultWheelPicker(monthValues)
                monthDatePickerMonth.addView(monthWheelPicker, flParams)
                monthDatePickerMonth.post {
                    monthWheelPicker.setSelectedItemPosition(Integer.parseInt(monthEditText.text.toString() ?: "1")-1)
                }

                val yearWheelPicker = getDefaultWheelPicker(yearValues)
                monthDatePickerYear.addView(yearWheelPicker, flParams)
                monthDatePickerYear.post {
                    yearWheelPicker.setSelectedItemPosition(Integer.parseInt(yearEditText.text.toString()) - 2000)
                }

                btnMonthDatePickerAdd.setOnClickListener {
                    val month = monthWheelPicker.currentItemPosition + 1
                    val year = 2000 + yearWheelPicker.currentItemPosition
                    viewModel.setMonthPaymentInLayout(month,year)
                    dismiss()
                }
                btnMonthDatePickerCancel.setOnClickListener { dismiss() }
                show()
            }
        }

        btnNewMonthyPaymentChooseDate.setOnClickListener {
            val dateDay = etNewMontlyPaymentDateDay
            val dateMonth = etNewMontlyPaymentDateMonth
            val dateYear = etNewMontlyPaymentDateYear
            Dialog(context!!).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(R.layout.date_picker_full)

                val dayWheelPicker = getDefaultWheelPicker(dayValues)
                val monthWheelPicker = getDefaultWheelPicker(monthValues)
                val yearWheelPicker = getDefaultWheelPicker(yearValues)

                datePickerDay.addView(dayWheelPicker, flParams)
                datePickerDay.post{
                    dayWheelPicker.setSelectedItemPosition((Integer.parseInt(dateDay.text.toString()) ?: 0)-1)
                }

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
                datePickerMonth.addView(monthWheelPicker, flParams)
                datePickerMonth.post{
                    monthWheelPicker.setSelectedItemPosition((Integer.parseInt(dateMonth.text.toString()) ?: 0)-1)
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
                datePickerYear.addView(yearWheelPicker, flParams)
                datePickerYear.post{
                    yearWheelPicker.setSelectedItemPosition((Integer.parseInt(dateYear.text.toString()) ?: 2000)-2000)
                }

                btnDatePickerAdd.setOnClickListener {
                    val day = dayWheelPicker.currentItemPosition + 1
                    val month = monthWheelPicker.currentItemPosition + 1
                    val year = 2000 + yearWheelPicker.currentItemPosition
                    viewModel.setDateOfPaymentInLayout(day,month,year)
                    dismiss()
                }
                btnDatePickerCancel.setOnClickListener { dismiss() }
                show()
            }
        }
    }

    private fun getDefaultWheelPicker(dataValues: List<String>) : WheelPicker{
        return WheelPicker(context!!).apply {
            setAtmospheric(true)
            visibleItemCount = 5
            selectedItemTextColor = Color.parseColor("#000000")
            itemTextColor = Color.parseColor("#50BBBBBB")
            data = dataValues
        }
    }

    private fun hideOrShowPaymentLayout() {
        if (newMontlyPaymentLayout.visibility == View.GONE) showPaymentLayout() else hidePaymentLayout()
    }

    private fun hidePaymentLayout(){
        newMontlyPaymentLayout.visibility = View.GONE
        updateShowNewLayoutButton()
    }

    private fun showPaymentLayout(){
        newMontlyPaymentLayout.visibility = View.VISIBLE
        updateShowNewLayoutButton()
    }

    private fun updateShowNewLayoutButton(){
        btnNewMonthlyPaymentShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
        btnNewMonthlyPaymentShow.rotation = if (newMontlyPaymentLayout.visibility == View.VISIBLE) 45F else 0F
    }

    private fun setupUi() {
        spinnerNewMontlyPaymentStudent.adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, arrayListOf("test","test2"))
        val cal = Calendar.getInstance()
        etNewMontlyPaymentDateDay.setText(cal.get(Calendar.DAY_OF_MONTH).toString())
        etNewMontlyPaymentDateMonth.setText((cal.get(Calendar.MONTH)+1).toString())
        etNewMontlyPaymentDateYear.setText(cal.get(Calendar.YEAR).toString())
        etNewMontlyPaymentMonth.setText((cal.get(Calendar.MONTH)+1).toString())
        etNewMontlyPaymentYear.setText(cal.get(Calendar.YEAR).toString())
    }

    private fun setupObservers() {
        viewModel.paymentsDoneInMonth.observe(this, Observer {
            monthPaymentList.adapter = MonthPaymentsAdapter(context!!,it, viewModel)
            monthPaymentList.setOnItemClickListener { _, _, pos, _ ->
                showPaymentLayout()
                etNewMontlyPaymentAmount.setText((it[pos].first.defaultPayment)?.toString())
                etNewMontlyPaymentMonth.setText(viewModel.paymentsMonth.toString())
                etNewMontlyPaymentYear.setText(viewModel.paymentsYear.toString())
                viewModel.getClickedName(it[pos].first.id ?: 0)

            }
            monthPaymentList.setOnItemLongClickListener { _, _, pos, _ ->
                viewModel.onRemoveMonthPaymentClick(pos)
                true
            }
        })
        viewModel.newPaymentMonth.observe(this,Observer {
            etNewMontlyPaymentMonth.setText(it.toString())
        })
        viewModel.newPaymentYear.observe(this,Observer {
            etNewMontlyPaymentYear.setText(it.toString())
        })
        viewModel.newPaymentDateDay.observe(this,Observer {
            etNewMontlyPaymentDateDay.setText(it.toString())
        })
        viewModel.newPaymentDateMonth.observe(this,Observer {
            etNewMontlyPaymentDateMonth.setText(it.toString())
        })
        viewModel.newPaymentDateYear.observe(this,Observer {
            etNewMontlyPaymentDateYear.setText(it.toString())
        })
        viewModel.newPaymentStudentsNames.observe(this,Observer {
            spinnerNewMontlyPaymentStudent.adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, it)
        })
        viewModel.currentMonthAndYear.observe(this,Observer {
            monthPaymentMonthLabel.text = it
        })
        viewModel.totalMonthPayment.observe(this,Observer {
            monthPaymentMonthTotal.text = it
        })
        viewModel.namePosition.observe(this,Observer {
            spinnerNewMontlyPaymentStudent.setSelection(it)
        })
        viewModel.removeMonthPaymentClick.observe(this,Observer {
            if (it.isFirstRun) {
                val payment = it.getContent()
                val dialog = AlertDialog.Builder(context)
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(payment.paymentDate)
                dialog.apply {
                    setMessage("Apagar o pagamento de ${payment.paymentAmount} feito a $formattedDate?")
                    setCancelable(true)
                    setPositiveButton("Sim") { _, _ ->
                        viewModel.removePayment(payment)
                    }
                    setNegativeButton("Não") { _, _ -> }
                    show()
                }
            }
        })
        viewModel.changeMonth(0)
    }

    fun closeKeyboard(view : View) {
        view.clearFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        view.let{ imm?.hideSoftInputFromWindow(it.windowToken, 0)}
    }
}
