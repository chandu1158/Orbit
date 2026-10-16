package com.example.ui.screens.contacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactItem
import com.example.data.repository.ContactsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ContactsUiState(
    val contacts: List<ContactItem> = emptyList(),
    val groupedContacts: Map<String, List<ContactItem>> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val hasPermission: Boolean = true
)

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {
    private val _rawContacts = MutableStateFlow<List<ContactItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _hasPermission = MutableStateFlow(repository.hasContactsPermission())

    val uiState: StateFlow<ContactsUiState> = combine(
        _rawContacts,
        _searchQuery,
        _isLoading,
        _hasPermission
    ) { contacts, query, loading, hasPerm ->
        val filtered = if (query.isBlank()) {
            contacts
        } else {
            val q = query.trim().lowercase()
            contacts.filter { contact ->
                contact.displayName.lowercase().contains(q) ||
                        contact.numbers.any { it.number.contains(q) }
            }
        }

        val grouped = filtered.groupBy { contact ->
            val first = contact.displayName.firstOrNull()?.uppercase() ?: "#"
            if (first.length == 1 && first[0] in 'A'..'Z') first else "#"
        }.toSortedMap { a, b ->
            if (a == "#") 1 else if (b == "#") -1 else a.compareTo(b)
        }

        ContactsUiState(
            contacts = filtered,
            groupedContacts = grouped,
            searchQuery = query,
            isLoading = loading,
            hasPermission = hasPerm
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContactsUiState())

    init {
        loadContacts()
    }

    fun loadContacts() {
        val perm = repository.hasContactsPermission()
        _hasPermission.value = perm
        if (!perm) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _rawContacts.value = repository.getContacts()
            } catch (e: Exception) {
                _rawContacts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContactsViewModel(ContactsRepository(context)) as T
        }
    }
}
