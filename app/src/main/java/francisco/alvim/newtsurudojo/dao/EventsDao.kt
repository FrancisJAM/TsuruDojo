package francisco.alvim.newtsurudojo.dao

import androidx.room.*
import francisco.alvim.newtsurudojo.entity.EventEntity

@Dao
interface EventsDao {

    @Query("SELECT * FROM event ORDER BY event.eventDate DESC")
    fun getAllEvents(): List<EventEntity>

    @Insert
    fun insertEvent(vararg event: EventEntity)

    @Delete
    fun deleteEvent(event: EventEntity)

    @Update
    fun updateEvent(vararg event: EventEntity)
}