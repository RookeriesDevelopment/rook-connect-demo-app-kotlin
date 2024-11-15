package com.rookmotion.rookconnectdemo.features.sdkplayground

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rookmotion.rook.sdk.RookEventManager
import com.rookmotion.rook.sdk.RookHelpers
import com.rookmotion.rook.sdk.RookPermissionsManager
import com.rookmotion.rook.sdk.RookSummaryManager
import com.rookmotion.rook.sdk.domain.enums.HealthConnectAvailability
import com.rookmotion.rook.sdk.domain.enums.HealthDataType
import com.rookmotion.rook.sdk.domain.enums.SyncStatus
import com.rookmotion.rook.sdk.domain.exception.DeviceNotSupportedException
import com.rookmotion.rook.sdk.domain.exception.HealthConnectNotInstalledException
import com.rookmotion.rook.sdk.domain.exception.HttpRequestException
import com.rookmotion.rook.sdk.domain.exception.MissingHealthConnectPermissionsException
import com.rookmotion.rook.sdk.domain.exception.RequestQuotaExceededException
import com.rookmotion.rook.sdk.domain.exception.SDKNotAuthorizedException
import com.rookmotion.rook.sdk.domain.exception.SDKNotInitializedException
import com.rookmotion.rook.sdk.domain.exception.TimeoutException
import com.rookmotion.rook.sdk.domain.exception.UserNotInitializedException
import com.rookmotion.rook.sdk.domain.model.SyncStatusWithData
import com.rookmotion.rookconnectdemo.extension.appendConsoleLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

class SDKPlaygroundViewModel(
    private val rookPermissionsManager: RookPermissionsManager,
    private val rookSummaryManager: RookSummaryManager,
    private val rookEventManager: RookEventManager,
) : ViewModel() {

    private val _availability = MutableStateFlow("")
    val availability get() = _availability.asStateFlow()

    private val _permissions = MutableStateFlow("")
    val permissions get() = _permissions.asStateFlow()

    private val _syncHealthData = MutableStateFlow("")
    val syncHealthData get() = _syncHealthData.asStateFlow()

    private val _pendingSummaries = MutableStateFlow("")
    val pendingSummaries get() = _pendingSummaries.asStateFlow()

    private val _pendingEvents = MutableStateFlow("")
    val pendingEvents get() = _pendingEvents.asStateFlow()

    fun checkAvailability() {
        val stringBuilder = StringBuilder()

        viewModelScope.launch {
            stringBuilder.appendConsoleLine("Checking availability...")
            _availability.emit(stringBuilder.toString())

            val string = when (rookPermissionsManager.checkHealthConnectAvailability()) {
                HealthConnectAvailability.INSTALLED -> "Health Connect is installed! You can skip the next step"
                HealthConnectAvailability.NOT_INSTALLED -> "Health Connect is not installed. Please download from the Play Store"
                else -> "This device is not compatible with health connect. Please close the app"
            }

            stringBuilder.appendConsoleLine("Availability checked successfully")
            stringBuilder.appendConsoleLine(string)
            _availability.emit(stringBuilder.toString())
        }
    }

    fun checkPermissions() {
        val stringBuilder = StringBuilder()

        viewModelScope.launch {
            stringBuilder.appendConsoleLine("Checking all permissions (Sleep, Physical and Body)...")
            _permissions.emit(stringBuilder.toString())

            val result = rookPermissionsManager.checkHealthConnectPermissions()

            result.fold(
                {
                    val string = if (it) {
                        "All permissions are granted! You can skip the next 2 steps"
                    } else {
                        "There are missing permissions. Please grant them"
                    }

                    stringBuilder.appendConsoleLine("All permissions checked successfully")
                    stringBuilder.appendConsoleLine(string)
                    _permissions.emit(stringBuilder.toString())
                },
                {
                    val error = when (it) {
                        is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                        is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                        is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                        is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                        else -> "${it.message}"
                    }

                    stringBuilder.appendConsoleLine("Error checking all permissions:")
                    stringBuilder.appendConsoleLine(error)
                    _permissions.emit(stringBuilder.toString())
                }
            )
        }
    }

    fun openHealthConnect() {
        viewModelScope.launch {
            Timber.i("Opening Health Connect...")

            val result = rookPermissionsManager.openHealthConnectSettings()

            result.fold(
                {
                    Timber.i("Health Connect was opened")
                },
                {
                    val error = when (it) {
                        is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                        is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                        is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                        is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                        else -> "${it.message}"
                    }

                    Timber.e("Error opening Health Connect:")
                    Timber.e(error)
                }
            )
        }
    }

    fun syncHealthData(localDate: LocalDate) {
        val stringBuilder = StringBuilder()

        viewModelScope.launch {
            stringBuilder.appendConsoleLine("Syncing health data...")
            _syncHealthData.emit(stringBuilder.toString())

            stringBuilder.appendConsoleLine("Syncing Sleep summary: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncSleepSummary(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing Physical summary: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncPhysicalSummary(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing Body summary: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncBodySummary(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing Physical events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncPhysicalEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing BloodGlucose events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncBloodGlucoseEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing BloodPressure events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncBloodPressureEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing BodyMetrics events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncBodyMetricsEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing BodyHeartRate events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncBodyHeartRateEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing PhysicalHeartRate events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncPhysicalHeartRateEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing Hydration events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncHydrationEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing Nutrition events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncNutritionEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing BodyOxygenation events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncBodyOxygenationEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing PhysicalOxygenation events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncPhysicalOxygenationEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing Temperature events: $localDate...")
            _syncHealthData.emit(stringBuilder.toString())

            syncTemperatureEvents(localDate, stringBuilder)

            stringBuilder.appendConsoleLine("Syncing Steps events of today: ${LocalDate.now()}...")
            _syncHealthData.emit(stringBuilder.toString())

            syncStepsEvents(stringBuilder)
        }
    }


    private suspend fun syncSleepSummary(localDate: LocalDate, stringBuilder: StringBuilder) {
        rookSummaryManager.syncSleepSummary(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("Sleep summary not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("Sleep summary synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing Sleep summary:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncPhysicalSummary(
        localDate: LocalDate,
        stringBuilder: StringBuilder,
    ) {
        rookSummaryManager.syncPhysicalSummary(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("Physical summary not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("Physical summary synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing Physical summary:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncBodySummary(localDate: LocalDate, stringBuilder: StringBuilder) {
        rookSummaryManager.syncBodySummary(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("Body summary not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("Body summary synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing Body summary:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncPhysicalEvents(localDate: LocalDate, stringBuilder: StringBuilder) {
        rookEventManager.syncPhysicalEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("Physical events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("Physical events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing Physical events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncBloodGlucoseEvents(localDate: LocalDate, stringBuilder: StringBuilder) {
        rookEventManager.syncBloodGlucoseEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("BloodGlucose events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("BloodGlucose events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing BloodGlucose events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncBloodPressureEvents(
        localDate: LocalDate,
        stringBuilder: StringBuilder,
    ) {
        rookEventManager.syncBloodPressureEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("BloodPressure events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("BloodPressure events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing BloodPressure events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncBodyMetricsEvents(localDate: LocalDate, stringBuilder: StringBuilder) {
        rookEventManager.syncBodyMetricsEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("BodyMetrics events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("BodyMetrics events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing BodyMetrics events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncBodyHeartRateEvents(
        localDate: LocalDate,
        stringBuilder: StringBuilder,
    ) {
        rookEventManager.syncBodyHeartRateEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("BodyHearRate events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("BodyHeartRate events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing BodyHeartRate events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncPhysicalHeartRateEvents(
        localDate: LocalDate,
        stringBuilder: StringBuilder,
    ) {
        rookEventManager.syncPhysicalHeartRateEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("PhysicalHeartRate events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("PhysicalHeartRate events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing PhysicalHeartRate events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncHydrationEvents(localDate: LocalDate, stringBuilder: StringBuilder) {
        rookEventManager.syncHydrationEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("Hydration events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("Hydration events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing Hydration events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncNutritionEvents(localDate: LocalDate, stringBuilder: StringBuilder) {
        rookEventManager.syncNutritionEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("Nutrition events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("Nutrition events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing Nutrition events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncBodyOxygenationEvents(
        localDate: LocalDate,
        stringBuilder: StringBuilder,
    ) {
        rookEventManager.syncBodyOxygenationEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("BodyOxygenation events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("BodyOxygenation events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing BodyOxygenation events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncPhysicalOxygenationEvents(
        localDate: LocalDate,
        stringBuilder: StringBuilder,
    ) {
        rookEventManager.syncPhysicalOxygenationEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("PhysicalOxygenation events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("PhysicalOxygenation events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing PhysicalOxygenation events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncTemperatureEvents(localDate: LocalDate, stringBuilder: StringBuilder) {
        rookEventManager.syncTemperatureEvents(localDate).fold(
            {
                when (it) {
                    SyncStatus.RECORDS_NOT_FOUND -> {
                        stringBuilder.appendConsoleLine("temperature events not found")
                    }

                    SyncStatus.SYNCED -> {
                        stringBuilder.appendConsoleLine("Temperature events synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing Temperature events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    private suspend fun syncStepsEvents(stringBuilder: StringBuilder) {
        rookEventManager.syncTodayHealthConnectStepsCount().fold(
            {
                when (it) {
                    SyncStatusWithData.RecordsNotFound -> {
                        stringBuilder.appendConsoleLine("Steps events not found")
                    }

                    is SyncStatusWithData.Synced -> {
                        stringBuilder.appendConsoleLine("${it.data} steps synced successfully")
                    }
                }

                _syncHealthData.emit(stringBuilder.toString())
            },
            {
                val error = when (it) {
                    is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                    is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                    is HealthConnectNotInstalledException -> "HealthConnectNotInstalledException: ${it.message}"
                    is DeviceNotSupportedException -> "DeviceNotSupportedException: ${it.message}"
                    is MissingHealthConnectPermissionsException -> "MissingPermissionsException: ${it.message}"
                    is RequestQuotaExceededException -> "RequestQuotaExceededException: ${it.message}"
                    is TimeoutException -> "TimeoutException: ${it.message}"
                    is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                    is SDKNotAuthorizedException -> "SDKNotAuthorizedException: ${it.message}"
                    else -> "${it.message}"
                }

                stringBuilder.appendConsoleLine("Error syncing Steps events:")
                stringBuilder.appendConsoleLine(error)
                _syncHealthData.emit(stringBuilder.toString())
            }
        )
    }

    fun syncPendingSummaries() {
        val stringBuilder = StringBuilder()

        viewModelScope.launch {
            stringBuilder.appendConsoleLine("Syncing pending summaries...")
            _pendingSummaries.emit(stringBuilder.toString())

            val result = rookSummaryManager.syncPendingSummaries()

            result.fold(
                {
                    stringBuilder.appendConsoleLine("Pending summaries synced successfully")
                    _pendingSummaries.emit(stringBuilder.toString())
                },
                {
                    val error = when (it) {
                        is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                        is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                        is TimeoutException -> "TimeoutException: ${it.message}"
                        is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                        else -> "${it.message}"
                    }

                    stringBuilder.appendConsoleLine("Error syncing pending summaries:")
                    stringBuilder.appendConsoleLine(error)
                    _pendingSummaries.emit(stringBuilder.toString())
                }
            )
        }
    }

    fun syncPendingEvents() {
        val stringBuilder = StringBuilder()

        viewModelScope.launch {
            stringBuilder.appendConsoleLine("Syncing pending events...")
            _pendingEvents.emit(stringBuilder.toString())

            val result = rookEventManager.syncPendingEvents()

            result.fold(
                {
                    stringBuilder.appendConsoleLine("Pending events synced successfully")
                    _pendingEvents.emit(stringBuilder.toString())
                },
                {
                    val error = when (it) {
                        is SDKNotInitializedException -> "SDKNotInitializedException: ${it.message}"
                        is UserNotInitializedException -> "UserNotInitializedException: ${it.message}"
                        is TimeoutException -> "TimeoutException: ${it.message}"
                        is HttpRequestException -> "HttpRequestException: code: ${it.code} message: ${it.message}"
                        else -> "${it.message}"
                    }

                    stringBuilder.appendConsoleLine("Error syncing pending events:")
                    stringBuilder.appendConsoleLine(error)
                    _pendingEvents.emit(stringBuilder.toString())
                }
            )
        }
    }
}