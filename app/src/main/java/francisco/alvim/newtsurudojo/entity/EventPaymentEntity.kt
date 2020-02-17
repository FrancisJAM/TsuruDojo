package francisco.alvim.newtsurudojo.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "eventPayments")
data class EventPaymentEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int?,

    @ColumnInfo(name = "eventId") var eventId: Int,
    @ColumnInfo(name = "studentName") var studentName: String,
    @ColumnInfo(name = "paymentAmount") var paymentAmount: Double,
    @ColumnInfo(name = "datePayment") var datePayment: Long,
    @ColumnInfo(name = "payed") var payed: Boolean

)