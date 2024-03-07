package francisco.alvim.tsuru.dojo.entity

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity (tableName = "monthPayment")
data class MonthPaymentEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int?,

    @Nullable @ColumnInfo(name = "studentId") var studentId: Int?,
    @Nullable @ColumnInfo(name = "paymentMonth") var paymentMonth: Int?,
    @Nullable @ColumnInfo(name = "paymentYear") var paymentYear: Int?,
    @Nullable @ColumnInfo(name = "paymentAmount") var paymentAmount: Double?,
    @Nullable @ColumnInfo(name = "paymentDate") var paymentDate: Long?


)