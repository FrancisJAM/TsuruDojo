package francisco.alvim.tsuru.dojo.data

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.FrameLayout
import com.aigestudio.wheelpicker.WheelPicker
import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.data.Constants.FIRST_YEAR
import kotlinx.android.synthetic.main.date_picker_full.*
import java.util.*

object Utils {
    @JvmStatic
    fun getMaxMonthDay(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    @JvmStatic
    fun getDayArrayFromMaxDay(maxDay: Int): MutableList<String>{
        val dayData = mutableListOf<String>()
        for (i in 1..maxDay) {
            dayData.add(i.toString())
        }
        return dayData
    }

    @JvmStatic
    fun getLevelNumValues(isDan: Boolean): ArrayList<String>{
        val levelNumValues = arrayListOf<String>()
        if (isDan) {
            for (i in 1..15) {
                levelNumValues.add(i.toString())
            }
        } else {
            for (i in 10 downTo 1) {
                levelNumValues.add(i.toString())
            }
        }
        return levelNumValues
    }

    @JvmStatic
    fun createWheelPicker(wheelValues: ArrayList<String>, context: Context) : WheelPicker {
        return WheelPicker(context).apply {
            setAtmospheric(true)
            visibleItemCount = 5
            selectedItemTextColor = Color.parseColor("#000000")
            itemTextColor = Color.parseColor("#50BBBBBB")
            data = wheelValues
        }
    }

    @JvmStatic
    fun setNewMaxDayNum(year: Int, month: Int, wheelPicker: WheelPicker) {
        val maxDay = getMaxMonthDay(year,month)
        val dayData = getDayArrayFromMaxDay(maxDay)
        var currentDaySelected = wheelPicker.currentItemPosition
        if (currentDaySelected > maxDay) currentDaySelected = maxDay - 1
        wheelPicker.setSelectedItemPosition(currentDaySelected)
        wheelPicker.data = dayData
    }

    @JvmStatic
    fun createDateDialog(etDateDay: EditText, etDateMonth: EditText, etDateYear: EditText, viewModel: TsuruDojoViewModel, wheelType: WheelType, context: Context){
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.date_picker_full)
        val flParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        flParams.gravity = Gravity.CENTER

        val dayWheelPicker = createWheelPicker(Constants.DAY_VALUES, context)
        val monthWheelPicker = createWheelPicker(Constants.MONTH_VALUES, context)
        val yearWheelPicker = createWheelPicker(Constants.YEAR_VALUES, context)

        val dayWheelFrameLayout = dialog.datePickerDay
        val monthWheelFrameLayout = dialog.datePickerMonth
        val yearWheelFrameLayout = dialog.datePickerYear

        dayWheelFrameLayout.addView(dayWheelPicker, flParams)
        dayWheelFrameLayout.post{
            dayWheelPicker.setSelectedItemPosition((etDateDay.text.toString().toIntOrNull() ?: 1)-1)
        }

        monthWheelPicker.setOnItemSelectedListener { _,_, month ->
            val year = FIRST_YEAR + yearWheelPicker.currentItemPosition
            setNewMaxDayNum(year, month, dayWheelPicker)
        }
        monthWheelFrameLayout.addView(monthWheelPicker, flParams)
        monthWheelFrameLayout.post{
            monthWheelPicker.setSelectedItemPosition((etDateMonth.text.toString().toIntOrNull() ?: 1)-1)
        }

        yearWheelPicker.setOnItemSelectedListener { _,_, pos ->
            val year = FIRST_YEAR + pos
            val month = monthWheelPicker.currentItemPosition
            setNewMaxDayNum(year, month, dayWheelPicker)
        }
        yearWheelFrameLayout.addView(yearWheelPicker, flParams)
        yearWheelFrameLayout.post{
            yearWheelPicker.setSelectedItemPosition((etDateYear.text.toString().toIntOrNull() ?: FIRST_YEAR)- FIRST_YEAR)
        }

        val acceptBtn = dialog.btnDatePickerAdd
        val cancelBtn = dialog.btnDatePickerCancel
        acceptBtn.setOnClickListener {
            val day = dayWheelPicker.currentItemPosition + 1
            val month = monthWheelPicker.currentItemPosition + 1
            val year = FIRST_YEAR + yearWheelPicker.currentItemPosition
            viewModel.onDateWheelAccept(day, month, year, wheelType)
            dialog.dismiss()
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}