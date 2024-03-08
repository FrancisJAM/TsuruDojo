package francisco.alvim.tsuru.dojo.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.adapters.BalanceMovementsAdapter
import francisco.alvim.tsuru.dojo.data.Constants
import francisco.alvim.tsuru.dojo.data.Constants.FIRST_YEAR
import francisco.alvim.tsuru.dojo.data.Utils
import francisco.alvim.tsuru.dojo.data.Utils.createDateDialog
import francisco.alvim.tsuru.dojo.data.WheelType
import kotlinx.android.synthetic.main.date_picker_full.*
import kotlinx.android.synthetic.main.fragment_balance.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class BalanceFragment : Fragment() {

    val viewModel: TsuruDojoViewModel by activityViewModels()
    private var isNewBalance = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_balance, container, false)
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
            createDateDialog(etBalanceDateDay, etBalanceDateMonth, etBalanceDateYear, viewModel, WheelType.BALANCE_DATE_PICK, requireContext())

        }
    }

    private fun closeAndClearNewMovementLayout(){
        closeNewBalanceMovementLayout()
        isNewBalance = true
        clearBalanceData()
        makeButtonNewBalance()
        updateNewBalanceButton()
    }

    private fun setupObservers() {
        viewModel.allBalanceMovements.observe(viewLifecycleOwner, Observer {balance ->
            balanceMovementList.adapter = BalanceMovementsAdapter(requireContext(),balance,viewModel)
            var total = 0.0
            balance.forEach {total += it.movementAmount!! }
            lblBalanceTotal.text = "Total Conta: ${total}€"
            balanceMovementList.setOnItemClickListener { _, _, position, _ ->
                openNewBalanceMovementLayout()
                btnNewBalanceMovementShow.setImageResource(R.drawable.ic_backup_gray)
                btnNewBalanceMovementShow.rotation = 0F
                val movement = balance[position]
                etBalanceName.setText(movement.movementName)
                etBalanceAmount.setText(abs(movement.movementAmount!!).toString())
                val date = movement.movementDate
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = date!!

                etBalanceDateDay.setText(calendar.get(Calendar.DAY_OF_MONTH).toString())
                etBalanceDateMonth.setText((Integer.parseInt(calendar.get(Calendar.MONTH).toString())+1).toString())
                etBalanceDateYear.setText(calendar.get(Calendar.YEAR).toString())

                setMovementPositive(movement.movementAmount!! >= 0.0)

                makeButtonChangeBalance()
                isNewBalance = false
                viewModel.setCurrentMovement(position)
            }

            balanceMovementList.setOnItemLongClickListener  { _, _, pos, _ ->
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(balance[pos].movementDate)
                AlertDialog.Builder(context).apply {
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