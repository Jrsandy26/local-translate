package com.example.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioRecorderHelper
import com.example.audio.SpeechRecognitionHelper
import com.example.audio.TextToSpeechHelper
import com.example.data.db.AppDatabase
import com.example.data.repository.TranslationRepository
import com.example.engine.OfflineTranslationEngine
import com.example.translation.GoogleTranslationEngine
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
    private val audioRecorderHelper: AudioRecorderHelper = AudioRecorderHelper(application)
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

    // Settings States
    val voiceSpeedStage = MutableStateFlow(2) // 0: 0.5x, 1: 0.75x, 2: 1.0x (Normal), 3: 1.25x
    val isFemaleVoice = MutableStateFlow(true) // true: Female, false: Male

    fun getVoiceSpeedRate(): Float {
        return when (voiceSpeedStage.value) {
            0 -> 0.5f
            1 -> 0.75f
            2 -> 1.0f
            3 -> 1.25f
            else -> 1.0f
        }
    }
    private val _sourceLanguage = MutableStateFlow(Language.findByCode("en"))
    val sourceLanguage: StateFlow<Language> = _sourceLanguage.asStateFlow()

    private val _targetLanguage = MutableStateFlow(Language.findByCode("ja"))
    val targetLanguage: StateFlow<Language> = _targetLanguage.asStateFlow()

    // Google ML Kit Model States
    val downloadedModels = MutableStateFlow<Map<String, Boolean>>(
        mapOf("en" to true, "ja" to true)
    )
    val downloadingModels = MutableStateFlow<Map<String, Boolean>>(emptyMap())

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
                val immediate = OfflineTranslationEngine.translate(
                    partial,
                    _sourceLanguage.value.code,
                    _targetLanguage.value.code
                )
                partialTranslationText.value = immediate
                
                viewModelScope.launch {
                    val googleRes = GoogleTranslationEngine.translate(
                        partial,
                        _sourceLanguage.value.code,
                        _targetLanguage.value.code
                    )
                    if (!googleRes.isNullOrBlank()) {
                        partialTranslationText.value = googleRes
                    }
                }
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
            setHomeInputText(homeInputText.value)
        }
    }

    fun setTargetLanguage(language: Language) {
        _targetLanguage.value = language
        if (homeInputText.value.isNotBlank()) {
            setHomeInputText(homeInputText.value)
        }
    }

    fun swapLanguages() {
        val temp = _sourceLanguage.value
        _sourceLanguage.value = _targetLanguage.value
        _targetLanguage.value = temp
        if (homeInputText.value.isNotBlank()) {
            setHomeInputText(homeInputText.value)
        }
    }

    fun setHomeInputText(text: String) {
        homeInputText.value = text
        if (text.isBlank()) {
            homeTranslatedText.value = ""
        } else {
            val immediate = OfflineTranslationEngine.translate(text, _sourceLanguage.value.code, _targetLanguage.value.code)
            homeTranslatedText.value = immediate

            viewModelScope.launch {
                val googleRes = GoogleTranslationEngine.translate(text, _sourceLanguage.value.code, _targetLanguage.value.code)
                if (!googleRes.isNullOrBlank()) {
                    homeTranslatedText.value = googleRes
                }
            }
        }
    }

    fun clearHomeInput() {
        homeInputText.value = ""
        homeTranslatedText.value = ""
    }

    fun speakHomeTranslation() {
        val text = homeTranslatedText.value
        if (text.isNotBlank()) {
            ttsHelper.speak(
                text = text,
                locale = _targetLanguage.value.locale,
                speedRate = getVoiceSpeedRate(),
                isFemaleVoice = isFemaleVoice.value
            )
        }
    }

    fun speakHomeSource() {
        val text = homeInputText.value
        if (text.isNotBlank()) {
            ttsHelper.speak(
                text = text,
                locale = _sourceLanguage.value.locale,
                speedRate = getVoiceSpeedRate(),
                isFemaleVoice = isFemaleVoice.value
            )
        }
    }

    fun saveHomeTranslationToHistory() {
        val srcText = homeInputText.value
        val transText = homeTranslatedText.value
        if (srcText.isBlank() || transText.isBlank()) return

        viewModelScope.launch {
            val title = "Saved: ${srcText.take(20)}..."
            val newId = repository.createNewSession(
                title = title,
                sourceLanguageCode = _sourceLanguage.value.code,
                targetLanguageCode = _targetLanguage.value.code
            )
            repository.addSegment(
                sessionId = newId,
                timeOffsetSeconds = 0,
                sourceText = srcText,
                translatedText = transText,
                speaker = "User",
                orderIndex = 0
            )
            repository.toggleFavorite(newId, false)
            Toast.makeText(getApplication(), "Saved to History & Favorites!", Toast.LENGTH_SHORT).show()
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

        val sessId = _currentSessionId.value
        if (recordAudioEnabled.value && sessId != null) {
            val audioPath = audioRecorderHelper.startRecording(sessId)
            if (audioPath != null) {
                viewModelScope.launch {
                    repository.updateSessionAudioPath(sessId, audioPath)
                }
            }
        }

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
                val id = _currentSessionId.value
                if (id != null) {
                    repository.updateSessionDuration(id, _totalDurationSec.value)
                }
            }
        }
    }

    fun stopLiveListening() {
        _isListening.value = false
        speechHelper?.stopListening()
        val savedPath = audioRecorderHelper.stopRecording()
        val sessId = _currentSessionId.value
        if (savedPath != null && sessId != null) {
            viewModelScope.launch {
                repository.updateSessionAudioPath(sessId, savedPath)
            }
        }
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

            var translated = GoogleTranslationEngine.translate(text, srcLangCode, tgtLangCode)
            if (translated.isNullOrBlank()) {
                translated = OfflineTranslationEngine.translate(text, srcLangCode, tgtLangCode)
            }

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

        val audioPath = _currentSession.value?.session?.audioFilePath
        if (!audioPath.isNullOrBlank()) {
            audioRecorderHelper.playAudio(audioPath) {
                _isPlayingAudio.value = false
                _currentPlaybackSec.value = 0
                _activeHighlightIndex.value = null
            }
        }

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
        audioRecorderHelper.stopPlayback()
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
            speedRate = getVoiceSpeedRate(),
            isFemaleVoice = isFemaleVoice.value,
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
            speedRate = getVoiceSpeedRate(),
            isFemaleVoice = isFemaleVoice.value,
            onStart = { _isSpeaking.value = true },
            onDone = { _isSpeaking.value = false }
        )
    }

    // Face to Face Mode Operations
    fun processFaceToFaceSpeech(speaker: Int, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            if (speaker == 1) {
                _faceToFaceSpeaker1Text.value = text
                var trans = GoogleTranslationEngine.translate(text, _sourceLanguage.value.code, _targetLanguage.value.code)
                if (trans.isNullOrBlank()) {
                    trans = OfflineTranslationEngine.translate(text, _sourceLanguage.value.code, _targetLanguage.value.code)
                }
                _faceToFaceSpeaker1Trans.value = trans
                ttsHelper.speak(trans, _targetLanguage.value.locale)
            } else {
                _faceToFaceSpeaker2Text.value = text
                var trans = GoogleTranslationEngine.translate(text, _targetLanguage.value.code, _sourceLanguage.value.code)
                if (trans.isNullOrBlank()) {
                    trans = OfflineTranslationEngine.translate(text, _targetLanguage.value.code, _sourceLanguage.value.code)
                }
                _faceToFaceSpeaker2Trans.value = trans
                ttsHelper.speak(trans, _sourceLanguage.value.locale)
            }
        }
    }

    // Google ML Kit Language Pack Model Downloads
    fun refreshDownloadedModels() {
        viewModelScope.launch {
            val updatedMap = mutableMapOf<String, Boolean>()
            Language.SUPPORTED_LANGUAGES.forEach { lang ->
                val isDownloaded = GoogleTranslationEngine.isModelDownloaded(lang.code)
                updatedMap[lang.code] = isDownloaded
            }
            downloadedModels.value = updatedMap
        }
    }

    fun downloadGoogleLanguageModel(langCode: String) {
        viewModelScope.launch {
            val currentDownloading = downloadingModels.value.toMutableMap()
            currentDownloading[langCode] = true
            downloadingModels.value = currentDownloading

            GoogleTranslationEngine.downloadModel(
                langCode = langCode,
                onSuccess = {
                    val updatedDownloading = downloadingModels.value.toMutableMap()
                    updatedDownloading.remove(langCode)
                    downloadingModels.value = updatedDownloading

                    val updatedDownloaded = downloadedModels.value.toMutableMap()
                    updatedDownloaded[langCode] = true
                    downloadedModels.value = updatedDownloaded

                    Toast.makeText(getApplication(), "Downloaded Google model for ${Language.findByCode(langCode).name}", Toast.LENGTH_SHORT).show()
                },
                onFailure = {
                    val updatedDownloading = downloadingModels.value.toMutableMap()
                    updatedDownloading.remove(langCode)
                    downloadingModels.value = updatedDownloading

                    Toast.makeText(getApplication(), "Failed to download model for ${langCode.uppercase()}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    fun deleteGoogleLanguageModel(langCode: String) {
        viewModelScope.launch {
            if (langCode.equals("en", ignoreCase = true)) {
                Toast.makeText(getApplication(), "English base model is required", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val deleted = GoogleTranslationEngine.deleteModel(langCode)
            if (deleted) {
                val updatedDownloaded = downloadedModels.value.toMutableMap()
                updatedDownloaded[langCode] = false
                downloadedModels.value = updatedDownloaded
                Toast.makeText(getApplication(), "Removed Google model for ${Language.findByCode(langCode).name}", Toast.LENGTH_SHORT).show()
            }
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
        audioRecorderHelper.release()
        playbackJob?.cancel()
        liveTimerJob?.cancel()
    }
}
