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
import francisco.alvim.newtsurudojo.adapters.BalanceMovementsAdapter
import kotlinx.android.synthetic.main.date_picker_full.*
import kotlinx.android.synthetic.main.fragment_balance.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class BalanceFragment : Fragment() {

    lateinit var viewModel: TsuruDojoViewModel
    private var isNewBalance = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_balance, container, false)
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
        setMovementPositive(true)
        val cal = Calendar.getInstance()
        etBalanceDateDay.setText(cal.get(Calendar.DAY_OF_MONTH).toString())
        etBalanceDateMonth.setText((cal.get(Calendar.MONTH)+1).toString())
        etBalanceDateYear.setText(cal.get(Calendar.YEAR).toString())
    }

    private fun setupButtons() {
        btnBalancePositive.setOnClickListener {
            setMovementPositive(true)
        }

        btnBalanceNegative.setOnClickListener {
            setMovementPositive(false)
        }

        btnBalanceAdd.setOnClickListener {
            closeKeyboard(it)
            val isPositiveMovement = getCurrentBalanceType()
            viewModel.addBalanceMovement(
                etBalanceName.text.toString(),
                etBalanceAmount.text.toString(),
                Integer.parseInt(etBalanceDateDay.text.toString()),
                Integer.parseInt(etBalanceDateMonth.text.toString()),
                Integer.parseInt(etBalanceDateYear.text.toString()),
                isPositiveMovement
            )
            closeAndClearNewMovementLayout()
        }

        btnBalanceChange.setOnClickListener {
            closeKeyboard(it)
            val isPositiveMovement = getCurrentBalanceType()
            viewModel.updateBalanceMovement(
                etBalanceName.text.toString(),
                etBalanceAmount.text.toString(),
                Integer.parseInt(etBalanceDateDay.text.toString()),
                Integer.parseInt(etBalanceDateMonth.text.toString()),
                Integer.parseInt(etBalanceDateYear.text.toString()),
                isPositiveMovement
            )
            closeAndClearNewMovementLayout()
        }

        btnNewBalanceMovementShow.setOnClickListener {
            if (isNewBalance) showOrHideNewBalanceMovementLayout()
            isNewBalance = true
            makeButtonNewBalance()
            clearBalanceData()
            updateNewBalanceButton()
            closeKeyboard(it)
        }


        btnBalanceChooseDate.setOnClickListener {
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
                dayWheelPicker.setSelectedItemPosition((Integer.parseInt(etBalanceDateDay.text.toString()) ?: 0)-1)
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
                monthWheelPicker.setSelectedItemPosition((Integer.parseInt(etBalanceDateMonth.text.toString()) ?: 0)-1)
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
                yearWheelPicker.setSelectedItemPosition((Integer.parseInt(etBalanceDateYear.text.toString()) ?: 2000)-2000)
            }
            val acceptBtn = dialog.btnDatePickerAdd
            val cancelBtn = dialog.btnDatePickerCancel
            acceptBtn.setOnClickListener {
                val day = dayWheelPicker.currentItemPosition + 1
                val month = monthWheelPicker.currentItemPosition + 1
                val year = 2000 + yearWheelPicker.currentItemPosition
                viewModel.setDateOfMovementInLayout(day,month,year)
                dialog.dismiss()
            }
            cancelBtn.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    fun closeAndClearNewMovementLayout(){
        closeNewBalanceMovementLayout()
        isNewBalance = true
        clearBalanceData()
        makeButtonNewBalance()
        updateNewBalanceButton()
    }

    private fun setupObservers() {
        viewModel.allBalanceMovements.observe(viewLifecycleOwner, Observer {balance ->
            balanceMovementList.adapter = BalanceMovementsAdapter(context!!,balance,viewModel)
            var total = 0.0
            balance.forEach {total += it.movementAmount }
            lblBalanceTotal.text = "Total Conta: ${total}€"
            balanceMovementList.setOnItemClickListener { _, _, position, _ ->
                openNewBalanceMovementLayout()
                btnNewBalanceMovementShow.setImageResource(R.drawable.ic_backup_gray)
                btnNewBalanceMovementShow.rotation = 0F
                val movement = balance[position]
                etBalanceName.setText(movement.movementName)
                etBalanceAmount.setText(abs(movement.movementAmount).toString())
                val date = movement.movementDate
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = date

                etBalanceDateDay.setText(calendar.get(Calendar.DAY_OF_MONTH).toString())
                etBalanceDateMonth.setText((Integer.parseInt(calendar.get(Calendar.MONTH).toString())+1).toString())
                etBalanceDateYear.setText(calendar.get(Calendar.YEAR).toString())

                setMovementPositive(movement.movementAmount >= 0.0)

                makeButtonChangeBalance()
                isNewBalance = false
                viewModel.setCurrentMovement(position)
            }

            balanceMovementList.setOnItemLongClickListener  { _, _, pos, _ ->
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(balance[pos].movementDate)
                val dialog = AlertDialog.Builder(context)
                dialog.apply {
                    setMessage("Apagar o movimento de ${balance[pos].movementAmount} feito a $formattedDate?")
                    setCancelable(true)
                    setPositiveButton("Sim") { _, _ ->
                        viewModel.removeBalanceMovement(balance[pos])
                    }
                    setNegativeButton("Não") { _, _ -> }
                    show()
                }
                true
            }
        })
        viewModel.newMovementDateDay.observe(viewLifecycleOwner, Observer {
            etBalanceDateDay.setText(it.toString())
        })
        viewModel.newMovementDateMonth.observe(viewLifecycleOwner, Observer {
            etBalanceDateMonth.setText(it.toString())
        })
        viewModel.newMovementDateYear.observe(viewLifecycleOwner, Observer {
            etBalanceDateYear.setText(it.toString())
        })
        viewModel.getBalanceMovements()
    }

    private fun clearBalanceData() {
        etBalanceName.setText("")
        etBalanceAmount.setText("")
        setMovementPositive(true)
    }

    fun closeKeyboard(view : View) {
        view.clearFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        view.let{ imm?.hideSoftInputFromWindow(it.windowToken, 0)}
    }

    private fun makeButtonNewBalance() {
        btnBalanceAdd.visibility = View.VISIBLE
        btnBalanceChange.visibility = View.GONE
        btnBalanceRecurrent.visibility = View.VISIBLE
    }

    private fun makeButtonChangeBalance() {
        btnBalanceAdd.visibility = View.GONE
        btnBalanceChange.visibility = View.VISIBLE
        btnBalanceRecurrent.visibility = View.GONE
    }

    private fun showOrHideNewBalanceMovementLayout() {
        balanceLayout.visibility = if (balanceLayout.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun closeNewBalanceMovementLayout() {
        balanceLayout.visibility = View.GONE
    }

    private fun openNewBalanceMovementLayout() {
        balanceLayout.visibility = View.VISIBLE
    }

    private fun updateNewBalanceButton() {
        if (!isNewBalance) {
            openNewBalanceMovementLayout()
            btnNewBalanceMovementShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
            btnNewBalanceMovementShow.rotation = 45F
        } else {
            if (balanceLayout.visibility == View.VISIBLE) {
                btnNewBalanceMovementShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
                btnNewBalanceMovementShow.rotation = 45F
            } else{
                btnNewBalanceMovementShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
                btnNewBalanceMovementShow.rotation = 0F
            }
        }
    }

    private fun setMovementPositive(isPositive: Boolean) {
        btnBalancePositive.isActivated = isPositive
        btnBalanceNegative.isActivated = !isPositive
    }

    private fun getCurrentBalanceType(): Boolean {
        return btnBalancePositive.isActivated && !btnBalanceNegative.isActivated
    }
}