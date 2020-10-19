package francisco.alvim.newtsurudojo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import francisco.alvim.newtsurudojo.R
import francisco.alvim.newtsurudojo.TsuruDojoViewModel
import francisco.alvim.newtsurudojo.entity.StudentEntity
import kotlinx.android.synthetic.main.card_student.view.*

class StudentsAdapter(context: Context, list: List<StudentEntity>, val viewModel: TsuruDojoViewModel): ArrayAdapter<StudentEntity>(context,0,list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.card_student, parent, false)
        val currentItem = getItem(position)
        view.tvStudentCardName.text = currentItem?.studentName ?: ""
        view.imgStudentCardTraining.setActivated(currentItem?.studentTrains ?: false)

        return view
    }


}