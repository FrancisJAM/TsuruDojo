package francisco.alvim.newtsurudojo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import francisco.alvim.newtsurudojo.dao.*
import francisco.alvim.newtsurudojo.entity.MonthPaymentEntity
import francisco.alvim.newtsurudojo.entity.EventEntity
import francisco.alvim.newtsurudojo.entity.EventPaymentEntity
import francisco.alvim.newtsurudojo.entity.StudentEntity

@Database( version = 1, entities = arrayOf(
        StudentEntity::class,
        EventEntity::class,
        EventPaymentEntity::class,
        MonthPaymentEntity::class
))
abstract class AppDatabase : RoomDatabase(){
    abstract fun studentDao() : StudentDao
    abstract fun monthPaymentDao() : MonthPaymentDao
    abstract fun eventsDao() : EventsDao
    abstract fun eventPaymentsDao() : EventsPaymentsDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            AppDatabase::class.java, "tsuru-dojo.db").build()
    }
}