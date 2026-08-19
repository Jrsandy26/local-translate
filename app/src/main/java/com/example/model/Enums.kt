package com.example.model

enum class ViewDisplayMode {
    BILINGUAL,
    TRANSLATION
}

enum class ExportFormat(val extension: String, val mimeType: String, val title: String) {
    TEXT("txt", "text/plain", "Plain Text (.txt)"),
    WORD("doc", "application/msword", "Word Document (.doc)"),
    PDF("pdf", "application/pdf", "PDF Document (.pdf)")
}

enum class ActiveScreen {
    TRANSLATE_HOME,
    LIVE_SESSION,
    FACE_TO_FACE,
    HISTORY,
    LANGUAGE_PACKS,
    LANGUAGE_LEARNING,
    SETTINGS
}
