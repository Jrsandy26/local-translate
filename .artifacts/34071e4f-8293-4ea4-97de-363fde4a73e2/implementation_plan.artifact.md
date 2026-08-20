# Implementation Plan - Translation Robustness & Language Expansion

This plan addresses translation stability, adds new Indian languages, and fixes performance issues in the Languages screen.

## User Review Required

> [!IMPORTANT]
> **Performance Fix**: The "Language Packs" screen scrolling lag will be fixed by optimizing how the list items are rendered and tracked.
> **Language Expansion**: I am adding **Telugu**, **Kannada**, and **Malayalam** to the supported offline languages.

## Proposed Changes

### 1. Translation Robustness

#### [MODIFY] [GoogleTranslationEngine.kt](file:///C:/Users/sande/AndroidStudioProjects/local-translate/app/src/main/java/com/rivatranslate/translation/GoogleTranslationEngine.kt)
- **Fix Broken Dictionary**: Correct the malformed Spanish entry for "where is the train station".
- **Enhanced Translator Lifecycle**: Limit the number of active translators in memory to prevent crashes on low-end devices.
- **Improved Fallback**: Ensure the smart fallback logic handles the new Indian languages.

### 2. Language Expansion

#### [MODIFY] [Language.kt](file:///C:/Users/sande/AndroidStudioProjects/local-translate/app/src/main/java/com/rivatranslate/model/Language.kt)
- Add entries for **Telugu** (`te`), **Kannada** (`kn`), and **Malayalam** (`ml`).

#### [MODIFY] [GoogleTranslationEngine.kt](file:///C:/Users/sande/AndroidStudioProjects/local-translate/app/src/main/java/com/rivatranslate/translation/GoogleTranslationEngine.kt)
- Update `mapLangCode` to include `te`, `kn`, and `ml` mapping to ML Kit constants.

### 3. UI Optimization (Fix Lag)

#### [MODIFY] [LanguagesScreen.kt](file:///C:/Users/sande/AndroidStudioProjects/local-translate/app/src/main/java/com/rivatranslate/ui/screens/LanguagesScreen.kt)
- **Lazy List Optimization**: Add `key` to `items()` in the `LazyColumn` for efficient re-ordering and rendering.
- **State Optimization**: Use `rememberDerivedStateOf` or move the download state management to the ViewModel to reduce unnecessary recompositions while scrolling.
- **Async Loading**: Improve the initial loading of "isDownloaded" states to be more efficient.

### 4. Translation Feedback

#### [MODIFY] [TranslateHomeScreen.kt](file:///C:/Users/sande/AndroidStudioProjects/local-translate/app/src/main/java/com/rivatranslate/ui/screens/TranslateHomeScreen.kt)
- **Loading Indicator**: Show a subtle `LinearProgressIndicator` when a translation is in progress.
- **Model Status**: If a translation is delayed due to a model download, display a temporary status message like "Downloading language pack...".

## Verification Plan

### Manual Verification
1.  **Scrolling Performance**: Navigate to "Language Packs" and scroll rapidly. Verify the lag is gone.
2.  **New Languages**: Verify that Telugu, Kannada, and Malayalam appear in the list and can be downloaded.
3.  **Robustness**: Verify that "where is the train station" translates correctly to Spanish.
4.  **Feedback**: Verify the loading indicator appears when typing a sentence to translate.
