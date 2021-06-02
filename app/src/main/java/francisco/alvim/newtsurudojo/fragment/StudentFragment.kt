package francisco.alvim.newtsurudojo.fragment


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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.aigestudio.wheelpicker.WheelPicker

import francisco.alvim.newtsurudojo.R
import francisco.alvim.newtsurudojo.TsuruDojoViewModel
import francisco.alvim.newtsurudojo.adapters.StudentsAdapter
import francisco.alvim.newtsurudojo.data.Constants.DAN
import francisco.alvim.newtsurudojo.data.Constants.LEVEL_TYPES
import francisco.alvim.newtsurudojo.data.Utils
import francisco.alvim.newtsurudojo.entity.StudentEntity
import kotlinx.android.synthetic.main.fragment_student.*
import kotlinx.android.synthetic.main.level_picker.*

class StudentFragment : Fragment() {

    lateinit var viewModel: TsuruDojoViewModel
    private var newStudent = true
    lateinit var levelNumWheelPicker: WheelPicker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_student, container, false)
        viewModel = activity?.let { ViewModelProviders.of(it).get(TsuruDojoViewModel::class.java)} ?: ViewModelProviders.of(this as Fragment).get(TsuruDojoViewModel::class.java)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
        setupButtons()
    }

    private fun setupUi() {
        newStudentLayout.visibility = View.GONE
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!newStudent) {
                    makeButtonChangeStudent()
                }
            }
        }
        etNewStudentName.addTextChangedListener(textWatcher)
        etNewStudentContact.addTextChangedListener(textWatcher)
        etNewStudentDefaultPayment.addTextChangedListener(textWatcher)
        etNewStudentLevelNum.addTextChangedListener(textWatcher)
        etNewStudentLevelType.addTextChangedListener(textWatcher)
    }

    private fun setupObservers() {

        viewModel.allStudents.observe(viewLifecycleOwner, Observer {
            studentList.adapter = StudentsAdapter(context!!,it, viewModel)
            studentList.setOnItemClickListener { _,_, pos,_ ->
                showNewStudentLayout()
                btnNewStudentShow.setImageResource(R.drawable.ic_backup_gray)
                btnNewStudentShow.rotation = 0F
                etNewStudentName.setText((it[pos].studentName))
                etNewStudentContact.setText(it[pos].studentCell)
                etNewStudentDefaultPayment.setText(it[pos].defaultPayment.toString())
                val level = it[pos].studentLevel?.split("-") ?: listOf()
                if (level.size > 0) etNewStudentLevelNum.setText(level.get(0))
                if (level.size > 1) etNewStudentLevelType.setText(level.get(1))
                btnStudentsTrains.setActivated(it[pos].studentTrains ?: false)
                hideAllNewStudentButtons()
                newStudent = false
                viewModel.setCurrentStudent(pos)
            }
            studentList.setOnItemLongClickListener { parent, view, pos, id ->
                viewModel.onRemoveStudentClick(it[pos])
                true
            }
        })
        viewModel.newStudentLevelNum.observe(viewLifecycleOwner, Observer {
            etNewStudentLevelNum.setText(it.toString())
        })
        viewModel.newStudentLevelType.observe(viewLifecycleOwner, Observer {
            etNewStudentLevelType.setText(it)
        })
        viewModel.removeStudentClick.observe(viewLifecycleOwner, Observer {
            if (it.isFirstRun) {
                val student = it.getContent()
                val dialog = AlertDialog.Builder(context)
                dialog.apply {
                    setMessage("Apagar o aluno ${student.studentName}?")
                    setCancelable(true)
                    setPositiveButton("Sim") { _, _ ->
                        viewModel.removeStudent(student)
                        viewModel.removeStudentNotesOfStudent(student)
                        clearNewStudentData()
                        hideNewStudentLayout()
                        newStudent = true
                        updateNewStudentButton()
                    }
                    setNegativeButton("Não") { _, _ -> }
                    show()
                }
            }
        })
        viewModel.getStudents()
    }

    private fun setupButtons() {
        btnStudentCheckNotes.setOnClickListener {
            if (!newStudent) viewModel.checkStudentNotesClick()
        }
        btnNewStudentShow.setOnClickListener {
            if (newStudent) hideOrShowStudentLayout()
            newStudent = true
            makeButtonNewStudent()
            clearNewStudentData()
            updateNewStudentButton()
            closeKeyboard(it)
        }
        btnStudentsTrains.setOnClickListener {
            btnStudentsTrains.setActivated(!btnStudentsTrains.isActivated)
            if (!newStudent) makeButtonChangeStudent()
        }
        btnStudentChange.setOnClickListener {
            val studentEntity = getStudentDataInLayout() ?: return@setOnClickListener
            viewModel.updateTheStudent(studentEntity)
            clearNewStudentData()
            hideNewStudentLayout()
            newStudent = true
            updateNewStudentButton()
            closeKeyboard(it)
        }
        btnStudentAdd.setOnClickListener {
            val newStudentEntity = getStudentDataInLayout() ?: return@setOnClickListener
            viewModel.addNewStudent(newStudentEntity)
            clearNewStudentData()
            hideNewStudentLayout()
            newStudent = true
            updateNewStudentButton()
            closeKeyboard(it)
        }

        btnStudentLevel.setOnClickListener {
            val dialog = Dialog(context!!)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.level_picker)

            val flParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            flParams.gravity = Gravity.CENTER

            val levelNumWheelFrameLayout = dialog.levelPickerNum
            val levelNum = (etNewStudentLevelNum.text.toString().toIntOrNull() ?: if (isDan()) 1 else 10) - 1

            addLevelNumPicker(levelNumWheelFrameLayout, isDan(), levelNum)

            val levelTypeWheelPicker = Utils.createWheelPicker(LEVEL_TYPES, context!!)
            val levelTypeWheelFrameLayout = dialog.levelPickerType

            levelTypeWheelFrameLayout.addView(levelTypeWheelPicker, flParams)
            levelTypeWheelPicker.setOnItemSelectedListener { _, _, pos ->
                remakeLevelNumPicker(levelNumWheelFrameLayout, pos==1)
            }
            levelTypeWheelFrameLayout.post{
                levelTypeWheelPicker.setSelectedItemPosition(if (isDan()) 1 else 0)
            }

            val acceptBtn = dialog.btnlevelPickerAdd
            acceptBtn.setOnClickListener {
                val type = LEVEL_TYPES[levelTypeWheelPicker.currentItemPosition]
                val levelNumValues = Utils.getLevelNumValues(levelTypeWheelPicker.currentItemPosition == 1)
                val level = levelNumValues[levelNumWheelPicker.currentItemPosition]
                viewModel.setNewStudentLevelLayout(Integer.parseInt(level), type)
                dialog.dismiss()
            }

            val cancelBtn = dialog.btnlevelPickerCancel
            cancelBtn.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    private fun isDan(): Boolean {
        return etNewStudentLevelType.text.toString() == DAN
    }

    private fun getStudentDataInLayout(): StudentEntity? {
        val defaultPayment = etNewStudentDefaultPayment.text.toString().toIntOrNull() ?: 0
        val newStudentName = etNewStudentName.text.toString()
        val newStudentContract = etNewStudentContact.text.toString()
        val newStudentLevelNum = etNewStudentLevelNum.text.toString()
        val newStudentLevelType = etNewStudentLevelType.text.toString()
        val newStudentLevel = "$newStudentLevelNum-$newStudentLevelType"
        val newStudentTrains = btnStudentsTrains.isActivated
        if (arrayOf(newStudentName, newStudentLevelNum, newStudentLevelType).contains("")) return null
        return StudentEntity(null,newStudentName,newStudentLevel,newStudentContract,newStudentTrains,defaultPayment)
    }

    private fun remakeLevelNumPicker(levelNumWheelFrameLayout: FrameLayout, isDan: Boolean){
        levelNumWheelFrameLayout.removeAllViews()
        addLevelNumPicker(levelNumWheelFrameLayout, isDan, null)
    }

    private fun addLevelNumPicker(levelNumWheelFrameLayout: FrameLayout, isDan: Boolean, currentPos: Int?) {
        val flParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        flParams.gravity = Gravity.CENTER

        val levelNumValues = Utils.getLevelNumValues(isDan)
        val newLevelNumWheelPicker = Utils.createWheelPicker(levelNumValues,context!!)
        levelNumWheelFrameLayout.addView(newLevelNumWheelPicker, flParams)
        levelNumWheelFrameLayout.post{
            newLevelNumWheelPicker.setSelectedItemPosition(
                if (isDan) {
                    currentPos ?: 0
                } else {
                    10 - (currentPos ?: 10)
                }
            )
        }
        levelNumWheelPicker = newLevelNumWheelPicker
    }

    private fun updateNewStudentButton() {
        if (!newStudent) {
            showNewStudentLayout()
            btnNewStudentShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
            btnNewStudentShow.rotation = 45F
        } else {
            if (newStudentLayout.visibility == View.VISIBLE) {
                btnNewStudentShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
                btnNewStudentShow.rotation = 45F
            } else{
                btnNewStudentShow.setImageResource(R.drawable.ic_add_circle_outline_gray)
                btnNewStudentShow.rotation = 0F
            }
        }
    }

    private fun clearNewStudentData() {
        etNewStudentName.setText("")
        etNewStudentLevelType.setText("")
        etNewStudentLevelNum.setText("")
        etNewStudentDefaultPayment.setText("35")
        etNewStudentName.setText("")
        etNewStudentContact.setText("")
        btnStudentsTrains.setActivated(true)
    }

    private fun makeButtonNewStudent() {
        btnStudentAdd.visibility = View.VISIBLE
        btnStudentChange.visibility = View.GONE
        btnStudentCheckNotes.visibility = View.GONE
    }

    private fun makeButtonChangeStudent() {
        btnStudentAdd.visibility = View.GONE
        btnStudentChange.visibility = View.VISIBLE
        btnStudentCheckNotes.visibility = View.VISIBLE
    }

    private fun hideAllNewStudentButtons() {
        btnStudentAdd.visibility = View.GONE
        btnStudentChange.visibility = View.GONE
        btnStudentCheckNotes.visibility = View.VISIBLE
    }

    private fun hideOrShowStudentLayout() {
        newStudentLayout.visibility = if (newStudentLayout.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun showNewStudentLayout() {
        newStudentLayout.visibility = View.VISIBLE
    }

    private fun hideNewStudentLayout() {
        newStudentLayout.visibility = View.GONE
    }

    fun closeKeyboard(view : View) {
        view.clearFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        view.let{ imm?.hideSoftInputFromWindow(it.windowToken, 0)}
    }

}

