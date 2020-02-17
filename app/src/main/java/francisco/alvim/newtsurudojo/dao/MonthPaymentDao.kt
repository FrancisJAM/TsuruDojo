package francisco.alvim.newtsurudojo.dao

import androidx.room.*
import francisco.alvim.newtsurudojo.entity.MonthPaymentEntity

@Dao
interface MonthPaymentDao {
    @Query("SELECT * FROM monthPayment")
    fun getAllMonthPayments(): List<MonthPaymentEntity>

    @Query("SELECT * FROM monthPayment WHERE paymentMonth = :month AND paymentYear = :year")
    fun getMonthPaymentsByDate(month: Int, year: Int): List<MonthPaymentEntity>

    @Query("SELECT * FROM monthPayment WHERE paymentMonth = :month AND paymentYear = :year AND studentId = :studentId")
    fun getMonthPaymentsOfStudentByDate(month: Int, year: Int, studentId: Int): List<MonthPaymentEntity>

    @Query("SELECT studentId FROM monthPayment WHERE paymentMonth = :month AND paymentYear = :year")
    fun getStudentsThatPayedByDate(month: Int, year: Int): List<Int>

    @Insert
    fun insertMonthPayment(vararg monthPayment: MonthPaymentEntity)

    @Delete
    fun deleteMonthPayment(monthPayment: MonthPaymentEntity)

    @Update
    fun updateMonthPayment(vararg monthPayment: MonthPaymentEntity)

}