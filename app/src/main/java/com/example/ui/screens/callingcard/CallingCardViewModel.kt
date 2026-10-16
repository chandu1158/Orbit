package com.example.ui.screens.callingcard

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CallingCardEntity
import com.example.data.repository.CallingCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CallingCardUiState(
    val cardId: String = CallingCardRepository.MY_CARD_ID,
    val displayName: String = "",
    val subtitle: String = "",
    val photoPath: String? = null,
    val gradientIndex: Int = 0,
    val fontStyleIndex: Int = 0,
    val textColorHex: String = "#FFFFFF",
    val textPlacement: String = "TOP",
    val photoScale: Float = 1.0f,
    val photoOffsetY: Float = 0f,
    val framingStyle: String = "FULL",
    val isRealisticPreviewOpen: Boolean = false,
    val isSaved: Boolean = false,
    val isExistingCard: Boolean = false,
    val isSaving: Boolean = false,
    val statusMessage: String? = null
)

class CallingCardViewModel(
    private val repository: CallingCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallingCardUiState())
    val uiState: StateFlow<CallingCardUiState> = _uiState.asStateFlow()

    fun loadCard(cardId: String, fallbackName: String, fallbackSubtitle: String = "") {
        val safeKey = CallingCardRepository.normalizeKey(cardId)
        viewModelScope.launch {
            val existing = repository.getCallingCard(safeKey)
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        cardId = existing.cardId,
                        displayName = existing.displayName,
                        subtitle = existing.subtitle,
                        photoPath = existing.photoPath,
                        gradientIndex = existing.gradientIndex,
                        fontStyleIndex = existing.fontStyleIndex,
                        textColorHex = existing.textColorHex,
                        textPlacement = existing.textPlacement,
                        photoScale = existing.photoScale,
                        photoOffsetY = existing.photoOffsetY,
                        framingStyle = existing.framingStyle,
                        isExistingCard = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        cardId = safeKey,
                        displayName = fallbackName.ifBlank { "Voxa User" },
                        subtitle = fallbackSubtitle,
                        photoPath = null,
                        gradientIndex = 0,
                        fontStyleIndex = 0,
                        textColorHex = "#FFFFFF",
                        textPlacement = "TOP",
                        photoScale = 1.0f,
                        photoOffsetY = 0f,
                        framingStyle = "FULL",
                        isExistingCard = false
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(displayName = name) }
    }

    fun updateSubtitle(subtitle: String) {
        _uiState.update { it.copy(subtitle = subtitle) }
    }

    fun setPhotoFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val savedPath = repository.savePhotoFromUri(context, _uiState.value.cardId, uri)
            _uiState.update { it.copy(photoPath = savedPath) }
        }
    }

    fun setPhotoFromBitmap(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            val savedPath = repository.savePhotoFromBitmap(context, _uiState.value.cardId, bitmap)
            _uiState.update { it.copy(photoPath = savedPath) }
        }
    }

    fun removePhoto(context: Context) {
        _uiState.value.photoPath?.let { path ->
            viewModelScope.launch {
                try {
                    java.io.File(path).delete()
                } catch (e: Exception) {
                    // Ignore deletion error
                }
                _uiState.update { it.copy(photoPath = null) }
            }
        }
    }

    fun setGradient(index: Int) {
        _uiState.update { it.copy(gradientIndex = index) }
    }

    fun setFontStyle(index: Int) {
        _uiState.update { it.copy(fontStyleIndex = index) }
    }

    fun setTextColor(hex: String) {
        _uiState.update { it.copy(textColorHex = hex) }
    }

    fun setTextPlacement(placement: String) {
        _uiState.update { it.copy(textPlacement = placement) }
    }

    fun setPhotoScale(scale: Float) {
        _uiState.update { it.copy(photoScale = scale.coerceIn(0.8f, 2.5f)) }
    }

    fun setPhotoOffsetY(offset: Float) {
        _uiState.update { it.copy(photoOffsetY = offset.coerceIn(-150f, 150f)) }
    }

    fun setFramingStyle(style: String) {
        _uiState.update { it.copy(framingStyle = style) }
    }

    fun toggleRealisticPreview(show: Boolean) {
        _uiState.update { it.copy(isRealisticPreviewOpen = show) }
    }

    fun resetToDefaults() {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                gradientIndex = 0,
                fontStyleIndex = 0,
                textColorHex = "#FFFFFF",
                textPlacement = "TOP",
                photoScale = 1.0f,
                photoOffsetY = 0f,
                framingStyle = "FULL"
            )
        }
    }

    fun saveCard(onSuccess: () -> Unit) {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val card = CallingCardEntity(
                cardId = s.cardId,
                displayName = s.displayName.ifBlank { "Voxa User" },
                subtitle = s.subtitle,
                photoPath = s.photoPath,
                gradientIndex = s.gradientIndex,
                fontStyleIndex = s.fontStyleIndex,
                textColorHex = s.textColorHex,
                textPlacement = s.textPlacement,
                photoScale = s.photoScale,
                photoOffsetY = s.photoOffsetY,
                framingStyle = s.framingStyle
            )
            repository.saveCallingCard(card)
            _uiState.update { it.copy(isSaving = false, isSaved = true, isExistingCard = true) }
            onSuccess()
        }
    }

    fun deleteCard(context: Context, onDeleted: () -> Unit) {
        val s = _uiState.value
        viewModelScope.launch {
            repository.deleteCallingCard(context, s.cardId)
            onDeleted()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            return CallingCardViewModel(CallingCardRepository(db)) as T
        }
    }
}
