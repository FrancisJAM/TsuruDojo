package francisco.alvim.tsuru.dojo.dao

import androidx.room.*
import francisco.alvim.tsuru.dojo.entity.BalanceEntity

@Dao
interface BalanceDao {

    @Query("SELECT * FROM balancePayment")
    fun getAllBalanceMovements(): List<BalanceEntity>

    @Query("SELECT * FROM balancePayment WHERE movementMonth = :month AND movementYear = :year")
    fun getBalanceMovements(month: Int,year: Int): List<BalanceEntity>

    @Insert
    fun insertBalanceMovement(vararg balanceMovement: BalanceEntity)

    @Delete
    fun deleteBalanceMovement(balanceMovement: BalanceEntity)

    @Update
    fun updateBalanceMovement(vararg balanceMovement: BalanceEntity)

}