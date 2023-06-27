package com.rookmotion.rookconnectdemo.features.healthconnect.permissions

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rookmotion.rook.health_connect.RookHealthConnectManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HCPermissionsViewModel(private val rookHealthConnectManager: RookHealthConnectManager) :
    ViewModel() {

    private val _permissionsState = MutableStateFlow<PermissionsState>(PermissionsState.None)
    val permissionsState get() = _permissionsState.asStateFlow()

    fun openHealthConnectSettings() {
        rookHealthConnectManager.openHealthConnectSettings()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            _permissionsState.emit(PermissionsState.Loading)

            val result = rookHealthConnectManager.hasAllPermissions()

            _permissionsState.emit(PermissionsState.Success(result))
        }
    }

    fun requestPermissions(activity: Activity) {
        rookHealthConnectManager.requestAllPermissions(activity)
    }
}
