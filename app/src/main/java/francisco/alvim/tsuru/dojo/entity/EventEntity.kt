package francisco.alvim.tsuru.dojo.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "event")
data class EventEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int?,

    @ColumnInfo(name = "eventName") var eventName: String,
    @ColumnInfo(name = "defaultPayment") var defaultPayment: Double,
    @ColumnInfo(name = "eventDate") var eventDate: Long


)