package francisco.alvim.tsuru.dojo.dao

import androidx.room.*
import francisco.alvim.tsuru.dojo.entity.StudentEntity

@Dao
interface StudentDao {
    @Query("SELECT * FROM student")
    fun getAllStudents(): List<StudentEntity>

    @Query("SELECT * FROM student WHERE studentTrains")
    fun getTrainingStudents(): List<StudentEntity>

    @Query("SELECT * FROM student WHERE id = :id")
    fun getStudentById(id : Int): StudentEntity

    @Query("SELECT studentName FROM student WHERE id = :id")
    fun getStudentName(id : Int): String

    @Query("SELECT studentName FROM student WHERE studentName NOT IN (SELECT studentName FROM eventPayments WHERE eventId = :eventId)")
    fun getUnselectedStudentNames(eventId : Int): List<String>

    @Query("SELECT id FROM student WHERE studentTrains")
    fun getTrainingStudentId(): List<Int>

    @Query("SELECT id FROM student WHERE studentTrains AND id != (SELECT studentId FROM monthPayment WHERE paymentMonth = :month AND paymentYear = :year)")
    fun getTrainingStudentIdsThatDidNotPayYet(month: Int, year: Int): List<Int>

    @Query("SELECT * FROM student WHERE studentTrains AND id NOT IN (SELECT studentId FROM monthPayment WHERE paymentMonth = :month AND paymentYear = :year)")
    fun getTrainingStudentsThatDidNotPayYet(month: Int, year: Int): List<StudentEntity>

    @Insert
    fun insertStudent(vararg student: StudentEntity)

    @Delete
    fun deleteStudent(student: StudentEntity)

    @Update
    fun updateStudent(vararg student: StudentEntity)

}