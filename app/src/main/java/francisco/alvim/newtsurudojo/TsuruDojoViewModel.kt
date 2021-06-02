package francisco.alvim.newtsurudojo

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import francisco.alvim.newtsurudojo.data.WheelType
import francisco.alvim.newtsurudojo.database.AppDatabase
import francisco.alvim.newtsurudojo.entity.*
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.*


class TsuruDojoViewModel(application : Application) : AndroidViewModel(application) {
    val job = Job()
    val uijob = Job()
    val ioScope = CoroutineScope(Dispatchers.IO + job)
    val uiScope = CoroutineScope(Dispatchers.Main + job)
    val database = AppDatabase.invoke(application)
    val removeMonthPaymentClick = MutableLiveData<Event<MonthPaymentEntity>>()
    val removeStudentClick = MutableLiveData<Event<StudentEntity>>()
    val removeStudentNoteClick = MutableLiveData<Event<StudentNotesEntity>>()
    val removeEventClick = MutableLiveData<Event<EventEntity>>()
    val removeEventPaymentClick = MutableLiveData<Event<EventPaymentEntity>>()
    val paymentsDoneInMonth = MutableLiveData<List<Pair<StudentEntity,MonthPaymentEntity?>>>()
    val currentMonthAndYear = MutableLiveData<String>()
    val studentNameOfStudentNotes = MutableLiveData<String>()
    val allStudents = MutableLiveData<List<StudentEntity>>()
    val allNotesOfStudent = MutableLiveData<List<StudentNotesEntity>>()
    val studentsNotInEventPaymentList = mutableListOf<String>()
    val selectedNamesInPickerList = mutableListOf<String>()
    val studentsNotInEventPayment = MutableLiveData<Event<List<String>>>()
    val selectedNamesInPicker = MutableLiveData<Event<List<String>>>()
    val allEvents = MutableLiveData<List<EventEntity>>()
    val currentEventPayments = MutableLiveData<List<EventPaymentEntity>>()
    val newStudentLevelNum = MutableLiveData<Int>()
    val newStudentLevelType = MutableLiveData<String>()
    val newPaymentMonth = MutableLiveData<Int>()
    val newPaymentYear = MutableLiveData<Int>()
    val newPaymentDateDay = MutableLiveData<Int>()
    val newPaymentDateMonth = MutableLiveData<Int>()
    val newPaymentDateYear = MutableLiveData<Int>()
    val newEventDateDay = MutableLiveData<Int>()
    val newEventDateMonth = MutableLiveData<Int>()
    val newEventDateYear = MutableLiveData<Int>()
    val newEventPaymentDateDay = MutableLiveData<Int>()
    val newEventPaymentDateMonth = MutableLiveData<Int>()
    val newEventPaymentDateYear = MutableLiveData<Int>()
    val newPaymentStudentsNames = MutableLiveData<List<String>>()
    val namePosition = MutableLiveData<Int>()
    val totalMonthPayment = MutableLiveData<String>()
    val totalEventPayment = MutableLiveData<String>()
    val allBalanceMovements = MutableLiveData<List<BalanceEntity>>()
    val newMovementDateDay = MutableLiveData<Int>()
    val newMovementDateMonth = MutableLiveData<Int>()
    val newMovementDateYear = MutableLiveData<Int>()
    val openEventPage = MutableLiveData<Event<Unit>>()
    val openStudentNotes = MutableLiveData<Event<Unit>>()
    val onbackClick = MutableLiveData<Event<Unit>>()

    var currentStudent = StudentEntity(null,"","","",true,35)
    var currentStudentNote = StudentNotesEntity(null,null,"")
    var currentEvent = EventEntity(null,"",0.0,0)
    var currentEventPayment = EventPaymentEntity(null,0,"",0.0,0,false)
    var currentBalanceMovement = BalanceEntity(null,0,0,0.0,0,"")
    val paymentsInMonth = mutableListOf<Pair<StudentEntity,MonthPaymentEntity?>>()
    val arrangedIdList = mutableListOf<Int>()
    val arrangedNameList = mutableListOf<String>()
    var totalInMonth = 0.0

    var paymentsMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    var paymentsYear = Calendar.getInstance().get(Calendar.YEAR)


    init {
        ioScope.launch {
            val studentList = database.studentDao().getAllStudents()

            studentList.forEach {
                arrangedNameList.add(it.studentName ?: "")
            }
            arrangeStudents(studentList)

            Handler(Looper.getMainLooper()).post{
                changeMonth(0)
                newPaymentStudentsNames.value = arrangedNameList
            }
        }
    }

    private fun arrangeStudents(studentList: List<StudentEntity>) {
        arrangedNameList.sort()
        arrangedNameList.forEach { name ->
            run loop@{
                studentList.forEach {
                    if (name == it.studentName) {
                        arrangedIdList.add(it.id ?: 0)
                        return@loop
                    }
                }
            }
        }
    }

    private fun getValuesOfMonth() {
        ioScope.launch {
            val trainingStudentsThatDidNotPay = database.studentDao().getTrainingStudentsThatDidNotPayYet(paymentsMonth, paymentsYear)
            val paymentsOfMonth = database.monthPaymentDao().getMonthPaymentsByDate(paymentsMonth, paymentsYear)
            val paymentList = mutableListOf<Pair<StudentEntity, MonthPaymentEntity?>>()

            trainingStudentsThatDidNotPay.forEach {
                paymentList.add(Pair(it,null))
            }
            totalInMonth = 0.0
            paymentsOfMonth.forEach {
                val student = database.studentDao().getStudentById(it.studentId)
                paymentList.add(Pair(student, it))
                totalInMonth += it.paymentAmount
            }
            paymentsInMonth.clear()
            paymentsInMonth.addAll(paymentList)
            Handler(Looper.getMainLooper()).post{
                paymentsDoneInMonth.value = paymentsInMonth
                totalMonthPayment.value = "Total mês: " + DecimalFormat("0.##").format(totalInMonth) + "€"
            }
        }
    }

    fun changeMonth(amount: Int) {
        paymentsMonth += amount
        if (paymentsMonth > 12) {
            paymentsMonth = 1
            paymentsYear += 1
        }
        else if (paymentsMonth < 1) {
            paymentsMonth = 12
            paymentsYear -= 1
        }
        currentMonthAndYear.value  = getMonthName() + " / $paymentsYear"
        getValuesOfMonth()
    }

    private fun getMonthName(): String {
        return when(paymentsMonth){
            1 -> "Janeiro"
            2 -> "Fevereiro"
            3 -> "Março"
            4 -> "Abril"
            5 -> "Maio"
            6 -> "Junho"
            7 -> "Julho"
            8 -> "Agosto"
            9 -> "Setembro"
            10 -> "Outubro"
            11 -> "Novembro"
            12 -> "Dezembro"
            else -> "Janeiro"
        }
    }

    fun addNewPayment(namePos: Int, amount: String, month: Int, year: Int, dateDay: Int, dateMonth: Int, dateYear: Int) {

        val date = GregorianCalendar(dateYear,dateMonth,dateDay)
        val payedAmount = amount.toDoubleOrNull() ?: 0.0
        if (payedAmount <= 0.0) return
        val monthPayment = MonthPaymentEntity(
            null,
            arrangedIdList[namePos],
            month,
            year,
            payedAmount,
            date.timeInMillis
        )
        ioScope.launch {
            val currentPayment = database.monthPaymentDao().getMonthPaymentsOfStudentByDate(month,year,arrangedIdList[namePos])
            if (currentPayment.isEmpty()) {
                database.monthPaymentDao().insertMonthPayment(monthPayment)
            } else {
                val updatePayment = monthPayment.copy(id = currentPayment[0].id)
                database.monthPaymentDao().updateMonthPayment(updatePayment)
            }
            Handler(Looper.getMainLooper()).post {
                changeMonth(0)
            }
        }

    }

    fun setMonthPaymentInLayout(month: Int, year: Int) {
        newPaymentMonth.value = month
        newPaymentYear.value = year
    }

    fun setDateOfPaymentInLayout(day: Int, month: Int, year: Int) {
        newPaymentDateDay.value = day
        newPaymentDateMonth.value = month
        newPaymentDateYear.value = year
    }

    fun getClickedName(id: Int) {
        for(i in 0..arrangedNameList.size-1){
            if (arrangedIdList[i] == id){
                namePosition.value = i
                return
            }
        }
    }

    fun removePayment(payment: MonthPaymentEntity) {
        ioScope.launch {
            database.monthPaymentDao().deleteMonthPayment(payment)
            Handler(Looper.getMainLooper()).post {
                changeMonth(0)
            }
        }
    }

    fun getStudents() {
        ioScope.launch {
            val list = database.studentDao().getAllStudents()
            Handler(Looper.getMainLooper()).post {
                allStudents.value = list
            }
        }
    }

    fun updateStudent(student: StudentEntity) {
        ioScope.launch {
            database.studentDao().updateStudent(student)
            getStudents()
        }
    }

    fun addNewStudent(student: StudentEntity) {
        ioScope.launch {
            database.studentDao().insertStudent(student)
            getStudents()
        }
    }

    fun removeStudent(student: StudentEntity){
        ioScope.launch {
            database.studentDao().deleteStudent(student)
            getStudents()
        }
    }

    fun onRemoveStudentClick(student: StudentEntity){
        removeStudentClick.value = Event(student)
    }
    fun onRemoveMonthPaymentClick(pos: Int){
        paymentsInMonth[pos].second?.let {
            removeMonthPaymentClick.value = Event(it)
        }
    }

    fun updateTheStudent(name: String, contact: String, defaultPayment: Int, levelNum: String, levelType: String, trains: Boolean) {
        val student = StudentEntity(null,name,"$levelNum-$levelType",contact,trains,defaultPayment)
        updateTheStudent(student)
    }

    fun updateTheStudent(student: StudentEntity) {
        currentStudent.studentName = student.studentName
        currentStudent.studentCell = student.studentCell
        currentStudent.defaultPayment = student.defaultPayment
        currentStudent.studentLevel = student.studentLevel
        currentStudent.studentTrains = student.studentTrains
        updateStudent(currentStudent)
    }

    fun setCurrentStudent(pos: Int) {
        allStudents.value?.get(pos)?.let{
            currentStudent = it
        }
    }

    fun addNewStudent(name: String, contact: String, defaultPayment: Int, levelNum: String, levelType: String, trains: Boolean) {
        val student = StudentEntity(null,name, "$levelNum-$levelType",contact,trains,defaultPayment)
        addNewStudent(student)
    }

    fun setNewStudentLevelLayout(level: Int, type: String) {
        newStudentLevelNum.value = level
        newStudentLevelType.value = type
    }

    fun updateSpinnerNames() {
        ioScope.launch {
            val studentList = database.studentDao().getAllStudents()
            arrangedNameList.clear()
            arrangedIdList.clear()

            studentList.forEach {
                arrangedNameList.add(it.studentName ?: "")
            }
            arrangedNameList.sort()
            arrangedNameList.forEach { name ->
                run loop@{
                    studentList.forEach {
                        if (name == it.studentName) {
                            arrangedIdList.add(it.id ?: 0)
                            return@loop
                        }
                    }
                }
            }
            Handler(Looper.getMainLooper()).post{
                changeMonth(0)
                newPaymentStudentsNames.value = arrangedNameList
            }
        }
    }

    fun checkStudentNotesClick(){
        openStudentNotes.value = Event(Unit)
    }

    fun onRemoveStudentNotesClick(studentNote: StudentNotesEntity){
        removeStudentNoteClick.value = Event(studentNote)
    }

    fun getCurrentStudentNotes(){
        getStudentNotes(currentStudent.id ?: return)
        getStudentNameOfStudentNotes(currentStudent.id ?: return)
    }
    fun getStudentNameOfStudentNotes(studentId: Int) {
        ioScope.launch {
            val student = database.studentDao().getStudentById(studentId)
            Handler(Looper.getMainLooper()).post {
                studentNameOfStudentNotes.value = student.studentName
            }
        }
    }

    fun setCurrentStudentNote(pos: Int){
        allNotesOfStudent.value?.get(pos)?.let{
            currentStudentNote = it
        }
    }

    fun getStudentNotes(studentId: Int){
        ioScope.launch {
            val studentNotes = database.studentNotesDao().getStudentNotesOfStudent(studentId)
            Handler(Looper.getMainLooper()).post {
                allNotesOfStudent.value = studentNotes
            }
        }
    }

    fun addNewStudentNote(note:String) {
        addNewStudentNote(note, currentStudent.id ?: return)

    }

    fun addNewStudentNote(note:String, studentId: Int){
        ioScope.launch {
            val studentNote = StudentNotesEntity(null, studentId, note )
            database.studentNotesDao().insertStudentNote(studentNote)
            getStudentNotes(studentId)
        }
    }

    fun updateStudentNote(note:String) {
        updateStudentNote(note, currentStudentNote)
    }

    fun updateStudentNote(note: String, studentNote: StudentNotesEntity){
        ioScope.launch {
            studentNote.studentNote = note
            database.studentNotesDao().updateStudentNote(studentNote)
        }
    }

    fun removeStudentNote(studentNote: StudentNotesEntity){
        ioScope.launch {
            database.studentNotesDao().deleteStudentNote(studentNote)
            getStudentNotes(currentStudent.id ?: return@launch)
        }
    }

    fun removeStudentNotesOfStudent(student: StudentEntity){
        ioScope.launch {
            val students = database.studentNotesDao().getStudentNotesOfStudent(student.id ?: return@launch)
            students.forEach {
                database.studentNotesDao().deleteStudentNote(it)
            }
        }
    }



    fun getEvents() {
        ioScope.launch {
            val list = database.eventsDao().getAllEvents()
            Handler(Looper.getMainLooper()).post {
                allEvents.value = list
            }
        }
    }

    fun setDateOfEventInLayout(day: Int, month: Int, year: Int) {
        newEventDateDay.value = day
        newEventDateMonth.value = month
        newEventDateYear.value = year
    }

    fun addNewEvent(event: EventEntity) {
        ioScope.launch {
            database.eventsDao().insertEvent(event)
            getEvents()
        }
    }

    fun onRemoveEventClick(eventEntity: EventEntity) {
        removeEventClick.value = Event(eventEntity)
    }

    fun headToEventPage(eventEntity: EventEntity) {
        currentEvent = eventEntity
        openEventPage.value = Event(Unit)
    }

    fun removeEvent(event: EventEntity) {
        ioScope.launch {
            database.eventsDao().deleteEvent(event)
            event.id?.let{
                database.eventPaymentsDao().deleteEventPaymentsInEvent(it)
            }
            getEvents()
        }
    }



    fun getEventPayments() {
        ioScope.launch {
            val list = database.eventPaymentsDao().getEventPayments(currentEvent.id ?: 0)
            Handler(Looper.getMainLooper()).post {
                currentEventPayments.value = list
                var totalPayments = 0.0
                list.forEach {
                    totalPayments += it.paymentAmount
                }
                totalEventPayment.value = "Total evento: $totalPayments€"
            }
        }
    }

    fun getStudentsForEventPaymentSelection() {
        ioScope.launch {
            val list = database.studentDao().getUnselectedStudentNames(currentEvent.id ?: 0)
            selectedNamesInPickerList.clear()
            studentsNotInEventPaymentList.clear()
            studentsNotInEventPaymentList.addAll(list)
            Handler(Looper.getMainLooper()).post {
                if (studentsNotInEventPaymentList.isNotEmpty()) {
                    studentsNotInEventPayment.value = Event(studentsNotInEventPaymentList)
                }
            }
        }
    }

    fun onRemoveEventPaymentClick(eventPaymentEntity: EventPaymentEntity) {
        removeEventPaymentClick.value = Event(eventPaymentEntity)
    }

    fun removeEventPayment(eventPayment: EventPaymentEntity) {
        ioScope.launch {
            database.eventPaymentsDao().deleteEventPayments(eventPayment)
            getEventPayments()
        }
    }

    fun addNewEventPaymentStudent(name: String) {
        val eventPayment = EventPaymentEntity(
            id = null,
            eventId = currentEvent.id ?: 0,
            studentName= name,
            paymentAmount = 0.0,
            payed = false,
            datePayment = 0
        )
        ioScope.launch {
            database.eventPaymentsDao().insertEventPayments(eventPayment)
            getEventPayments()
        }
    }

    fun updateEventPayment(name: String, paymentAmount: Double, payed: Boolean, date: GregorianCalendar) {
        currentEventPayment = currentEventPayment.copy(studentName= name, paymentAmount = paymentAmount, payed = payed,datePayment = date.timeInMillis)
        ioScope.launch {
            database.eventPaymentsDao().updateEventPayments(currentEventPayment)
            getEventPayments()
        }
    }

    fun setDateOfEventPaymentInLayout(day: Int, month: Int, year: Int) {
        newEventPaymentDateDay.value = day
        newEventPaymentDateMonth.value = month
        newEventPaymentDateYear.value = year
    }


    fun getBalanceMovements(){
        ioScope.launch {
            val list = database.balanceDao().getAllBalanceMovements().toMutableList()
            list.sortByDescending { it.movementDate }
            Handler(Looper.getMainLooper()).post {
                allBalanceMovements.value = list
            }
        }
    }

    fun addBalanceMovement(name: String, amount: String, day: Int, month: Int, year: Int, isPositive: Boolean) {
        val date = GregorianCalendar(year,month-1,day)
        val movedAmount = amount.toDoubleOrNull() ?: 0.0
        if (movedAmount <= 0.0) return
        val movementAmount = movedAmount * if (isPositive) 1 else -1
        val balanceMovement = BalanceEntity(
            null,
            month,
            year,
            movementAmount,
            date.timeInMillis,
            name
        )
        ioScope.launch {
            database.balanceDao().insertBalanceMovement(balanceMovement)
            Handler(Looper.getMainLooper()).post{
                getBalanceMovements()
            }
        }
    }

    fun updateBalanceMovement(name: String, amount: String, day: Int, month: Int, year: Int, isPositive: Boolean) {
        val date = GregorianCalendar(year,month-1,day)
        val movedAmount = amount.toDoubleOrNull() ?: 0.0
        if (movedAmount <= 0.0) return
        val movementAmount = movedAmount * if (isPositive) 1 else -1

        currentBalanceMovement.movementMonth = month
        currentBalanceMovement.movementYear = year
        currentBalanceMovement.movementAmount = movementAmount
        currentBalanceMovement.movementDate = date.timeInMillis
        currentBalanceMovement.movementName = name
        ioScope.launch {
            database.balanceDao().updateBalanceMovement(currentBalanceMovement)
            Handler(Looper.getMainLooper()).post{
                getBalanceMovements()
            }
        }
    }

    fun removeBalanceMovement(balanceMovement: BalanceEntity){
        ioScope.launch {
            database.balanceDao().deleteBalanceMovement(balanceMovement)
            Handler(Looper.getMainLooper()).post{
                getBalanceMovements()
            }
        }
    }

    fun setCurrentMovement(pos: Int) {
        allBalanceMovements.value?.get(pos)?.let{
            currentBalanceMovement = it
        }
    }

    fun setDateOfMovementInLayout(day: Int, month: Int, year: Int) {
        newMovementDateDay.value = day
        newMovementDateMonth.value = month
        newMovementDateYear.value = year
    }

    fun onRemoveMovementClick(pos: Int) {

    }


    fun onBackClick() {
        onbackClick.value = Event(Unit)
    }

    fun addSelectedStudentNameInPicker(clickedPosition: Int) {
        if (studentsNotInEventPaymentList.size>clickedPosition)
            selectedNamesInPickerList.add(studentsNotInEventPaymentList[clickedPosition])
    }

    fun removeSelectedStudentNameInPicker(clickedPosition: Int) {
        if (selectedNamesInPickerList.contains(studentsNotInEventPaymentList[clickedPosition]) && studentsNotInEventPaymentList.size>clickedPosition)
            selectedNamesInPickerList.remove(studentsNotInEventPaymentList[clickedPosition])
    }

    fun addAllSelectedStudentNames(){
        if (selectedNamesInPickerList.isNotEmpty()){
            selectedNamesInPicker.value = Event(selectedNamesInPickerList)
        }
    }

    fun updateMonthInTop() {
        setMonthPaymentInLayout(paymentsMonth, paymentsYear)
    }

    fun onDateWheelAccept(day: Int, month: Int, year: Int, wheelType: WheelType) {
        when (wheelType) {
            WheelType.BALANCE_DATE_PICK -> setDateOfMovementInLayout(day,month,year)
            WheelType.EVENT_PAYMENTS_DATE_PICK -> setDateOfEventPaymentInLayout(day,month,year)
            WheelType.EVENTS_DATE_PICK -> setDateOfEventInLayout(day,month,year)
            WheelType.MONTH_PAYMENT_DATE_PICK -> setDateOfPaymentInLayout(day,month,year)
            else -> {}
        }
    }


}