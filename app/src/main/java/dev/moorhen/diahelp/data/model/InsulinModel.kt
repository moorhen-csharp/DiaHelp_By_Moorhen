package dev.moorhen.diahelp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "insulin_notes", // Изменено имя таблицы
    foreignKeys = [ForeignKey(
        entity = UserModel::class, // Предполагается, что у вас есть UserModel
        parentColumns = ["id"],    // столбец в родительской таблице
        childColumns = ["userId"], // столбец в этой таблице
        onDelete = ForeignKey.CASCADE // Опционально: удалять записи при удалении пользователя
    )]
)
data class InsulinModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val InsulinDose: Double,
    val Date: Date = Date()
)