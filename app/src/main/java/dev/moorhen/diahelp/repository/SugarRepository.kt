package dev.moorhen.diahelp.data.repository

import android.content.Context
import dev.moorhen.diahelp.data.db.AppDatabase
import dev.moorhen.diahelp.data.model.SugarModel
import java.util.*

class SugarRepository(context: Context) {

    private val dao = AppDatabase.getDatabase(context).sugarDao()

    suspend fun insert(note: SugarModel) = dao.insert(note)

    // Старый метод: возвращает все записи из БД (теперь не нужен для UI)
    // suspend fun getAllSugarNotes() = dao.getAll()

    // Новый метод: возвращает записи конкретного пользователя
    suspend fun getAllSugarNotesByUserId(userId: Int) = dao.getAllByUserId(userId)

    // Старый метод: очищает всю таблицу (не рекомендуется)
    // suspend fun clearAll() = dao.clear()

    // Новый метод: очищает записи конкретного пользователя
    suspend fun clearAllForUser(userId: Int) = dao.clearByUserId(userId)

    // Обновленный метод: возвращает записи конкретного пользователя за период
    suspend fun getNotesByPeriod(userId: Int, startDate: Date, endDate: Date) =
        dao.getByPeriod(userId, startDate, endDate)

    /**
     * Вставляет запись, только если для этого userId+Date ещё нет записи в БД.
     * Используется при импорте из Health Connect, чтобы не плодить дубли
     * при повторных запусках синхронизации.
     * Возвращает true, если запись была реально добавлена.
     */
    suspend fun insertIfNotExists(note: SugarModel): Boolean {
        val exists = dao.countByUserIdAndDate(note.userId, note.Date) > 0
        if (!exists) {
            dao.insert(note)
            return true
        }
        return false
    }

    // Возвращает пары (время в мс, уровень сахара) для построения графика.
    // Записи "не измерял" (SugarLevel == -1.0) исключаются.
    suspend fun getAllSugarDataWithDates(userId: Int): List<Pair<Long, Double>> {
        return getAllSugarNotesByUserId(userId)
            .filter { it.SugarLevel != -1.0 }
            .map { Pair(it.Date.time, it.SugarLevel) }
            .sortedBy { it.first }
    }

    suspend fun getSugarDataByPeriod(userId: Int, startDate: Date, endDate: Date): List<Pair<Long, Double>> {
        return getNotesByPeriod(userId, startDate, endDate)
            .filter { it.SugarLevel != -1.0 }
            .map { Pair(it.Date.time, it.SugarLevel) }
            .sortedBy { it.first }
    }

}