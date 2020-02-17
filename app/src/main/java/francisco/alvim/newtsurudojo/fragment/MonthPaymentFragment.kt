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
        }
        btnMonthPaymentMonthPrev.setOnClickListener {
            viewModel.changeMonth(-1)
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
            val dialog = Dialog(context!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.date_picker_month)
            val monthWheelFrameLayout = dialog.monthDatePickerMonth
            val yearWheelFrameLayout = dialog.monthDatePickerYear
            val flParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            flParams.gravity = Gravity.CENTER

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
            monthWheelFrameLayout.addView(monthWheelPicker, flParams)
            monthWheelFrameLayout.post{
                monthWheelPicker.setSelectedItemPosition(Integer.parseInt(etNewMontlyPaymentMonth.text.toString())-1)
            }

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
            yearWheelFrameLayout.addView(yearWheelPicker, flParams)
            yearWheelFrameLayout.post{
                yearWheelPicker.setSelectedItemPosition(Integer.parseInt(etNewMontlyPaymentYear.text.toString())-2000)
            }

            val acceptBtn = dialog.btnMonthDatePickerAdd
            val cancelBtn = dialog.btnMonthDatePickerCancel
            acceptBtn.setOnClickListener {
                val month = monthWheelPicker.currentItemPosition + 1
                val year = 2000 + yearWheelPicker.currentItemPosition
                viewModel.setMonthPaymentInLayout(month,year)
                dialog.dismiss()
            }
            cancelBtn.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        btnNewMonthyPaymentChooseDate.setOnClickListener {
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
                dayWheelPicker.setSelectedItemPosition((Integer.parseInt(etNewMontlyPaymentDateDay.text.toString()) ?: 0)-1)
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
                monthWheelPicker.setSelectedItemPosition((Integer.parseInt(etNewMontlyPaymentDateMonth.text.toString()) ?: 0)-1)
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
                yearWheelPicker.setSelectedItemPosition((Integer.parseInt(etNewMontlyPaymentDateYear.text.toString()) ?: 2000)-2000)
            }
            val acceptBtn = dialog.btnDatePickerAdd
            val cancelBtn = dialog.btnDatePickerCancel
            acceptBtn.setOnClickListener {
                val day = dayWheelPicker.currentItemPosition + 1
                val month = monthWheelPicker.currentItemPosition + 1
                val year = 2000 + yearWheelPicker.currentItemPosition
                viewModel.setDateOfPaymentInLayout(day,month,year)
                dialog.dismiss()
            }
            cancelBtn.setOnClickListener { dialog.dismiss() }
            dialog.show()
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
        if (newMontlyPaymentLayout.visibility == View.VISIBLE) {
            btnNewMonthlyPaymentShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
            btnNewMonthlyPaymentShow.rotation = 45F
        } else{
            btnNewMonthlyPaymentShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
            btnNewMonthlyPaymentShow.rotation = 0F
        }
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
            monthPaymentList.setOnItemClickListener { parent, v, pos, id ->
                showPaymentLayout()
                etNewMontlyPaymentAmount.setText((it[pos].first.defaultPayment)?.toString())
                etNewMontlyPaymentMonth.setText(viewModel.paymentsMonth.toString())
                etNewMontlyPaymentYear.setText(viewModel.paymentsYear.toString())
                viewModel.getClickedName(it[pos].first.id ?: 0)

            }
            monthPaymentList.setOnItemLongClickListener { parent, view, pos, id ->
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
