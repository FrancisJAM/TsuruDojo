package francisco.alvim.newtsurudojo.entity

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "student")
data class StudentEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int?,

    @Nullable @ColumnInfo(name = "studentName") var studentName: String?,
    @Nullable @ColumnInfo(name = "studentLevel") var studentLevel: String?,
    @Nullable @ColumnInfo(name = "studentCell") var studentCell: String?,
    @Nullable @ColumnInfo(name = "studentTrains") var studentTrains: Boolean?,
    @Nullable @ColumnInfo(name = "defaultPayment") var defaultPayment: Int?

)