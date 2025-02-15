package francisco.alvim.tsuru.dojo.entity

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity (tableName = "studentNotes")
data class StudentNotesEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int?,

    @Nullable @ColumnInfo(name = "studentId") var studentId: Int?,
    @Nullable @ColumnInfo(name = "studentNote") var studentNote: String?

)
