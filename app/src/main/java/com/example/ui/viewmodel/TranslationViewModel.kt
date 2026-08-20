package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechRecognitionHelper
import com.example.audio.TextToSpeechHelper
import com.example.data.db.AppDatabase
import com.example.data.repository.TranslationRepository
import com.example.model.ActiveScreen
import com.example.model.Language
import com.example.model.RecentTranslation
import com.example.model.TranscriptSegment
import com.example.model.TranslationSession
import com.example.translation.GoogleTranslationEngine
import com.example.translation.LanguageModelDownloadWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class TranslationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TranslationRepository
    val recentTranslations: StateFlow<List<RecentTranslation>>
    val favoriteTranslations: StateFlow<List<RecentTranslation>>
    val sessions: StateFlow<List<TranslationSession>>

    private val _activeScreen = MutableStateFlow(ActiveScreen.HOME)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    // Navigation back stack for system Android back gesture & back button
    private val screenBackStack = mutableListOf<ActiveScreen>()

    // Home translation state
    val homeInputText = MutableStateFlow("")
    val homeTranslatedText = MutableStateFlow("")
    val isTranslating = MutableStateFlow(false)

    // Language selection
    private val _sourceLanguage = MutableStateFlow(Language.findByCode("ja"))
    val sourceLanguage: StateFlow<Language> = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow(Language.findByCode("en"))
    val targetLanguage: StateFlow<Language> = _targetLanguage.asStateFlow()

    // Voice & Speech Recognition
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Live Translate Session State
    val liveSegments = MutableStateFlow<List<TranscriptSegment>>(emptyList())
    val liveActiveSpeaker = MutableStateFlow(1) // 1: User/Source, 2: Partner/Target
    val livePartialText = MutableStateFlow("")
    val livePartialTranslated = MutableStateFlow("")
    val isLiveSessionRunning = MutableStateFlow(false)
    val isLiveSessionPaused = MutableStateFlow(false)
    val liveTimerSeconds = MutableStateFlow(14)
    val recordAudioChecked = MutableStateFlow(true)
    private var liveTimerJob: Job? = null

    // Conversation State (Two-Way)
    val speaker1Text = MutableStateFlow("")
    val speaker1Translated = MutableStateFlow("")
    val speaker2Text = MutableStateFlow("")
    val speaker2Translated = MutableStateFlow("")
    val activeConversationSpeaker = MutableStateFlow(1)

    // UI Dialogs
    val showLanguageSelector = MutableStateFlow(false)
    val isSelectingSource = MutableStateFlow(true)
    val showSettingsDialog = MutableStateFlow(false)
    val showNotificationsDialog = MutableStateFlow(false)

    // Settings
    val speechSpeed = MutableStateFlow(1.0f)
    val speechPitch = MutableStateFlow(1.0f)

    private var speechHelper: SpeechRecognitionHelper? = null
    private var ttsHelper: TextToSpeechHelper? = null
    private var translateDebounceJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TranslationRepository(db.translationDao())

        recentTranslations = repository.recentTranslations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        favoriteTranslations = repository.favoriteTranslations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        sessions = repository.sessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        ttsHelper = TextToSpeechHelper(application)

        speechHelper = SpeechRecognitionHelper(
            context = application,
            onPartialSpeechResult = { partial ->
                handlePartialSpeech(partial)
            },
            onFinalSpeechResult = { final ->
                handleFinalSpeech(final)
            },
            onRmsLevelChanged = { rms ->
                _rmsLevel.value = rms
            },
            onListeningStateChanged = { listening ->
                _isListening.value = listening
            },
            onAudioBufferReceived = { buffer ->
                if (buffer != null && buffer.isNotEmpty()) {
                    warmupModels()
                }
            }
        )

        // Preload language models
        enqueueLanguageDownloads(_sourceLanguage.value, _targetLanguage.value)
    }

    fun setActiveScreen(screen: ActiveScreen) {
        if (_activeScreen.value != screen) {
            screenBackStack.add(_activeScreen.value)
            _activeScreen.value = screen
        }
    }

    /**
     * Handles Android back gestures or UI back buttons.
     * Returns true if back navigation was handled internally, false if app should exit.
     */
    fun navigateBack(): Boolean {
        // 1. Close dialogs or sheets first if open
        if (showLanguageSelector.value) {
            showLanguageSelector.value = false
            return true
        }
        if (showSettingsDialog.value) {
            showSettingsDialog.value = false
            return true
        }

        // 2. Stop ongoing speech recognition if active in sub-screens
        if (isListening.value) {
            speechHelper?.stopListening()
        }

        // 3. Navigate back through backstack
        while (screenBackStack.isNotEmpty()) {
            val previousScreen = screenBackStack.removeAt(screenBackStack.size - 1)
            if (previousScreen != _activeScreen.value) {
                _activeScreen.value = previousScreen
                return true
            }
        }

        // 4. If on a non-home tab and stack is empty, return to HOME
        if (_activeScreen.value != ActiveScreen.HOME) {
            _activeScreen.value = ActiveScreen.HOME
            return true
        }

        // Already at root HOME with no overlays
        return false
    }

    fun setSourceLanguage(language: Language) {
        _sourceLanguage.value = language
        enqueueLanguageDownloads(language, _targetLanguage.value)
        if (homeInputText.value.isNotBlank()) {
            triggerTranslation(homeInputText.value)
        }
    }

    fun setTargetLanguage(language: Language) {
        _targetLanguage.value = language
        enqueueLanguageDownloads(_sourceLanguage.value, language)
        if (homeInputText.value.isNotBlank()) {
            triggerTranslation(homeInputText.value)
        }
    }

    fun swapLanguages() {
        val temp = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = temp

        val tempText = homeInputText.value
        homeInputText.value = homeTranslatedText.value
        homeTranslatedText.value = tempText

        enqueueLanguageDownloads(_sourceLanguage.value, _targetLanguage.value)
    }

    fun onHomeInputChanged(text: String) {
        homeInputText.value = text
        if (text.isBlank()) {
            homeTranslatedText.value = ""
            return
        }
        triggerTranslation(text)
    }

    private fun triggerTranslation(text: String) {
        translateDebounceJob?.cancel()
        translateDebounceJob = viewModelScope.launch {
            delay(300)
            isTranslating.value = true
            try {
                val result = GoogleTranslationEngine.translate(
                    text = text,
                    sourceLangCode = _sourceLanguage.value.code,
                    targetLangCode = _targetLanguage.value.code
                )
                homeTranslatedText.value = result
                
                // Add to recent if non-empty
                if (text.trim().length > 1) {
                    repository.addRecentTranslation(
                        source = text.trim(),
                        translated = result,
                        sourceLang = _sourceLanguage.value.code,
                        targetLang = _targetLanguage.value.code
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Ignore debounce cancellations cleanly without error logs
            } catch (e: Exception) {
                Log.e("TranslationVM", "Translation error", e)
            } finally {
                isTranslating.value = false
            }
        }
    }

    fun toggleMicListening() {
        if (_isListening.value) {
            speechHelper?.stopListening()
        } else {
            val locale = Locale.forLanguageTag(_sourceLanguage.value.code)
            speechHelper?.startListening(locale)
        }
    }

    fun startLiveSession() {
        isLiveSessionRunning.value = true
        isLiveSessionPaused.value = false
        if (liveSegments.value.isEmpty() && _sourceLanguage.value.code == "ja") {
            populateDemoSegments()
        }
        startLiveTimer()
        val locale = Locale.forLanguageTag(_sourceLanguage.value.code)
        speechHelper?.startListening(locale)
    }

    fun pauseLiveSession() {
        isLiveSessionPaused.value = true
        speechHelper?.stopListening()
    }

    fun resumeLiveSession() {
        isLiveSessionPaused.value = false
        val locale = Locale.forLanguageTag(_sourceLanguage.value.code)
        speechHelper?.startListening(locale)
    }

    fun stopLiveSession() {
        isLiveSessionRunning.value = false
        isLiveSessionPaused.value = false
        liveTimerJob?.cancel()
        liveTimerJob = null
        speechHelper?.stopListening()
    }

    fun resetLiveTranscript() {
        liveSegments.value = emptyList()
        livePartialText.value = ""
        livePartialTranslated.value = ""
        liveTimerSeconds.value = 0
    }

    fun toggleRecordAudio() {
        recordAudioChecked.value = !recordAudioChecked.value
    }

    private fun startLiveTimer() {
        liveTimerJob?.cancel()
        liveTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (isLiveSessionRunning.value && !isLiveSessionPaused.value) {
                    liveTimerSeconds.value += 1
                }
            }
        }
    }

    fun populateDemoSegments() {
        liveSegments.value = listOf(
            TranscriptSegment(
                sessionId = 0,
                speaker = "日本語",
                sourceText = "先生、カバ。",
                translatedText = "Teacher, hippo.",
                isSourceSpeaker = true
            ),
            TranscriptSegment(
                sessionId = 0,
                speaker = "日本語",
                sourceText = "Shinji、virmal。",
                translatedText = "Shinji, Virmal.",
                isSourceSpeaker = true
            ),
            TranscriptSegment(
                sessionId = 0,
                speaker = "日本語",
                sourceText = "うん。",
                translatedText = "Yeah.",
                isSourceSpeaker = true
            )
        )
    }

    private fun handlePartialSpeech(partial: String) {
        when (_activeScreen.value) {
            ActiveScreen.HOME -> {
                homeInputText.value = partial
                viewModelScope.launch {
                    val res = GoogleTranslationEngine.translate(
                        partial,
                        _sourceLanguage.value.code,
                        _targetLanguage.value.code
                    )
                    homeTranslatedText.value = res
                }
            }
            ActiveScreen.LIVE_TRANSLATE -> {
                livePartialText.value = partial
                viewModelScope.launch {
                    val res = GoogleTranslationEngine.translate(
                        partial,
                        _sourceLanguage.value.code,
                        _targetLanguage.value.code
                    )
                    livePartialTranslated.value = res
                }
            }
            ActiveScreen.CONVERSATION -> {
                if (activeConversationSpeaker.value == 1) {
                    speaker1Text.value = partial
                } else {
                    speaker2Text.value = partial
                }
            }
            else -> {}
        }
    }

    private fun handleFinalSpeech(final: String) {
        when (_activeScreen.value) {
            ActiveScreen.HOME -> {
                homeInputText.value = final
                viewModelScope.launch {
                    val res = GoogleTranslationEngine.translate(
                        final,
                        _sourceLanguage.value.code,
                        _targetLanguage.value.code
                    )
                    homeTranslatedText.value = res
                    repository.addRecentTranslation(
                        source = final,
                        translated = res,
                        sourceLang = _sourceLanguage.value.code,
                        targetLang = _targetLanguage.value.code
                    )
                }
            }
            ActiveScreen.LIVE_TRANSLATE -> {
                livePartialText.value = ""
                livePartialTranslated.value = ""
                viewModelScope.launch {
                    val res = GoogleTranslationEngine.translate(
                        final,
                        _sourceLanguage.value.code,
                        _targetLanguage.value.code
                    )
                    val newSegment = TranscriptSegment(
                        sessionId = 0,
                        speaker = "Speaker ${_sourceLanguage.value.code.uppercase()}",
                        sourceText = final,
                        translatedText = res,
                        isSourceSpeaker = true
                    )
                    liveSegments.value = liveSegments.value + newSegment
                    repository.addRecentTranslation(
                        source = final,
                        translated = res,
                        sourceLang = _sourceLanguage.value.code,
                        targetLang = _targetLanguage.value.code
                    )
                }
            }
            ActiveScreen.CONVERSATION -> {
                val isSpeaker1 = activeConversationSpeaker.value == 1
                val srcCode = if (isSpeaker1) _sourceLanguage.value.code else _targetLanguage.value.code
                val tgtCode = if (isSpeaker1) _targetLanguage.value.code else _sourceLanguage.value.code

                viewModelScope.launch {
                    val res = GoogleTranslationEngine.translate(final, srcCode, tgtCode)
                    if (isSpeaker1) {
                        speaker1Text.value = final
                        speaker1Translated.value = res
                        speakText(res, tgtCode)
                    } else {
                        speaker2Text.value = final
                        speaker2Translated.value = res
                        speakText(res, tgtCode)
                    }
                    repository.addRecentTranslation(final, res, srcCode, tgtCode)
                }
            }
            else -> {}
        }
    }

    fun speakText(text: String, langCode: String) {
        if (text.isBlank()) return
        ttsHelper?.speechRate = speechSpeed.value
        ttsHelper?.pitch = speechPitch.value
        ttsHelper?.speak(
            text = text,
            langCode = langCode,
            onStart = { _isSpeaking.value = true },
            onDone = { _isSpeaking.value = false }
        )
    }

    fun toggleFavorite(item: RecentTranslation) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun deleteRecentTranslation(item: RecentTranslation) {
        viewModelScope.launch {
            repository.deleteRecent(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private fun enqueueLanguageDownloads(source: Language, target: Language) {
        try {
            LanguageModelDownloadWorker.enqueueDownload(getApplication(), source.code)
            LanguageModelDownloadWorker.enqueueDownload(getApplication(), target.code)
        } catch (e: Exception) {
            Log.e("TranslationVM", "Error enqueueing downloads", e)
        }
    }

    private fun warmupModels() {
        viewModelScope.launch {
            try {
                GoogleTranslationEngine.translate(
                    "warmup",
                    _sourceLanguage.value.code,
                    _targetLanguage.value.code
                )
            } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper?.destroy()
        ttsHelper?.shutdown()
        GoogleTranslationEngine.close()
    }
}
