package com.rookmotion.rookconnectdemo.features.connectionspage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rookmotion.rook.sdk.RookDataSources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConnectionsPageViewModel(
    private val rookDataSources: RookDataSources,
) : ViewModel() {

    private val _state = MutableStateFlow(ConnectionsPageState())
    val state get() = _state.asStateFlow()

    fun getAvailableDataSources() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }

            rookDataSources.getAvailableDataSources(HOME_PAGE_URL).fold(
                { dataSources ->
                    _state.update { it.copy(loading = false, dataSources = dataSources) }
                },
                { throwable ->
                    _state.update { it.copy(loading = false, error = "${throwable.message}") }
                },
            )
        }
    }

    fun openConnectionUrl(connectionUrl: String) {
        viewModelScope.launch {
            _state.update { it.copy(webViewUrl = connectionUrl) }
        }
    }

    fun closeConnectionUrl() {
        viewModelScope.launch {
            _state.update { it.copy(webViewUrl = null) }
        }
    }

    fun isHomePageUrl(url: String): Boolean {
        return url.startsWith(HOME_PAGE_URL)
    }
}

private const val HOME_PAGE_URL = "https://www.google.com"
