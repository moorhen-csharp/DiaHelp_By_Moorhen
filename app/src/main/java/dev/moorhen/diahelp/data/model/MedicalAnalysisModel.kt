package dev.moorhen.diahelp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "medical_analysis_records",
    foreignKeys = [ForeignKey(
        entity = UserModel::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MedicalAnalysisModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val analysisType: String,   // "blood", "urine", "hormones", "lipids"

    // Общий анализ крови
    val hba1c: Double? = null,          // %
    val cpeptide: Double? = null,       // нг/мл
    val hemoglobin: Double? = null,     // г/л
    val leukocytes: Double? = null,     // ×10⁹/л
    val platelets: Double? = null,      // ×10⁹/л

    // Липидный профиль
    val cholesterol: Double? = null,    // ммоль/л
    val hdl: Double? = null,            // ммоль/л
    val ldl: Double? = null,            // ммоль/л
    val triglycerides: Double? = null,  // ммоль/л

    // Биохимия
    val creatinine: Double? = null,     // ммоль/л
    val urea: Double? = null,           // ммоль/л
    val alt: Double? = null,            // Ед/л
    val ast: Double? = null,            // Ед/л

    val date: Date = Date(),
    val syncedToHc: Boolean = false
)
