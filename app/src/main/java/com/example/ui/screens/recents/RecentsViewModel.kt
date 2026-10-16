package com.example.ui.screens.recents

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import com.example.data.repository.CallLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RecentsFilter {
    ALL,
    MISSED
}

data class RecentsUiState(
    val items: List<CallLogItem> = emptyList(),
    val filter: RecentsFilter = RecentsFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val hasPermission: Boolean = true
)

class RecentsViewModel(private val repository: CallLogRepository) : ViewModel() {
    private val _rawItems = MutableStateFlow<List<CallLogItem>>(emptyList())
    private val _filter = MutableStateFlow(RecentsFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _hasPermission = MutableStateFlow(repository.hasCallLogPermission())

    val uiState: StateFlow<RecentsUiState> = combine(
        _rawItems,
        _filter,
        _searchQuery,
        _isLoading,
        _hasPermission
    ) { items, filter, query, loading, hasPerm ->
        val filteredByType = if (filter == RecentsFilter.MISSED) {
            items.filter { it.type == CallType.MISSED || it.type == CallType.REJECTED }
        } else {
            items
        }

        val filteredByQuery = if (query.isBlank()) {
            filteredByType
        } else {
            val q = query.trim().lowercase()
            filteredByType.filter {
                (it.cachedName?.lowercase()?.contains(q) == true) ||
                        it.number.contains(q)
            }
        }

        RecentsUiState(
            items = filteredByQuery,
            filter = filter,
            searchQuery = query,
            isLoading = loading,
            hasPermission = hasPerm
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecentsUiState())

    init {
        loadRecents()
    }

    fun loadRecents() {
        val perm = repository.hasCallLogPermission()
        _hasPermission.value = perm
        if (!perm) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _rawItems.value = repository.getCallLogs()
            } catch (e: Exception) {
                _rawItems.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFilter(filter: RecentsFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteCallLog(item: CallLogItem) {
        viewModelScope.launch {
            val success = repository.deleteCallLog(item.id)
            if (success) {
                _rawItems.update { current -> current.filter { it.id != item.id } }
            }
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            val success = repository.clearAllCallLogs()
            if (success) {
                _rawItems.value = emptyList()
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecentsViewModel(CallLogRepository(context)) as T
        }
    }
}
