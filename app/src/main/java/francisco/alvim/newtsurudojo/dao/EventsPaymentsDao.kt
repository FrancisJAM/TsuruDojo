package francisco.alvim.newtsurudojo.dao

import androidx.room.*
import francisco.alvim.newtsurudojo.entity.EventPaymentEntity

@Dao
interface EventsPaymentsDao {

    @Query("SELECT * FROM eventPayments")
    fun getAllEventsPayments(): List<EventPaymentEntity>

    @Query("SELECT * FROM eventPayments WHERE eventId = :eventId")
    fun getEventPayments(eventId: Int): List<EventPaymentEntity>

    @Insert
    fun insertEventPayments(vararg specialGroup: EventPaymentEntity)

    @Delete
    fun deleteEventPayments(specialGroup: EventPaymentEntity)

    @Update
    fun updateEventPayments(vararg specialGroup: EventPaymentEntity)

    @Query("DELETE FROM eventPayments WHERE eventId = :eventId")
    fun deleteEventPaymentsInEvent(eventId: Int)

}