package dev.moorhen.diahelp.utils

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
//import androidx.health.connect.client.records.Hba1cRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.BloodGlucose
import androidx.health.connect.client.units.Percentage
import dev.moorhen.diahelp.data.model.InsulinModel
import dev.moorhen.diahelp.data.model.SugarModel
import java.time.Instant
import java.time.ZoneOffset
import java.util.Date

object HealthConnectManager {

    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getWritePermission(BloodGlucoseRecord::class),
//        HealthPermission.getReadPermission(Hba1cRecord::class),
//        HealthPermission.getWritePermission(Hba1cRecord::class),
    )

    fun isAvailable(context: Context): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    fun getClientOrNull(context: Context): HealthConnectClient? =
        if (isAvailable(context)) HealthConnectClient.getOrCreate(context) else null

    fun createPermissionRequestContract() =
        PermissionController.createRequestPermissionResultContract()

    suspend fun hasAllPermissions(client: HealthConnectClient): Boolean =
        client.permissionController.getGrantedPermissions().containsAll(PERMISSIONS)

    suspend fun writeSugarNotes(
        client: HealthConnectClient,
        notes: List<SugarModel>
    ): Int {
        val records = notes
            .filter { it.SugarLevel > 0 }
            .map { note ->
                val instant = note.Date.toInstant()
                BloodGlucoseRecord(
                    time = instant,
                    zoneOffset = ZoneOffset.systemDefault().rules.getOffset(instant),
                    level = BloodGlucose.millimolesPerLiter(note.SugarLevel),
                    specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_CAPILLARY_BLOOD,
                    metadata = Metadata.manualEntry()                )
            }
        if (records.isEmpty()) return 0
        client.insertRecords(records)
        return records.size
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun writeInsulinNotes(
        client: HealthConnectClient,
        notes: List<InsulinModel>
    ): Int = 0

//    suspend fun writeHba1c(client: HealthConnectClient, hba1cPercent: Double) {
//        val now = Instant.now()
//        val record = Hba1cRecord(
//            time = now,
//            zoneOffset = ZoneOffset.systemDefault().rules.getOffset(now),
//            percentage = Percentage(hba1cPercent),
//            metadata = Metadata.manualEntry()
//        )
//        client.insertRecords(listOf(record))
//    }

    suspend fun readBloodGlucose(
        client: HealthConnectClient,
        userId: Int,
        startMs: Long = System.currentTimeMillis() - 30L * 24 * 3600 * 1000,
        endMs: Long = System.currentTimeMillis()
    ): List<SugarModel> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = BloodGlucoseRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.ofEpochMilli(startMs),
                    Instant.ofEpochMilli(endMs)
                )
            )
        )
        return response.records.map { record ->
            SugarModel(
                userId = userId,
                SugarLevel = record.level.inMillimolesPerLiter,
                MeasurementTime = "Другое",
                HealthType = "Норма",
                InsulinDose = 0.0,
                Date = Date(record.time.toEpochMilli())
            )
        }
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun readInsulinDelivery(
        client: HealthConnectClient,
        userId: Int,
        startMs: Long = System.currentTimeMillis() - 30L * 24 * 3600 * 1000,
        endMs: Long = System.currentTimeMillis()
    ): List<InsulinModel> = emptyList()
}