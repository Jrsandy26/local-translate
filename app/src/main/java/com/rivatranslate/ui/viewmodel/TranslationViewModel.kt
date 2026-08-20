package com.rivatranslate.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rivatranslate.audio.AudioPlaybackHelper
import com.rivatranslate.audio.AudioRecorderHelper
import com.rivatranslate.audio.SpeechRecognitionHelper
import com.rivatranslate.audio.TextToSpeechHelper
import com.rivatranslate.data.db.AppDatabase
import com.rivatranslate.data.repository.TranslationRepository
import com.rivatranslate.model.ActiveScreen
import com.rivatranslate.model.AppThemeMode
import com.rivatranslate.model.Language
import com.rivatranslate.model.RecentTranslation
import com.rivatranslate.model.TranscriptSegment
import com.rivatranslate.model.TranslationSession

import com.rivatranslate.service.LiveSessionManager
import com.rivatranslate.service.LiveTranslationService
import com.rivatranslate.translation.GoogleTranslationEngine
import com.rivatranslate.translation.LanguageModelDownloadWorker
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
    val isModelDownloading = MutableStateFlow(false)

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
    val liveTimerSeconds = MutableStateFlow(0)
    val recordAudioChecked = MutableStateFlow(true)
    private var liveTimerJob: Job? = null
    private var lastRecordedAudioPath: String? = null

    // Session completion & stopped dialog
    val showSessionStoppedDialog = MutableStateFlow(false)
    val completedSession = MutableStateFlow<TranslationSession?>(null)
    val completedSegments = MutableStateFlow<List<TranscriptSegment>>(emptyList())

    // History Session Details Dialog / Viewer
    val showSessionDetailDialog = MutableStateFlow(false)
    val activeDetailSession = MutableStateFlow<TranslationSession?>(null)
    val activeDetailSegments = MutableStateFlow<List<TranscriptSegment>>(emptyList())

    // Audio Helpers
    val audioRecorderHelper = AudioRecorderHelper(application)
    val audioPlaybackHelper = AudioPlaybackHelper(application)

    val isAudioPlaying: StateFlow<Boolean> = audioPlaybackHelper.isPlaying
    val audioPlaybackPosMs: StateFlow<Int> = audioPlaybackHelper.currentPositionMs
    val audioPlaybackDurationMs: StateFlow<Int> = audioPlaybackHelper.totalDurationMs
    val activePlaybackSegmentIndex: StateFlow<Int> = audioPlaybackHelper.activeSegmentIndex

    // Sequential TTS playback job
    private var sequentialTtsJob: Job? = null

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

    // Language Download States
    private val _downloadedCodes = MutableStateFlow(setOf("en"))
    val downloadedCodes: StateFlow<Set<String>> = _downloadedCodes.asStateFlow()

    private val _downloadingCodes = MutableStateFlow(setOf<String>())
    val downloadingCodes: StateFlow<Set<String>> = _downloadingCodes.asStateFlow()

    fun refreshDownloadedModels() {
        viewModelScope.launch {
            val codes = mutableSetOf("en")
            Language.ALL_LANGUAGES.forEach { lang ->
                if (GoogleTranslationEngine.isModelDownloaded(lang.code)) {
                    codes.add(lang.code)
                }
            }
            _downloadedCodes.value = codes
        }
    }

    fun downloadLanguageModel(langCode: String) {
        _downloadingCodes.value = _downloadingCodes.value + langCode
        GoogleTranslationEngine.downloadModel(
            langCode = langCode,
            onSuccess = {
                _downloadedCodes.value = _downloadedCodes.value + langCode
                _downloadingCodes.value = _downloadingCodes.value - langCode
            },
            onFailure = {
                _downloadingCodes.value = _downloadingCodes.value - langCode
            }
        )
    }

    fun deleteLanguageModel(langCode: String) {
        _downloadingCodes.value = _downloadingCodes.value + langCode
        GoogleTranslationEngine.deleteModel(
            langCode = langCode,
            onSuccess = {
                _downloadedCodes.value = _downloadedCodes.value - langCode
                _downloadingCodes.value = _downloadingCodes.value - langCode
            },
            onFailure = {
                _downloadingCodes.value = _downloadingCodes.value - langCode
            }
        )
    }

    // Settings & Theme Preferences
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val themeMode = MutableStateFlow(
        AppThemeMode.fromKey(prefs.getString("key_theme_mode", AppThemeMode.SYSTEM.key))
    )

    val speechSpeed = MutableStateFlow(prefs.getFloat("key_speech_speed", 1.0f))
    val speechPitch = MutableStateFlow(prefs.getFloat("key_speech_pitch", 1.0f))
    val preferredVoiceGender = MutableStateFlow(prefs.getString("key_voice_gender", "Default") ?: "Default")

    fun setThemeMode(mode: AppThemeMode) {
        themeMode.value = mode
        prefs.edit().putString("key_theme_mode", mode.key).apply()
    }

    fun toggleThemeMode() {
        val current = themeMode.value
        val nextMode = when (current) {
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.SYSTEM -> AppThemeMode.DARK
        }
        setThemeMode(nextMode)
    }

    fun updateSpeechSpeed(speed: Float) {
        speechSpeed.value = speed
        prefs.edit().putFloat("key_speech_speed", speed).apply()
    }

    fun updateSpeechPitch(pitch: Float) {
        speechPitch.value = pitch
        prefs.edit().putFloat("key_speech_pitch", pitch).apply()
    }

    fun updateVoiceGender(gender: String) {
        preferredVoiceGender.value = gender
        prefs.edit().putString("key_voice_gender", gender).apply()
    }

    private var speechHelper: SpeechRecognitionHelper? = null
    private var ttsHelper: TextToSpeechHelper? = null
    private var translateDebounceJob: Job? = null
    private var historySaveJob: Job? = null

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
                LiveSessionManager.updateRms(rms)
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

        // Synchronize with LiveSessionManager for Background Service & Live Notifications
        LiveSessionManager.onSessionStateChanged = { running, paused ->
            isLiveSessionRunning.value = running
            isLiveSessionPaused.value = paused
            if (!running) {
                speechHelper?.stopListening()
            }
        }
        LiveSessionManager.onTimerTick = { seconds ->
            liveTimerSeconds.value = seconds
        }
        LiveSessionManager.onActionFromNotification = { action ->
            when (action) {
                LiveTranslationService.ACTION_PAUSE -> {
                    pauseLiveSession()
                }
                LiveTranslationService.ACTION_RESUME -> {
                    resumeLiveSession()
                }
                LiveTranslationService.ACTION_STOP -> {
                    stopLiveSession()
                }
            }
        }
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
            clearHomeInput()
            return
        }
        triggerTranslation(text)
    }

    fun clearHomeInput() {
        homeInputText.value = ""
        homeTranslatedText.value = ""
        isTranslating.value = false
        translateDebounceJob?.cancel()
        historySaveJob?.cancel()
    }

    private fun triggerTranslation(text: String) {
        translateDebounceJob?.cancel()
        translateDebounceJob = viewModelScope.launch {
            delay(300)
            isTranslating.value = true
            
            // Check if model is downloaded
            val isSrcDownloaded = GoogleTranslationEngine.isModelDownloaded(_sourceLanguage.value.code)
            val isTgtDownloaded = GoogleTranslationEngine.isModelDownloaded(_targetLanguage.value.code)
            
            if (!isSrcDownloaded || !isTgtDownloaded) {
                isModelDownloading.value = true
            }

            try {
                val result = GoogleTranslationEngine.translate(
                    text = text,
                    sourceLangCode = _sourceLanguage.value.code,
                    targetLangCode = _targetLanguage.value.code
                )
                homeTranslatedText.value = result
                
                // Debounced history save
                scheduleHistorySave(text.trim(), result)
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Ignore debounce cancellations cleanly
            } catch (e: Exception) {
                Log.e("TranslationVM", "Translation error", e)
            } finally {
                isTranslating.value = false
                isModelDownloading.value = false
            }
        }
    }

    private fun scheduleHistorySave(source: String, translated: String) {
        if (source.length <= 1) return
        
        historySaveJob?.cancel()
        historySaveJob = viewModelScope.launch {
            delay(2000) // Wait for user to stop typing
            repository.addRecentTranslation(
                source = source,
                translated = translated,
                sourceLang = _sourceLanguage.value.code,
                targetLang = _targetLanguage.value.code
            )
        }
    }

    fun toggleMicListening() {
        if (_isListening.value) {
            speechHelper?.stopListening()
        } else {
            val locale = Locale.forLanguageTag(_sourceLanguage.value.code)
            speechHelper?.continuousMode = false
            speechHelper?.startListening(locale)
        }
    }

    fun startLiveSession() {
        liveSegments.value = emptyList()
        livePartialText.value = ""
        livePartialTranslated.value = ""
        liveTimerSeconds.value = 0
        isLiveSessionRunning.value = true
        isLiveSessionPaused.value = false
        
        if (recordAudioChecked.value) {
            lastRecordedAudioPath = audioRecorderHelper.startRecording()
        } else {
            lastRecordedAudioPath = null
        }

        speechHelper?.continuousMode = true
        LiveSessionManager.startService(getApplication())
        val locale = Locale.forLanguageTag(_sourceLanguage.value.code)
        speechHelper?.startListening(locale)
    }

    fun pauseLiveSession() {
        isLiveSessionPaused.value = true
        audioRecorderHelper.pauseRecording()
        LiveSessionManager.pauseService(getApplication())
        speechHelper?.pauseListening()
    }

    fun resumeLiveSession() {
        isLiveSessionPaused.value = false
        audioRecorderHelper.resumeRecording()
        LiveSessionManager.resumeService(getApplication())
        val locale = Locale.forLanguageTag(_sourceLanguage.value.code)
        speechHelper?.resumeListening(locale)
    }

    fun stopLiveSession() {
        isLiveSessionRunning.value = false
        isLiveSessionPaused.value = false
        speechHelper?.stopListening()
        
        val recordedPath = if (recordAudioChecked.value) {
            audioRecorderHelper.stopRecording() ?: lastRecordedAudioPath
        } else {
            audioRecorderHelper.stopRecording()
            null
        }

        LiveSessionManager.stopService(getApplication())

        val sessionDuration = liveTimerSeconds.value
        val title = "${_sourceLanguage.value.name} → ${_targetLanguage.value.name}"
        val currentPartial = livePartialText.value.trim()
        val currentPartialTrans = livePartialTranslated.value.trim()

        viewModelScope.launch {
            var allSegs = liveSegments.value.toMutableList()

            // 1. Flush any pending recognized speech if not yet added to segments
            if (currentPartial.isNotBlank()) {
                val translated = if (currentPartialTrans.isNotBlank()) {
                    currentPartialTrans
                } else {
                    try {
                        GoogleTranslationEngine.translate(
                            currentPartial,
                            _sourceLanguage.value.code,
                            _targetLanguage.value.code
                        )
                    } catch (e: Exception) {
                        currentPartial
                    }
                }

                val finalSeg = TranscriptSegment(
                    sessionId = 0,
                    speaker = "Speaker ${_sourceLanguage.value.code.uppercase()}",
                    sourceText = currentPartial,
                    translatedText = translated,
                    isSourceSpeaker = true
                )
                allSegs.add(finalSeg)
                liveSegments.value = allSegs
            }

            livePartialText.value = ""
            livePartialTranslated.value = ""

            // 2. Persist session and segments to Room database
            val duration = sessionDuration.coerceAtLeast(if (allSegs.isNotEmpty()) 1 else 0)
            val savedId = repository.saveCompletedSession(
                title = title,
                src = _sourceLanguage.value.code,
                tgt = _targetLanguage.value.code,
                durationSeconds = duration,
                audioPath = recordedPath,
                segments = allSegs
            )

            // 3. Also insert segments to recent_translations so they appear in both History tabs
            for (seg in allSegs) {
                if (seg.sourceText.isNotBlank()) {
                    try {
                        repository.addRecentTranslation(
                            source = seg.sourceText,
                            translated = seg.translatedText,
                            sourceLang = _sourceLanguage.value.code,
                            targetLang = _targetLanguage.value.code
                        )
                    } catch (e: Exception) {
                        Log.e("TranslationVM", "Error inserting recent translation", e)
                    }
                }
            }

            val savedSegments = allSegs.map { it.copy(sessionId = savedId) }

            completedSession.value = TranslationSession(
                id = savedId,
                title = title,
                sourceLanguageCode = _sourceLanguage.value.code,
                targetLanguageCode = _targetLanguage.value.code,
                durationSeconds = duration,
                audioFilePath = recordedPath,
                createdAt = System.currentTimeMillis()
            )
            completedSegments.value = savedSegments
            showSessionStoppedDialog.value = true
        }
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

    // Audio and Synced Transcript Playback Methods
    fun playSessionAudio(
        audioPath: String?,
        segments: List<TranscriptSegment>,
        targetLangCode: String = _targetLanguage.value.code
    ) {
        stopAudioPlayback()
        if (!audioPath.isNullOrBlank() && java.io.File(audioPath).exists()) {
            // Play real recorded audio file
            audioPlaybackHelper.playAudioFile(
                filePath = audioPath,
                onComplete = {
                    audioPlaybackHelper.setActiveSegment(-1)
                },
                onError = {
                    // Fallback to sequential TTS if audio file cannot be played
                    if (segments.isNotEmpty()) {
                        playSequentialTts(segments, useTranslated = true, targetLangCode = targetLangCode)
                    }
                }
            )
        } else if (segments.isNotEmpty()) {
            // Play sequential synchronized TTS speech
            playSequentialTts(segments, useTranslated = true, targetLangCode = targetLangCode)
        }
    }

    fun playSequentialTts(
        segments: List<TranscriptSegment>,
        useTranslated: Boolean,
        targetLangCode: String = _targetLanguage.value.code,
        fallbackAudioPath: String? = null
    ) {
        stopAudioPlayback()
        if (segments.isEmpty()) {
            if (!fallbackAudioPath.isNullOrBlank() && java.io.File(fallbackAudioPath).exists()) {
                playSessionAudio(fallbackAudioPath, segments, targetLangCode)
            }
            return
        }

        val totalSec = (segments.size * 3).coerceAtLeast(3)
        val totalMs = totalSec * 1000
        audioPlaybackHelper.setSimulatedDuration(totalSec)
        audioPlaybackHelper.setPlayingState(true)

        sequentialTtsJob = viewModelScope.launch {
            val segmentDurationMs = 3000
            for (index in segments.indices) {
                if (!audioPlaybackHelper.isPlaying.value) break
                val seg = segments[index]
                audioPlaybackHelper.setActiveSegment(index)
                val textToSpeak = if (useTranslated) seg.translatedText else seg.sourceText
                val langToSpeak = if (useTranslated) targetLangCode else _sourceLanguage.value.code

                speakText(textToSpeak, langToSpeak)

                // Smoothly update progress in 100ms ticks across the 3 seconds segment window
                val segStartMs = index * segmentDurationMs
                val steps = 30
                for (step in 0 until steps) {
                    if (!audioPlaybackHelper.isPlaying.value) break
                    val currentMs = segStartMs + (step * 100)
                    audioPlaybackHelper.updateProgressManually(currentMs, totalMs)
                    delay(100)
                }
            }
            audioPlaybackHelper.setActiveSegment(-1)
            audioPlaybackHelper.setPlayingState(false)
            audioPlaybackHelper.updateProgressManually(totalMs, totalMs)
        }
    }

    fun pauseAudioPlayback() {
        sequentialTtsJob?.cancel()
        audioPlaybackHelper.pause()
    }

    fun resumeAudioPlayback() {
        audioPlaybackHelper.resume()
    }

    fun stopAudioPlayback() {
        sequentialTtsJob?.cancel()
        sequentialTtsJob = null
        audioPlaybackHelper.stop()
        audioPlaybackHelper.setActiveSegment(-1)
    }

    fun seekAudioPlayback(posMs: Int) {
        audioPlaybackHelper.seekTo(posMs)
    }

    fun playSingleSegment(segment: TranscriptSegment, speakTranslated: Boolean) {
        val text = if (speakTranslated) segment.translatedText else segment.sourceText
        val lang = if (speakTranslated) _targetLanguage.value.code else _sourceLanguage.value.code
        speakText(text, lang)
    }

    fun openSessionDetail(session: TranslationSession) {
        viewModelScope.launch {
            val segs = repository.getSegmentsList(session.id)
            activeDetailSession.value = session
            activeDetailSegments.value = segs
            showSessionDetailDialog.value = true
        }
    }

    suspend fun getSessionSegments(sessionId: Long): List<TranscriptSegment> {
        return repository.getSegmentsList(sessionId)
    }

    fun deleteSession(session: TranslationSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
            if (activeDetailSession.value?.id == session.id) {
                showSessionDetailDialog.value = false
                activeDetailSession.value = null
            }
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            repository.clearAllSessions()
        }
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
        ttsHelper?.preferredVoiceGender = preferredVoiceGender.value
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
        LiveSessionManager.stopService(getApplication())
        GoogleTranslationEngine.close()
    }
}
