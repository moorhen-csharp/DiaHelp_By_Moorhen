//package dev.moorhen.diahelp.utils
//
//import android.content.Context
//import androidx.health.connect.client.HealthConnectClient
//import androidx.health.connect.client.PermissionController
//import androidx.health.connect.client.permission.HealthPermission
//import androidx.health.connect.client.records.BloodGlucoseRecord
//import androidx.health.connect.client.records.InsulinDeliveryRecord
//
///**
// * Заготовка для интеграции с Health Connect.
// *
// * На данный момент класс только проверяет наличие Health Connect на устройстве
// * и определяет набор необходимых разрешений. Чтение/запись данных глюкозы
// * и инсулина будет реализовано на следующем этапе.
// *
// * Зависимость "androidx.health.connect:connect-client" уже добавлена в build.gradle.kts,
// * а необходимые <uses-permission> — в AndroidManifest.xml.
// */
//object HealthConnectManager {
//
//    /**
//     * Набор разрешений, которые потребуются приложению при полной интеграции:
//     * чтение и запись уровня глюкозы крови и доз инсулина.
//     */
//    val PERMISSIONS = setOf(
//        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
//        HealthPermission.getWritePermission(BloodGlucoseRecord::class),
//        HealthPermission.getReadPermission(InsulinDeliveryRecord::class),
//        HealthPermission.getWritePermission(InsulinDeliveryRecord::class),
//    )
//
//    /**
//     * Проверяет, доступен ли Health Connect на устройстве (установлено приложение
//     * Health Connect / поддерживается системой).
//     */
//    fun isAvailable(context: Context): Boolean {
//        val status = HealthConnectClient.getSdkStatus(context)
//        return status == HealthConnectClient.SDK_AVAILABLE
//    }
//
//    /**
//     * Возвращает клиент Health Connect, если он доступен, иначе null.
//     */
//    fun getClientOrNull(context: Context): HealthConnectClient? {
//        return if (isAvailable(context)) {
//            HealthConnectClient.getOrCreate(context)
//        } else {
//            null
//        }
//    }
//
//    /**
//     * Возвращает контракт для запроса разрешений Health Connect.
//     * Использование (в Activity/Fragment):
//     *
//     * val requestPermissions = registerForActivityResult(
//     *     HealthConnectManager.createPermissionRequestContract()
//     * ) { granted -> ... }
//     *
//     * requestPermissions.launch(HealthConnectManager.PERMISSIONS)
//     */
//    fun createPermissionRequestContract() =
//        PermissionController.createRequestPermissionResultContract()
//
//    // TODO: реализовать чтение последних записей BloodGlucoseRecord/InsulinDeliveryRecord
//    // TODO: реализовать запись новых записей SugarModel/InsulinModel в Health Connect
//}
