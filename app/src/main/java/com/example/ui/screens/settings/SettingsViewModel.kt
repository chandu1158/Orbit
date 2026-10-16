package com.example.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.BlockedNumberEntity
import com.example.data.model.SimAccountInfo
import com.example.data.repository.BlockedNumbersRepository
import com.example.data.repository.TelecomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticFeedbackEnabled: Boolean = true,
    val isDefaultDialer: Boolean = false,
    val availableSims: List<SimAccountInfo> = emptyList(),
    val blockedNumbers: List<BlockedNumberEntity> = emptyList()
)

class SettingsViewModel(
    private val telecomRepo: TelecomRepository,
    private val blockedRepo: BlockedNumbersRepository
) : ViewModel() {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val _hapticEnabled = MutableStateFlow(true)
    private val _isDefaultDialer = MutableStateFlow(telecomRepo.isDefaultDialer())
    private val _sims = MutableStateFlow(telecomRepo.getAvailableSimAccounts())

    val uiState: StateFlow<SettingsUiState> = combine(
        _themeMode,
        _hapticEnabled,
        _isDefaultDialer,
        _sims,
        blockedRepo.blockedNumbers
    ) { theme, haptic, isDefault, sims, blocked ->
        SettingsUiState(
            themeMode = theme,
            hapticFeedbackEnabled = haptic,
            isDefaultDialer = isDefault,
            availableSims = sims,
            blockedNumbers = blocked
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun refreshRoleStatus() {
        _isDefaultDialer.value = telecomRepo.isDefaultDialer()
        _sims.value = telecomRepo.getAvailableSimAccounts()
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabled.value = enabled
    }

    fun unblockNumber(phoneNumber: String) {
        viewModelScope.launch {
            blockedRepo.unblockNumber(phoneNumber)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return SettingsViewModel(
                TelecomRepository(context),
                BlockedNumbersRepository(db)
            ) as T
        }
    }
}
