package francisco.alvim.tsuru.dojo.database

import android.content.Context
import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import francisco.alvim.tsuru.dojo.dao.*
import francisco.alvim.tsuru.dojo.entity.*
import java.lang.Exception

@Database( version = 3, entities = arrayOf(
        StudentEntity::class,
        StudentNotesEntity::class,
        EventEntity::class,
        EventPaymentEntity::class,
        MonthPaymentEntity::class,
        BalanceEntity::class
))
abstract class AppDatabase : RoomDatabase(){
    abstract fun studentDao() : StudentDao
    abstract fun studentNotesDao() : StudentNotesDao
    abstract fun monthPaymentDao() : MonthPaymentDao
    abstract fun eventsDao() : EventsDao
    abstract fun eventPaymentsDao() : EventsPaymentsDao
    abstract fun balanceDao() : BalanceDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, "tsuru-dojo.db")
                .addMigrations(MIGRATION2)
                .addMigrations(MIGRATION3)
                .build()


        private val MIGRATION2 = object : Migration(1,2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.beginTransaction()
                try {
                    database.execSQL("CREATE TABLE `balancePayment` (`id` INTEGER, `movementMonth` INTEGER NOT NULL, `movementYear` INTEGER NOT NULL, `movementAmount` REAL NOT NULL, `movementDate` INTEGER NOT NULL, `movementName` TEXT NOT NULL, PRIMARY KEY(`id`))")
                    database.setTransactionSuccessful()
                } finally {
                    database.endTransaction()
                }
            }
        }
        private val MIGRATION3 = object : Migration(2,3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.beginTransaction()
                try {
                    database.execSQL("CREATE TABLE `studentNotes` (`id` INTEGER, `studentId` INTEGER, `studentNote` TEXT, PRIMARY KEY(`id`))")
                    database.setTransactionSuccessful()
                } finally {
                    database.endTransaction()
                }
            }
        }
    }

}