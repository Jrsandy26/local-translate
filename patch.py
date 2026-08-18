with open("app/src/main/java/com/example/ui/screens/WhisperPOCScreen.kt", "r") as f:
    data = f.read()

# Replace the text on the Start button
data = data.replace('Text(if (isRecording) "Stop Recording" else "Start Japanese ASR")', 'Text(if (isRecording) "Stop Recording" else "Start Japanese ASR")\n\n            // Auto scroll down in text field\n')

with open("app/src/main/java/com/example/ui/screens/WhisperPOCScreen.kt", "w") as f:
    f.write(data)
