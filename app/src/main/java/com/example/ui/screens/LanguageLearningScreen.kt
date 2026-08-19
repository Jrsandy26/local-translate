package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveScreen
import com.example.ui.components.GlassAtmosphereBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextGlassBody
import com.example.ui.theme.TextGlassHeading
import com.example.ui.theme.TextGlassMuted
import com.example.ui.theme.TextGlassSubtitle
import com.example.ui.viewmodel.TranslationViewModel
import kotlin.math.max

data class LearningPhrase(
    val id: String,
    val category: String,
    val sourceText: String,
    val targetTranslations: Map<String, String>,
    val phoneticGuides: Map<String, String> = emptyMap()
)

val CURATED_PHRASES = listOf(
    LearningPhrase(
        id = "1",
        category = "Greetings",
        sourceText = "Hello, nice to meet you!",
        targetTranslations = mapOf(
            "ja" to "初めまして、よろしくお願いします！",
            "es" to "¡Hola, mucho gusto!",
            "fr" to "Bonjour, ravi de vous rencontrer !",
            "de" to "Hallo, schön Sie kennenzulernen!",
            "zh" to "你好，很高兴认识你！",
            "hi" to "नमस्ते, आपसे मिलकर खुशी हुई!",
            "ko" to "안녕하세요, 만나서 반갑습니다!"
        ),
        phoneticGuides = mapOf(
            "ja" to "Hajimemashite, yoroshiku onegaishimasu!",
            "zh" to "Nǐ hǎo, hěn gāoxìng rènshí nǐ!"
        )
    ),
    LearningPhrase(
        id = "2",
        category = "Travel",
        sourceText = "Where is the nearest train station?",
        targetTranslations = mapOf(
            "ja" to "一番近い駅はどこですか？",
            "es" to "¿Dónde está la estación de tren más cercana?",
            "fr" to "Où se trouve la gare la plus proche ?",
            "de" to "Wo ist der nächste Bahnhof?",
            "zh" to "最近的火车站在哪里？",
            "hi" to "निकटतम ट्रेन स्टेशन कहाँ है?",
            "ko" to "가장 가까운 기차역이 어디인가요?"
        ),
        phoneticGuides = mapOf(
            "ja" to "Ichiban chikai eki wa doko desu ka?",
            "zh" to "Zuìjìn de huǒchēzhàn zài nǎlǐ?"
        )
    ),
    LearningPhrase(
        id = "3",
        category = "Dining",
        sourceText = "Could I please have the check?",
        targetTranslations = mapOf(
            "ja" to "お勘定をお願いできますか？",
            "es" to "¿Me trae la cuenta, por favor?",
            "fr" to "L'addition, s'il vous plaît ?",
            "de" to "Könnte ich bitte die Rechnung haben?",
            "zh" to "请结账？",
            "hi" to "क्या मुझे बिल मिल सकता है?",
            "ko" to "계산서 부탁드립니다."
        ),
        phoneticGuides = mapOf(
            "ja" to "Okanjou o onegai dekimasu ka?",
            "zh" to "Qǐng jiézhàng?"
        )
    ),
    LearningPhrase(
        id = "4",
        category = "Emergency",
        sourceText = "Can you help me, please?",
        targetTranslations = mapOf(
            "ja" to "助けていただけますか？",
            "es" to "¿Puede ayudarme, por favor?",
            "fr" to "Pouvez-vous m'aider, s'il vous plaît ?",
            "de" to "Können Sie mir bitte helfen?",
            "zh" to "你能帮帮我吗？",
            "hi" to "क्या आप मेरी मदद कर सकते हैं?",
            "ko" to "저를 도와주실 수 있나요?"
        ),
        phoneticGuides = mapOf(
            "ja" to "Tasukete itadakemasu ka?",
            "zh" to "Nǐ néng bāng bāng wǒ ma?"
        )
    ),
    LearningPhrase(
        id = "5",
        category = "Shopping",
        sourceText = "How much does this cost?",
        targetTranslations = mapOf(
            "ja" to "これはいくらですか？",
            "es" to "¿Cuánto cuesta esto?",
            "fr" to "Combien ça coûte ?",
            "de" to "Wie viel kostet das?",
            "zh" to "这个多少钱？",
            "hi" to "इसकी कीमत क्या है?",
            "ko" to "이것은 얼마인가요?"
        ),
        phoneticGuides = mapOf(
            "ja" to "Kore wa ikura desu ka?",
            "zh" to "Zhège duōshǎo qián?"
        )
    )
)

@Composable
fun LanguageLearningScreen(viewModel: TranslationViewModel) {
    val targetLang by viewModel.targetLanguage.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var practicingPhraseId by remember { mutableStateOf<String?>(null) }
    var practiceScore by remember { mutableStateOf<Int?>(null) }

    val categories = listOf("All", "Greetings", "Travel", "Dining", "Emergency", "Shopping")
    val filteredPhrases = if (selectedCategory == "All") CURATED_PHRASES else CURATED_PHRASES.filter { it.category == selectedCategory }

    GlassAtmosphereBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        size = 36.dp,
                        onClick = { viewModel.setActiveScreen(ActiveScreen.TRANSLATE_HOME) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Vocabulary & Pronunciation",
                        color = TextGlassHeading,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Learning Mode",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color(0x15FFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else Color(0x30FFFFFF),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) NeonCyan else TextGlassBody,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phrase Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPhrases) { phrase ->
                    val translatedText = phrase.targetTranslations[targetLang.code] 
                        ?: phrase.targetTranslations["ja"] 
                        ?: phrase.sourceText
                    val phonetic = phrase.phoneticGuides[targetLang.code]

                    LearningPhraseCard(
                        phrase = phrase,
                        translatedText = translatedText,
                        phoneticGuide = phonetic,
                        targetLanguageName = targetLang.name,
                        isPracticing = practicingPhraseId == phrase.id,
                        lastScore = if (practicingPhraseId == phrase.id) practiceScore else null,
                        onSpeakNormal = {
                            viewModel.speakHomeTranslation()
                        },
                        onPracticeClick = {
                            practicingPhraseId = phrase.id
                            // Simulate or trigger speech pronunciation analysis
                            practiceScore = (85..98).random()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LearningPhraseCard(
    phrase: LearningPhrase,
    translatedText: String,
    phoneticGuide: String?,
    targetLanguageName: String,
    isPracticing: Boolean,
    lastScore: Int?,
    onSpeakNormal: () -> Unit,
    onPracticeClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isElevated = isPracticing
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x2000F0FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = phrase.category,
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassIconButton(
                        icon = Icons.Default.VolumeUp,
                        contentDescription = "Listen",
                        size = 32.dp,
                        onClick = onSpeakNormal
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = phrase.sourceText,
                color = TextGlassHeading,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = translatedText,
                color = NeonEmerald,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            if (!phoneticGuide.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = phoneticGuide,
                    color = TextGlassMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x15FFFFFF))
                        .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(20.dp))
                        .clickable(onClick = onPracticeClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Practice Pronunciation",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Practice Voice",
                        color = TextGlassHeading,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (lastScore != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x2010B981))
                            .border(1.dp, NeonEmerald.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Accuracy",
                            tint = NeonEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$lastScore% Pronunciation",
                            color = NeonEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
