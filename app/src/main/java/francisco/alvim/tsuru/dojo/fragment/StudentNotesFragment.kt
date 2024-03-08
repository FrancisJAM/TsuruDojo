package francisco.alvim.tsuru.dojo.fragment


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aigestudio.wheelpicker.WheelPicker

import francisco.alvim.tsuru.dojo.R
import francisco.alvim.tsuru.dojo.TsuruDojoViewModel
import francisco.alvim.tsuru.dojo.adapters.StudentNotesAdapter
import francisco.alvim.tsuru.dojo.adapters.StudentsAdapter
import francisco.alvim.tsuru.dojo.data.Constants.DAN
import francisco.alvim.tsuru.dojo.data.Constants.LEVEL_TYPES
import francisco.alvim.tsuru.dojo.data.Utils
import francisco.alvim.tsuru.dojo.entity.StudentEntity
import kotlinx.android.synthetic.main.fragment_student.*
import kotlinx.android.synthetic.main.fragment_student_notes.*
import kotlinx.android.synthetic.main.level_picker.*
class StudentNotesFragment : Fragment() {

    val viewModel: TsuruDojoViewModel by activityViewModels()
    private var newStudentNote = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_student_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
        setupButtons()
    }

    private fun setupUi() {
        newStudentNotesLayout.visibility = View.GONE
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!newStudentNote) {
                    makeButtonChangeStudentNote()
                }
            }
        }
        etNewStudentNote.addTextChangedListener(textWatcher)
    }

    private fun setupObservers() {

        viewModel.studentNameOfStudentNotes.observe(viewLifecycleOwner, Observer {
            lblStudentNotesStudentName.text = it
        })
        viewModel.allNotesOfStudent.observe(viewLifecycleOwner, Observer {
            studentNotesList.adapter = StudentNotesAdapter(requireContext(),it, viewModel)
            studentNotesList.setOnItemClickListener { _,_, pos,_ ->
                showNewStudentNotesLayout()
                btnNewStudentNoteShow.setImageResource(R.drawable.ic_backup_gray)
                btnNewStudentNoteShow.rotation = 0F
                etNewStudentNote.setText((it[pos].studentNote))
                hideAllNewStudentNoteButtons()
                newStudentNote = false
                viewModel.setCurrentStudentNote(pos)
            }
            studentNotesList.setOnItemLongClickListener { parent, view, pos, id ->
                viewModel.onRemoveStudentNotesClick(it[pos])
                true
            }
        })
        viewModel.removeStudentNoteClick.observe(viewLifecycleOwner, Observer {
            it.onFirstRun {
                val studentNote = it.getContent()
                val dialog = AlertDialog.Builder(context)
                dialog.apply {
                    setMessage("Apagar o comentário?")
                    setCancelable(true)
                    setPositiveButton("Sim") { _, _ ->
                        viewModel.removeStudentNote(studentNote)
                        clearNewStudentNoteData()
                        hideNewStudentNotesLayout()
                        newStudentNote = true
                        updateNewStudentNotesButton()
                    }
                    setNegativeButton("Não") { _, _ -> }
                    show()
                }
            }
        })
        viewModel.getCurrentStudentNotes()
    }

    private fun setupButtons() {
        btnStudentNotesBack.setOnClickListener {
            viewModel.onBackClick()
        }

        btnNewStudentNoteShow.setOnClickListener {
            if (newStudentNote) hideOrShowStudentNoteLayout()
            newStudentNote = true
            makeButtonNewStudentNote()
            clearNewStudentNoteData()
            updateNewStudentNotesButton()
            closeKeyboard(it)
        }
        btnStudentNotesChange.setOnClickListener {
            val note = etNewStudentNote.text.toString()
            viewModel.updateStudentNote(note)
            clearNewStudentNoteData()
            hideNewStudentNotesLayout()
            newStudentNote = true
            updateNewStudentNotesButton()
            closeKeyboard(it)
        }
        btnStudentNotesAdd.setOnClickListener {
            val note = etNewStudentNote.text.toString()
            if (note == "") return@setOnClickListener
            viewModel.addNewStudentNote(note)
            clearNewStudentNoteData()
            hideNewStudentNotesLayout()
            newStudentNote = true
            updateNewStudentNotesButton()
            closeKeyboard(it)
        }
    }

    private fun updateNewStudentNotesButton() {
        if (!newStudentNote) {
            showNewStudentNotesLayout()
            btnNewStudentNoteShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
            btnNewStudentNoteShow.rotation = 45F
        } else {
            if (newStudentNotesLayout.visibility == View.VISIBLE) {
                btnNewStudentNoteShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
                btnNewStudentNoteShow.rotation = 45F
            } else{
                btnNewStudentNoteShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
                btnNewStudentNoteShow.rotation = 0F
            }
        }
    }

    private fun clearNewStudentNoteData() {
        etNewStudentNote.setText("")
    }

    private fun makeButtonNewStudentNote() {
        btnStudentNotesAdd.visibility = View.VISIBLE
        btnStudentNotesChange.visibility = View.GONE
    }

    private fun makeButtonChangeStudentNote() {
        btnStudentNotesAdd.visibility = View.INVISIBLE
        btnStudentNotesChange.visibility = View.VISIBLE
    }

    private fun hideAllNewStudentNoteButtons() {
        btnStudentNotesAdd.visibility = View.GONE
        btnStudentNotesChange.visibility = View.GONE
    }

    private fun hideOrShowStudentNoteLayout() {
        newStudentNotesLayout.visibility = if (newStudentNotesLayout.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun showNewStudentNotesLayout() {
        newStudentNotesLayout.visibility = View.VISIBLE
    }

    private fun hideNewStudentNotesLayout() {
        newStudentNotesLayout.visibility = View.GONE
    }

    fun closeKeyboard(view : View) {
        view.clearFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        view.let{ imm?.hideSoftInputFromWindow(it.windowToken, 0)}
    }

}


