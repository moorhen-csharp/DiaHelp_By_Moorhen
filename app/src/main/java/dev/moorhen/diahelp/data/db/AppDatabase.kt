package dev.moorhen.diahelp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.moorhen.diahelp.data.db.SugarDao
import dev.moorhen.diahelp.data.model.InsulinModel
import dev.moorhen.diahelp.data.model.MedicalAnalysisModel
import dev.moorhen.diahelp.data.model.UserModel
import dev.moorhen.diahelp.data.model.SugarModel
import dev.moorhen.diahelp.utils.Converters

@Database(
    entities = [UserModel::class, SugarModel::class, InsulinModel::class, MedicalAnalysisModel::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sugarDao(): SugarDao
    abstract fun insulinDao(): InsulinDao
    abstract fun medicalAnalysisDao(): MedicalAnalysisDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =  Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diahelp_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
