package dev.moorhen.diahelp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.moorhen.diahelp.data.model.InsulinModel
import dev.moorhen.diahelp.data.model.MedicalAnalysisModel
import dev.moorhen.diahelp.data.model.UserModel
import dev.moorhen.diahelp.data.model.SugarModel
import dev.moorhen.diahelp.utils.Converters

@Database(
    entities = [UserModel::class, SugarModel::class, InsulinModel::class, MedicalAnalysisModel::class],
    version = 6,
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

        /**
         * Миграция 5 → 6: создаём таблицу medical_analysis_records
         * БЕЗ уничтожения существующих данных сахара и инсулина.
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medical_analysis_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `analysisType` TEXT NOT NULL,
                        `hba1c` REAL,
                        `cpeptide` REAL,
                        `hemoglobin` REAL,
                        `leukocytes` REAL,
                        `platelets` REAL,
                        `cholesterol` REAL,
                        `hdl` REAL,
                        `ldl` REAL,
                        `triglycerides` REAL,
                        `creatinine` REAL,
                        `urea` REAL,
                        `alt` REAL,
                        `ast` REAL,
                        `date` INTEGER NOT NULL,
                        `syncedToHc` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
            }
        }

        /**
         * Миграция 4 → 5: для устройств которые пропустили версию 5.
         * Аналогично создаём таблицу анализов.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medical_analysis_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `analysisType` TEXT NOT NULL,
                        `hba1c` REAL,
                        `cpeptide` REAL,
                        `hemoglobin` REAL,
                        `leukocytes` REAL,
                        `platelets` REAL,
                        `cholesterol` REAL,
                        `hdl` REAL,
                        `ldl` REAL,
                        `triglycerides` REAL,
                        `creatinine` REAL,
                        `urea` REAL,
                        `alt` REAL,
                        `ast` REAL,
                        `date` INTEGER NOT NULL,
                        `syncedToHc` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
            }
        }

        /**
         * Миграция 3 → 4: для устройств на совсем старой версии.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // В версии 4 добавлялась таблица medical_analysis_records
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medical_analysis_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `analysisType` TEXT NOT NULL,
                        `hba1c` REAL,
                        `cpeptide` REAL,
                        `hemoglobin` REAL,
                        `leukocytes` REAL,
                        `platelets` REAL,
                        `cholesterol` REAL,
                        `hdl` REAL,
                        `ldl` REAL,
                        `triglycerides` REAL,
                        `creatinine` REAL,
                        `urea` REAL,
                        `alt` REAL,
                        `ast` REAL,
                        `date` INTEGER NOT NULL,
                        `syncedToHc` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diahelp_db"
                )
                    // Миграции вместо destructive — данные сохраняются!
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    // fallbackToDestructiveMigration() УДАЛЁН
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
