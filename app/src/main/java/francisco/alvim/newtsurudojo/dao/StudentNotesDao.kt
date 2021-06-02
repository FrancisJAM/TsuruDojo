package francisco.alvim.newtsurudojo.dao

import androidx.room.*
import francisco.alvim.newtsurudojo.entity.StudentEntity
import francisco.alvim.newtsurudojo.entity.StudentNotesEntity

@Dao
interface StudentNotesDao {
    @Query("SELECT * FROM studentNotes")
    fun getAllStudentsNotes(): List<StudentNotesEntity>

    @Query("SELECT * FROM studentNotes WHERE studentId == :studentId")
    fun getStudentNotesOfStudent(studentId: Int): List<StudentNotesEntity>

    @Insert
    fun insertStudentNote(vararg student: StudentNotesEntity)

    @Delete
    fun deleteStudentNote(student: StudentNotesEntity)

    @Update
    fun updateStudentNote(vararg student: StudentNotesEntity)
}