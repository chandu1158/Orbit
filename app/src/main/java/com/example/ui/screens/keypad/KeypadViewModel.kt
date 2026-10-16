package com.example.ui.screens.keypad

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactItem
import com.example.data.model.SimAccountInfo
import com.example.data.repository.ContactsRepository
import com.example.data.repository.TelecomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class KeypadUiState(
    val enteredNumber: String = "",
    val suggestedContacts: List<ContactItem> = emptyList(),
    val availableSims: List<SimAccountInfo> = emptyList(),
    val selectedSim: SimAccountInfo? = null,
    val showSimPicker: Boolean = false
)

class KeypadViewModel(
    private val telecomRepository: TelecomRepository,
    private val contactsRepository: ContactsRepository
) : ViewModel() {
    private val _enteredNumber = MutableStateFlow("")
    private val _allContacts = MutableStateFlow<List<ContactItem>>(emptyList())
    private val _availableSims = MutableStateFlow<List<SimAccountInfo>>(emptyList())
    private val _selectedSim = MutableStateFlow<SimAccountInfo?>(null)
    private val _showSimPicker = MutableStateFlow(false)

    val uiState: StateFlow<KeypadUiState> = combine(
        _enteredNumber,
        _allContacts,
        _availableSims,
        _selectedSim,
        _showSimPicker
    ) { number, contacts, sims, selectedSim, showPicker ->
        val suggestions = if (number.isNotBlank()) {
            T9Engine.searchContacts(number, contacts)
        } else {
            emptyList()
        }

        KeypadUiState(
            enteredNumber = number,
            suggestedContacts = suggestions,
            availableSims = sims,
            selectedSim = selectedSim,
            showSimPicker = showPicker
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), KeypadUiState())

    init {
        refreshSims()
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            _allContacts.value = contactsRepository.getContacts()
        }
    }

    fun refreshSims() {
        val sims = telecomRepository.getAvailableSimAccounts()
        _availableSims.value = sims
        if (_selectedSim.value == null && sims.isNotEmpty()) {
            _selectedSim.value = sims.first()
        }
    }

    fun appendDigit(char: Char) {
        _enteredNumber.value += char
    }

    fun appendPlus() {
        _enteredNumber.value += "+"
    }

    fun backspace() {
        if (_enteredNumber.value.isNotEmpty()) {
            _enteredNumber.value = _enteredNumber.value.dropLast(1)
        }
    }

    fun clearNumber() {
        _enteredNumber.value = ""
    }

    fun setNumber(number: String) {
        _enteredNumber.value = number
    }

    fun onCallClicked(onRequireSimSelection: (List<SimAccountInfo>, String) -> Unit) {
        val number = _enteredNumber.value.trim()
        if (number.isEmpty()) return

        val sims = _availableSims.value
        if (sims.size > 1 && _selectedSim.value == null) {
            onRequireSimSelection(sims, number)
        } else {
            telecomRepository.placeCall(number, _selectedSim.value?.phoneAccountHandle)
        }
    }

    fun callWithSim(sim: SimAccountInfo, number: String) {
        telecomRepository.placeCall(number, sim.phoneAccountHandle)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return KeypadViewModel(
                TelecomRepository(context),
                ContactsRepository(context)
            ) as T
        }
    }
}
