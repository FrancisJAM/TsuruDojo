package francisco.alvim.tsuru.dojo.entity

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity (tableName = "balancePayment")
data class BalanceEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int?,

    @Nullable @ColumnInfo(name = "movementMonth") var movementMonth: Int?,
    @Nullable @ColumnInfo(name = "movementYear") var movementYear: Int?,
    @Nullable @ColumnInfo(name = "movementAmount") var movementAmount: Double?,
    @Nullable @ColumnInfo(name = "movementDate") var movementDate: Long?,
    @Nullable @ColumnInfo(name = "movementName") var movementName: String?


)