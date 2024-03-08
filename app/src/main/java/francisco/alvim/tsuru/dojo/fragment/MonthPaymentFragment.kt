package francisco.alvim.tsuru.dojo.fragment


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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aigestudio.wheelpicker.WheelPicker

import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.adapters.MonthPaymentsAdapter
import francisco.alvim.tsuru.dojo.data.Constants.FIRST_YEAR
import francisco.alvim.tsuru.dojo.data.Constants.MONTH_VALUES
import francisco.alvim.tsuru.dojo.data.Constants.YEAR_VALUES
import francisco.alvim.tsuru.dojo.data.Utils.createDateDialog
import francisco.alvim.tsuru.dojo.data.WheelType
import kotlinx.android.synthetic.main.date_picker_month.*
import kotlinx.android.synthetic.main.fragment_month_payment.*
import java.text.SimpleDateFormat
import java.util.*

class MonthPaymentFragment : Fragment() {

    val viewModel: TsuruDojoViewModel by activityViewModels()
    private val dayValues = mutableListOf<String>().apply { for (i in 1..31) add(i.toString()) }
    private val monthValues = mutableListOf<String>().apply { for (i in 1..12) add(i.toString()) }
    private val yearValues = mutableListOf<String>().apply { for (i in 2000..2050) add(i.toString()) }
    private val flParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply{ gravity = Gravity.CENTER }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_month_payment, container, false)
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
            Dialog(requireContext()).apply{
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(R.layout.date_picker_month)

                val monthWheelPicker = getDefaultWheelPicker(MONTH_VALUES)
                monthDatePickerMonth.addView(monthWheelPicker, flParams)
                monthDatePickerMonth.post {
                    monthWheelPicker.setSelectedItemPosition(Integer.parseInt(monthEditText.text.toString() ?: "1")-1)
                }

                val yearWheelPicker = getDefaultWheelPicker(YEAR_VALUES)
                monthDatePickerYear.addView(yearWheelPicker, flParams)
                monthDatePickerYear.post {
                    yearWheelPicker.setSelectedItemPosition(Integer.parseInt(yearEditText.text.toString()) - FIRST_YEAR)
                }

                btnMonthDatePickerAdd.setOnClickListener {
                    val month = monthWheelPicker.currentItemPosition + 1
                    val year = FIRST_YEAR + yearWheelPicker.currentItemPosition
                    viewModel.setMonthPaymentInLayout(month,year)
                    dismiss()
                }
                btnMonthDatePickerCancel.setOnClickListener { dismiss() }
                show()
            }
        }

        btnNewMonthyPaymentChooseDate.setOnClickListener {
            val dateDayET = etNewMontlyPaymentDateDay
            val dateMonthET = etNewMontlyPaymentDateMonth
            val dateYearET = etNewMontlyPaymentDateYear
            createDateDialog(dateDayET, dateMonthET, dateYearET, viewModel, WheelType.MONTH_PAYMENT_DATE_PICK, requireContext())
        }
    }

    private fun getDefaultWheelPicker(dataValues: List<String>) : WheelPicker{
        return WheelPicker(requireContext()).apply {
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
        spinnerNewMontlyPaymentStudent.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayListOf("test","test2"))
        val cal = Calendar.getInstance()
        etNewMontlyPaymentDateDay.setText(cal.get(Calendar.DAY_OF_MONTH).toString())
        etNewMontlyPaymentDateMonth.setText((cal.get(Calendar.MONTH)+1).toString())
        etNewMontlyPaymentDateYear.setText(cal.get(Calendar.YEAR).toString())
        etNewMontlyPaymentMonth.setText((cal.get(Calendar.MONTH)+1).toString())
        etNewMontlyPaymentYear.setText(cal.get(Calendar.YEAR).toString())
    }

    private fun setupObservers() {
        viewModel.paymentsDoneInMonth.observe(viewLifecycleOwner, Observer {
            monthPaymentList.adapter = MonthPaymentsAdapter(requireContext(),it, viewModel)
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
        viewModel.newPaymentMonth.observe(viewLifecycleOwner,Observer {
            etNewMontlyPaymentMonth.setText(it.toString())
        })
        viewModel.newPaymentYear.observe(viewLifecycleOwner,Observer {
            etNewMontlyPaymentYear.setText(it.toString())
        })
        viewModel.newPaymentDateDay.observe(viewLifecycleOwner,Observer {
            etNewMontlyPaymentDateDay.setText(it.toString())
        })
        viewModel.newPaymentDateMonth.observe(viewLifecycleOwner,Observer {
            etNewMontlyPaymentDateMonth.setText(it.toString())
        })
        viewModel.newPaymentDateYear.observe(viewLifecycleOwner,Observer {
            etNewMontlyPaymentDateYear.setText(it.toString())
        })
        viewModel.newPaymentStudentsNames.observe(viewLifecycleOwner,Observer {
            spinnerNewMontlyPaymentStudent.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it)
        })
        viewModel.currentMonthAndYear.observe(viewLifecycleOwner,Observer {
            monthPaymentMonthLabel.text = it
        })
        viewModel.totalMonthPayment.observe(viewLifecycleOwner,Observer {
            monthPaymentMonthTotal.text = it
        })
        viewModel.namePosition.observe(viewLifecycleOwner,Observer {
            spinnerNewMontlyPaymentStudent.setSelection(it)
        })
        viewModel.removeMonthPaymentClick.observe(viewLifecycleOwner,Observer {
            it.onFirstRun {
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
