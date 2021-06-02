package francisco.alvim.newtsurudojo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import francisco.alvim.newtsurudojo.R
import francisco.alvim.newtsurudojo.TsuruDojoViewModel
import francisco.alvim.newtsurudojo.entity.StudentNotesEntity
import kotlinx.android.synthetic.main.card_student_note.view.*

class StudentNotesAdapter(context: Context, list: List<StudentNotesEntity>, val viewModel: TsuruDojoViewModel): ArrayAdapter<StudentNotesEntity>(context,0,list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.card_student_note, parent, false)
        val currentItem = getItem(position)
        view.tvStudentNote.text = currentItem?.studentNote ?: ""

        return view
    }


}