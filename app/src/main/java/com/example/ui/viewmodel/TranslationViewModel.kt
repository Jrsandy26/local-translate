package com.example.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechRecognitionHelper
import com.example.audio.TextToSpeechHelper
import com.example.data.db.AppDatabase
import com.example.data.repository.TranslationRepository
import com.example.engine.OfflineTranslationEngine
import com.example.export.DocumentExportManager
import com.example.model.ActiveScreen
import com.example.model.ExportFormat
import com.example.model.Language
import com.example.model.SessionWithSegments
import com.example.model.TranscriptSegment
import com.example.model.TranslationSession
import com.example.model.ViewDisplayMode
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
    private val ttsHelper: TextToSpeechHelper = TextToSpeechHelper(application)
    private var speechHelper: SpeechRecognitionHelper? = null

    val allSessions: StateFlow<List<SessionWithSegments>>

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _currentSession = MutableStateFlow<SessionWithSegments?>(null)
    val currentSession: StateFlow<SessionWithSegments?> = _currentSession.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewDisplayMode.BILINGUAL)
    val viewMode: StateFlow<ViewDisplayMode> = _viewMode.asStateFlow()

    private val _activeScreen = MutableStateFlow(ActiveScreen.TRANSLATE_HOME)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    // Home Screen direct text translation
    val homeInputText = MutableStateFlow("")
    val homeTranslatedText = MutableStateFlow("")

    // Live speech / stream states
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    val partialTranscriptText = MutableStateFlow("")
    val partialTranslationText = MutableStateFlow("")
    val rmsLevel = MutableStateFlow(0f)

    // Audio Playback / scrubber state
    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private val _currentPlaybackSec = MutableStateFlow(0)
    val currentPlaybackSec: StateFlow<Int> = _currentPlaybackSec.asStateFlow()

    private val _totalDurationSec = MutableStateFlow(2118) // Default 35:18
    val totalDurationSec: StateFlow<Int> = _totalDurationSec.asStateFlow()

    private val _activeHighlightIndex = MutableStateFlow<Int?>(null)
    val activeHighlightIndex: StateFlow<Int?> = _activeHighlightIndex.asStateFlow()

    // Active Languages
    private val _sourceLanguage = MutableStateFlow(Language.findByCode("en"))
    val sourceLanguage: StateFlow<Language> = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow(Language.findByCode("ja"))
    val targetLanguage: StateFlow<Language> = _targetLanguage.asStateFlow()

    // Face to Face Mode States
    private val _faceToFaceSpeaker1Text = MutableStateFlow("")
    val faceToFaceSpeaker1Text: StateFlow<String> = _faceToFaceSpeaker1Text.asStateFlow()
    private val _faceToFaceSpeaker1Trans = MutableStateFlow("")
    val faceToFaceSpeaker1Trans: StateFlow<String> = _faceToFaceSpeaker1Trans.asStateFlow()

    private val _faceToFaceSpeaker2Text = MutableStateFlow("")
    val faceToFaceSpeaker2Text: StateFlow<String> = _faceToFaceSpeaker2Text.asStateFlow()
    private val _faceToFaceSpeaker2Trans = MutableStateFlow("")
    val faceToFaceSpeaker2Trans: StateFlow<String> = _faceToFaceSpeaker2Trans.asStateFlow()

    // Dialog flags
    val showExportDialog = MutableStateFlow(false)
    val showRenameDialog = MutableStateFlow(false)
    val showQuickSpeechDialog = MutableStateFlow(false)
    val showLanguageSheet = MutableStateFlow(false)
    val isSelectingSourceLanguage = MutableStateFlow(true)
    val recordAudioEnabled = MutableStateFlow(true)

    private var playbackJob: Job? = null
    private var liveTimerJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TranslationRepository(db.translationDao())

        allSessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }

        // Initialize Speech Helper
        speechHelper = SpeechRecognitionHelper(
            context = application,
            onPartialResult = { partial ->
                partialTranscriptText.value = partial
                val trans = OfflineTranslationEngine.translate(
                    partial,
                    _sourceLanguage.value.code,
                    _targetLanguage.value.code
                )
                partialTranslationText.value = trans
            },
            onFinalResult = { final ->
                processLiveSpeechResult(final, "Speaker 1")
            },
            onRmsChanged = { rmsdB ->
                rmsLevel.value = rmsdB
            },
            onListeningStateChanged = { listening ->
                _isListening.value = listening
            }
        )

        // Observe allSessions and select initial session (e.g. Session 14)
        viewModelScope.launch {
            allSessions.collect { sessions ->
                if (sessions.isNotEmpty() && _currentSession.value == null) {
                    val session14 = sessions.firstOrNull { it.session.title.contains("14") } ?: sessions.first()
                    selectSession(session14.session.id)
                }
            }
        }
    }

    fun selectSession(sessionId: Long) {
        _currentSessionId.value = sessionId
        viewModelScope.launch {
            repository.getSession(sessionId).collect { sessionWithSegments ->
                if (sessionWithSegments != null) {
                    _currentSession.value = sessionWithSegments
                    _sourceLanguage.value = Language.findByCode(sessionWithSegments.session.sourceLanguageCode)
                    _targetLanguage.value = Language.findByCode(sessionWithSegments.session.targetLanguageCode)
                    _totalDurationSec.value = sessionWithSegments.session.durationSeconds.coerceAtLeast(60)
                }
            }
        }
    }

    fun setViewMode(mode: ViewDisplayMode) {
        _viewMode.value = mode
    }

    fun setActiveScreen(screen: ActiveScreen) {
        _activeScreen.value = screen
    }

    fun setSourceLanguage(language: Language) {
        _sourceLanguage.value = language
        if (homeInputText.value.isNotBlank()) {
            homeTranslatedText.value = OfflineTranslationEngine.translate(homeInputText.value, language.code, _targetLanguage.value.code)
        }
    }

    fun setTargetLanguage(language: Language) {
        _targetLanguage.value = language
        if (homeInputText.value.isNotBlank()) {
            homeTranslatedText.value = OfflineTranslationEngine.translate(homeInputText.value, _sourceLanguage.value.code, language.code)
        }
    }

    fun swapLanguages() {
        val temp = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = temp
        if (homeInputText.value.isNotBlank()) {
            homeTranslatedText.value = OfflineTranslationEngine.translate(homeInputText.value, _sourceLanguage.value.code, _targetLanguage.value.code)
        }
    }

    fun setHomeInputText(text: String) {
        homeInputText.value = text
        if (text.isBlank()) {
            homeTranslatedText.value = ""
        } else {
            homeTranslatedText.value = OfflineTranslationEngine.translate(text, _sourceLanguage.value.code, _targetLanguage.value.code)
        }
    }

    fun clearHomeInput() {
        homeInputText.value = ""
        homeTranslatedText.value = ""
    }

    fun speakHomeTranslation() {
        val text = homeTranslatedText.value
        if (text.isNotBlank()) {
            ttsHelper.speak(text, _targetLanguage.value.locale)
        }
    }

    fun speakHomeSource() {
        val text = homeInputText.value
        if (text.isNotBlank()) {
            ttsHelper.speak(text, _sourceLanguage.value.locale)
        }
    }

    fun openSession(sessionId: Long) {
        selectSession(sessionId)
        _activeScreen.value = ActiveScreen.LIVE_SESSION
    }

    fun startNewLiveSession() {
        viewModelScope.launch {
            stopAudioPlayback()
            stopLiveListening()

            val newCount = (allSessions.value.size + 1)
            val newTitle = "Live translation $newCount"
            val newId = repository.createNewSession(
                title = newTitle,
                sourceLanguageCode = _sourceLanguage.value.code,
                targetLanguageCode = _targetLanguage.value.code
            )

            val freshSession = SessionWithSegments(
                session = TranslationSession(
                    id = newId,
                    title = newTitle,
                    timestamp = System.currentTimeMillis(),
                    durationSeconds = 0,
                    sourceLanguageCode = _sourceLanguage.value.code,
                    targetLanguageCode = _targetLanguage.value.code,
                    isFavorite = false
                ),
                segments = emptyList()
            )
            _currentSession.value = freshSession
            _currentSessionId.value = newId
            _currentPlaybackSec.value = 0
            _totalDurationSec.value = 0
            _activeScreen.value = ActiveScreen.LIVE_SESSION

            selectSession(newId)
        }
    }

    fun toggleLiveListening() {
        if (_isListening.value) {
            stopLiveListening()
        } else {
            startLiveListening()
        }
    }

    fun startLiveListening() {
        stopAudioPlayback()
        _isListening.value = true
        partialTranscriptText.value = ""
        partialTranslationText.value = ""

        try {
            speechHelper?.startListening(_sourceLanguage.value.locale, continuous = true)
        } catch (e: Exception) {
            // Graceful fallback
        }

        liveTimerJob?.cancel()
        liveTimerJob = viewModelScope.launch {
            while (_isListening.value) {
                delay(1000)
                _totalDurationSec.value += 1
                _currentPlaybackSec.value = _totalDurationSec.value
                val sessId = _currentSessionId.value
                if (sessId != null) {
                    repository.updateSessionDuration(sessId, _totalDurationSec.value)
                }
            }
        }
    }

    fun stopLiveListening() {
        _isListening.value = false
        speechHelper?.stopListening()
        liveTimerJob?.cancel()
        partialTranscriptText.value = ""
        partialTranslationText.value = ""
    }

    fun submitManualUtterance(text: String, speaker: String = "Speaker 1") {
        if (text.isBlank()) return
        viewModelScope.launch {
            var currentSess = _currentSession.value
            if (currentSess == null) {
                val newCount = (allSessions.value.size + 1)
                val newTitle = "Live translation $newCount"
                val newId = repository.createNewSession(
                    title = newTitle,
                    sourceLanguageCode = _sourceLanguage.value.code,
                    targetLanguageCode = _targetLanguage.value.code
                )
                currentSess = SessionWithSegments(
                    session = TranslationSession(
                        id = newId,
                        title = newTitle,
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = 0,
                        sourceLanguageCode = _sourceLanguage.value.code,
                        targetLanguageCode = _targetLanguage.value.code,
                        isFavorite = false
                    ),
                    segments = emptyList()
                )
                _currentSession.value = currentSess
                _currentSessionId.value = newId
                selectSession(newId)
            }

            val isSpeaker1 = speaker.contains("1") || speaker.contains("You", ignoreCase = true)
            val srcLangCode = if (isSpeaker1) _sourceLanguage.value.code else _targetLanguage.value.code
            val tgtLangCode = if (isSpeaker1) _targetLanguage.value.code else _sourceLanguage.value.code

            val translated = OfflineTranslationEngine.translate(text, srcLangCode, tgtLangCode)

            val order = currentSess.segments.size
            val timeOffset = if (_currentPlaybackSec.value > 0) _currentPlaybackSec.value else (order * 15 + 5)

            val segId = repository.addSegment(
                sessionId = currentSess.session.id,
                timeOffsetSeconds = timeOffset,
                sourceText = text,
                translatedText = translated,
                speaker = speaker,
                orderIndex = order
            )

            val newSegment = TranscriptSegment(
                id = segId,
                sessionId = currentSess.session.id,
                timeOffsetSeconds = timeOffset,
                sourceText = text,
                translatedText = translated,
                speaker = speaker,
                orderIndex = order
            )
            _currentSession.value = currentSess.copy(
                segments = currentSess.segments + newSegment
            )

            val updatedDuration = maxOf(_totalDurationSec.value, timeOffset + 5)
            _totalDurationSec.value = updatedDuration
            _currentPlaybackSec.value = updatedDuration
            repository.updateSessionDuration(currentSess.session.id, updatedDuration)

            partialTranscriptText.value = ""
            partialTranslationText.value = ""
        }
    }

    private fun processLiveSpeechResult(text: String, speaker: String) {
        submitManualUtterance(text, speaker)
    }

    // Audio Playback / Scrubber methods
    fun toggleAudioPlayback() {
        if (_isPlayingAudio.value) {
            stopAudioPlayback()
        } else {
            startAudioPlayback()
        }
    }

    fun startAudioPlayback() {
        stopLiveListening()
        _isPlayingAudio.value = true

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val maxDuration = _totalDurationSec.value.coerceAtLeast(30)

            while (_isPlayingAudio.value && _currentPlaybackSec.value < maxDuration) {
                delay(1000)
                _currentPlaybackSec.value += 1

                // Find matching segment to highlight
                val currentSegments = _currentSession.value?.getSortedSegments() ?: emptyList()
                val matchIndex = currentSegments.indexOfLast { it.timeOffsetSeconds <= _currentPlaybackSec.value }
                if (matchIndex != -1) {
                    _activeHighlightIndex.value = matchIndex
                }

                if (_currentPlaybackSec.value >= maxDuration) {
                    _isPlayingAudio.value = false
                    _currentPlaybackSec.value = 0
                    _activeHighlightIndex.value = null
                    break
                }
            }
        }
    }

    fun stopAudioPlayback() {
        _isPlayingAudio.value = false
        playbackJob?.cancel()
        ttsHelper.stop()
    }

    fun seekTo(seconds: Int) {
        _currentPlaybackSec.value = seconds
        val currentSegments = _currentSession.value?.getSortedSegments() ?: emptyList()
        val matchIndex = currentSegments.indexOfLast { it.timeOffsetSeconds <= seconds }
        _activeHighlightIndex.value = if (matchIndex != -1) matchIndex else null
    }

    // TTS Voice Translation Playback
    fun speakSegment(segment: TranscriptSegment) {
        val targetLocale = _targetLanguage.value.locale
        _isSpeaking.value = true
        ttsHelper.speak(
            text = segment.translatedText,
            locale = targetLocale,
            onStart = { _isSpeaking.value = true },
            onDone = { _isSpeaking.value = false }
        )
    }

    fun speakSource(segment: TranscriptSegment) {
        val sourceLocale = _sourceLanguage.value.locale
        _isSpeaking.value = true
        ttsHelper.speak(
            text = segment.sourceText,
            locale = sourceLocale,
            onStart = { _isSpeaking.value = true },
            onDone = { _isSpeaking.value = false }
        )
    }

    // Face to Face Mode Operations
    fun processFaceToFaceSpeech(speaker: Int, text: String) {
        if (text.isBlank()) return

        if (speaker == 1) {
            _faceToFaceSpeaker1Text.value = text
            val trans = OfflineTranslationEngine.translate(text, _sourceLanguage.value.code, _targetLanguage.value.code)
            _faceToFaceSpeaker1Trans.value = trans
            ttsHelper.speak(trans, _targetLanguage.value.locale)
        } else {
            _faceToFaceSpeaker2Text.value = text
            val trans = OfflineTranslationEngine.translate(text, _targetLanguage.value.code, _sourceLanguage.value.code)
            _faceToFaceSpeaker2Trans.value = trans
            ttsHelper.speak(trans, _sourceLanguage.value.locale)
        }
    }

    // Session Management
    fun renameSession(sessionId: Long, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updateSessionTitle(sessionId, newTitle.trim())
            Toast.makeText(getApplication(), "Session renamed", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFavorite(sessionId: Long, current: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(sessionId, current)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            val remaining = allSessions.value.filter { it.session.id != sessionId }
            if (remaining.isNotEmpty()) {
                selectSession(remaining.first().session.id)
            } else {
                _currentSession.value = null
            }
            Toast.makeText(getApplication(), "Session deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportCurrentSession(format: ExportFormat, includeTimestamps: Boolean, includeSourceText: Boolean) {
        val sessionData = _currentSession.value ?: return
        val result = DocumentExportManager.exportAndShare(
            context = getApplication(),
            sessionWithSegments = sessionData,
            format = format,
            includeTimestamps = includeTimestamps,
            includeSourceText = includeSourceText
        )

        if (result.isSuccess) {
            Toast.makeText(getApplication(), "Exported to ${format.title}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechHelper?.destroy()
        ttsHelper.shutdown()
        playbackJob?.cancel()
        liveTimerJob?.cancel()
    }
}
