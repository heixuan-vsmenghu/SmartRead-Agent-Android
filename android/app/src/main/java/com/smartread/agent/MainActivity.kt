package com.smartread.agent

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartReadAgentApp()
        }
    }
}

private enum class Screen {
    Home,
    AgentChat,
    KnowledgeCards,
    History,
}

data class ArticleAnalysis(
    val originalText: String,
    val oneSentenceSummary: String,
    val keywords: List<String>,
    val bulletSummaries: List<String>,
    val sentenceCount: Int,
    val characterCount: Int,
    val sentenceImportances: List<SentenceImportance> = emptyList(),
    val localModelStatus: String = "未运行",
)

data class SentenceImportance(
    val sentence: String,
    val score: Float,
    val level: String,
    val source: String,
    val index: Int,
)

data class ChatMessage(
    val role: MessageRole,
    val content: String,
)

enum class MessageRole {
    USER,
    AGENT,
}

data class KnowledgeCard(
    val title: String,
    val type: String,
    val content: String,
)

data class QuizQuestion(
    val question: String,
    val referenceAnswer: String,
)

data class HistoryRecord(
    val id: Long,
    val title: String,
    val preview: String,
    val savedAt: Long,
    val displayTime: String,
    val originalText: String,
    val oneSentenceSummary: String,
    val keywords: List<String>,
    val sentenceCount: Int,
    val characterCount: Int,
)

private data class SentenceScore(
    val sentence: String,
    val score: Int,
    val index: Int,
)

object HistoryRepository {
    private const val PREFS_NAME = "smartread_history"
    private const val KEY_RECORDS = "records"
    private const val MAX_RECORDS = 12
    private val timeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    }

    fun load(context: Context): List<HistoryRecord> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_RECORDS, "[]")
            .orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                array.getJSONObject(index).toHistoryRecord()
            }
        }.getOrElse { emptyList() }
    }

    fun save(context: Context, analysis: ArticleAnalysis): List<HistoryRecord> {
        val existing = load(context)
        val now = System.currentTimeMillis()
        val record = HistoryRecord(
            id = now,
            title = analysis.oneSentenceSummary.take(28).ifBlank { "未命名阅读记录" },
            preview = analysis.originalText.replace(Regex("\\s+"), " ").take(72),
            savedAt = now,
            displayTime = timeFormat.format(Date(now)),
            originalText = analysis.originalText,
            oneSentenceSummary = analysis.oneSentenceSummary,
            keywords = analysis.keywords,
            sentenceCount = analysis.sentenceCount,
            characterCount = analysis.characterCount,
        )
        val next = (listOf(record) + existing.filter { it.originalText != analysis.originalText })
            .take(MAX_RECORDS)
        persist(context, next)
        return next
    }

    fun clear(context: Context): List<HistoryRecord> {
        persist(context, emptyList())
        return emptyList()
    }

    private fun persist(context: Context, records: List<HistoryRecord>) {
        val array = JSONArray()
        records.forEach { array.put(it.toJson()) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECORDS, array.toString())
            .apply()
    }

    private fun HistoryRecord.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("title", title)
            .put("preview", preview)
            .put("savedAt", savedAt)
            .put("displayTime", displayTime)
            .put("originalText", originalText)
            .put("oneSentenceSummary", oneSentenceSummary)
            .put("keywords", JSONArray(keywords))
            .put("sentenceCount", sentenceCount)
            .put("characterCount", characterCount)
    }

    private fun JSONObject.toHistoryRecord(): HistoryRecord {
        val keywordArray = optJSONArray("keywords") ?: JSONArray()
        return HistoryRecord(
            id = optLong("id"),
            title = optString("title"),
            preview = optString("preview"),
            savedAt = optLong("savedAt"),
            displayTime = optString("displayTime"),
            originalText = optString("originalText"),
            oneSentenceSummary = optString("oneSentenceSummary"),
            keywords = List(keywordArray.length()) { index -> keywordArray.optString(index) },
            sentenceCount = optInt("sentenceCount"),
            characterCount = optInt("characterCount"),
        )
    }
}

private val SmartReadColors = lightColorScheme(
    primary = Color(0xFF1D4ED8),
    secondary = Color(0xFF047857),
    background = Color(0xFFF6F8FB),
    surface = Color.White,
    onPrimary = Color.White,
    onSurface = Color(0xFF172033),
)

private val SampleTexts = listOf(
    "软件项目管理强调在有限时间和资源下完成软件开发目标。项目负责人需要进行需求分析、任务分解、进度跟踪、风险管理和测试管理。对于小规模课程项目来说，版本控制、沟通记录、任务看板和测试反馈同样重要，它们可以体现团队协作过程，也能帮助开发者及时发现问题并调整计划。",
    "移动端 AI 应用通常需要在体验、性能和模型能力之间取得平衡。SmartRead Agent 的目标不是一次性完成复杂商业系统，而是先实现文本导入、摘要、关键词和结果展示，再逐步加入 Agent 问答、知识卡片、历史记录和 LiteRT 端侧分析能力。",
    "Jetpack Compose 是 Android 的现代声明式 UI 工具包。开发者可以用 Kotlin 描述界面状态，当状态变化时界面会自动重组。它适合构建输入、摘要结果、列表和卡片等页面，也便于在课程项目中快速完成可演示的移动端原型。",
)

private val QuickQuestions = listOf(
    "这篇文章主要讲了什么？",
    "提取这篇文章的核心关键词。",
    "帮我整理复习重点。",
    "根据文章生成 3 道复习题。",
    "生成知识卡片。",
)

object TextAnalyzer {
    private val sentenceRegex = Regex("[^。！？!?；;\\n]+[。！？!?；;]?")
    private val englishTokenRegex = Regex("[A-Za-z][A-Za-z0-9+.#-]{1,}")
    private val domainTerms = listOf(
        "SmartRead", "Agent", "Android", "Kotlin", "Compose", "LiteRT", "AI",
        "摘要", "关键词", "知识卡片", "问答", "历史记录", "文本", "阅读", "复习",
        "项目管理", "任务", "测试", "风险", "版本", "沟通", "模型", "端侧",
    )
    private val stopWords = setOf(
        "一个", "一种", "以及", "通过", "进行", "需要", "可以", "对于", "后续",
        "项目", "功能", "能力", "阶段", "当前", "完成", "实现", "提供",
    )

    fun analyze(rawText: String): ArticleAnalysis? {
        val text = rawText.trim()
        if (text.isBlank()) return null

        val sentences = splitSentences(text)
        val keywordScores = scoreKeywords(text)
        val keywords = keywordScores
            .keys
            .take(8)
            .toList()
            .ifEmpty { listOf("阅读", "摘要", "文本") }

        val rankedSentences = sentences.mapIndexed { index, sentence ->
            val keywordHit = keywords.count { sentence.contains(it, ignoreCase = true) }
            val lengthScore = when (sentence.length) {
                in 18..90 -> 3
                in 8..140 -> 2
                else -> 1
            }
            val positionScore = when (index) {
                0 -> 3
                sentences.lastIndex -> 1
                else -> 2
            }
            SentenceScore(sentence, keywordHit * 3 + lengthScore + positionScore, index)
        }

        val bestSentence = rankedSentences
            .maxWithOrNull(compareBy<SentenceScore> { it.score }.thenBy { -it.index })
            ?.sentence
            ?: text.take(80)

        val bullets = rankedSentences
            .sortedWith(compareByDescending<SentenceScore> { it.score }.thenBy { it.index })
            .take(3)
            .sortedBy { it.index }
            .map { normalizeSentence(it.sentence) }
            .ifEmpty { listOf(normalizeSentence(bestSentence)) }

        return ArticleAnalysis(
            originalText = text,
            oneSentenceSummary = normalizeSentence(bestSentence, maxLength = 86),
            keywords = keywords,
            bulletSummaries = bullets,
            sentenceCount = sentences.size,
            characterCount = text.length,
        )
    }

    private fun splitSentences(text: String): List<String> {
        return sentenceRegex.findAll(text)
            .map { normalizeSentence(it.value) }
            .filter { it.length >= 4 }
            .toList()
            .ifEmpty { listOf(normalizeSentence(text, maxLength = 120)) }
    }

    private fun scoreKeywords(text: String): LinkedHashMap<String, Int> {
        val scores = mutableMapOf<String, Int>()
        domainTerms.forEach { term ->
            val count = Regex(Regex.escape(term), RegexOption.IGNORE_CASE).findAll(text).count()
            if (count > 0) scores[term] = count * 4
        }

        englishTokenRegex.findAll(text).forEach { match ->
            val token = match.value.trim()
            if (token.length >= 2) scores[token] = (scores[token] ?: 0) + 2
        }

        text.windowed(size = 2, step = 1, partialWindows = false)
            .filter { phrase ->
                phrase.all { it in '\u4e00'..'\u9fff' } && phrase !in stopWords
            }
            .forEach { phrase ->
                scores[phrase] = (scores[phrase] ?: 0) + 1
            }

        return scores.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.length })
            .fold(linkedMapOf()) { acc, entry ->
                if (entry.key.length in 2..12 && acc.keys.none { it.contains(entry.key) || entry.key.contains(it) }) {
                    acc[entry.key] = entry.value
                }
                acc
            }
    }

    private fun normalizeSentence(sentence: String, maxLength: Int = 110): String {
        val compact = sentence
            .replace(Regex("\\s+"), " ")
            .trim()
            .trim('。', '！', '？', '!', '?', ';', '；', ',', '，')
        return if (compact.length <= maxLength) compact else compact.take(maxLength) + "..."
    }
}

object SentenceImportanceAnalyzer {
    private val sentenceRegex = Regex("[^。！？!?；;\\n]+[。！？!?；;]?")
    private val cueWords = listOf("因此", "总之", "主要", "核心", "说明", "体现", "可以看出", "关键", "重点")
    private val punctuationHints = listOf("：", ":", "；", ";")

    fun attachImportance(
        analysis: ArticleAnalysis,
        classifier: SentenceImportanceClassifier,
    ): ArticleAnalysis {
        val sentences = splitSentences(analysis.originalText)
        val importances = sentences.take(5).mapIndexed { index, sentence ->
            val features = extractSentenceFeatures(
                sentence = sentence,
                index = index,
                total = sentences.size,
                keywords = analysis.keywords,
            )
            val result = classifier.classify(features)
            SentenceImportance(
                sentence = sentence,
                score = result.score,
                level = result.level,
                source = result.source,
                index = index,
            )
        }
        val usingModel = importances.any { it.source == "LiteRT 本地模型" }
        val status = if (usingModel) {
            "推理方式：LiteRT 本地模型"
        } else if (classifier.isModelLoaded) {
            "模型已加载，但本次推理失败，已使用规则评分兜底。"
        } else {
            "当前使用规则评分兜底，模型文件未成功加载。"
        }
        return analysis.copy(
            sentenceImportances = importances,
            localModelStatus = status,
        )
    }

    private fun splitSentences(text: String): List<String> {
        return sentenceRegex.findAll(text)
            .map { it.value.replace(Regex("\\s+"), " ").trim().trim('。', '！', '？', '!', '?', ';', '；') }
            .filter { it.length >= 4 }
            .toList()
            .ifEmpty { listOf(text.trim().take(120)) }
    }

    fun extractSentenceFeatures(
        sentence: String,
        index: Int,
        total: Int,
        keywords: List<String>,
    ): FloatArray {
        val lengthNorm = (sentence.length / 90f).coerceIn(0f, 1f)
        val keywordBase = keywords.take(8).ifEmpty { listOf("文本") }
        val keywordHits = keywordBase.count { sentence.contains(it, ignoreCase = true) }
        val keywordOverlap = (keywordHits / keywordBase.size.toFloat()).coerceIn(0f, 1f)
        val positionScore = if (total <= 1) {
            1f
        } else {
            (1f - index / (total - 1).toFloat() * 0.65f).coerceIn(0.35f, 1f)
        }
        val punctuationScore = when {
            punctuationHints.any { sentence.contains(it) } -> 1f
            sentence.contains("，") || sentence.contains(",") -> 0.35f
            else -> 0.1f
        }
        val cueScore = if (cueWords.any { sentence.contains(it) }) 1f else 0f
        return floatArrayOf(lengthNorm, keywordOverlap, positionScore, punctuationScore, cueScore)
    }
}

object LocalReadingAgent {
    fun answerQuestion(question: String, analysis: ArticleAnalysis?): String {
        val cleanedQuestion = question.trim()
        if (analysis == null) {
            return "请先输入文章并完成摘要分析，再使用 Agent 问答功能。"
        }
        if (cleanedQuestion.isBlank()) {
            return "请输入一个想围绕文章提问的问题。"
        }

        val bullets = analysis.bulletSummaries.toNumberedText()
        val keywords = analysis.keywords.joinToString("、")

        return when {
            cleanedQuestion.containsAny("讲了什么", "主要内容", "总结", "概括") -> {
                """
                这篇文章可以概括为：${analysis.oneSentenceSummary}

                分点理解：
                $bullets
                """.trimIndent()
            }

            cleanedQuestion.containsAny("关键词", "重点词", "核心词") -> {
                "这篇文章的核心关键词包括：$keywords。"
            }

            cleanedQuestion.containsAny("重点", "核心观点", "知识点") -> {
                """
                可以把以下内容作为复习重点：
                $bullets

                这些内容覆盖了文章中的核心信息，适合整理到课堂笔记或复习提纲中。
                """.trimIndent()
            }

            cleanedQuestion.containsAny("复习", "考试", "题目", "练习题") -> {
                generateQuizQuestions(analysis)
                    .mapIndexed { index, item ->
                        "${index + 1}. ${item.question}\n参考答案：${item.referenceAnswer}"
                    }
                    .joinToString("\n\n")
            }

            cleanedQuestion.containsAny("卡片", "知识卡片") -> {
                "已根据文章摘要生成知识卡片，可在知识卡片页面查看。"
            }

            else -> {
                """
                根据当前文章内容，可以从以下几个方面理解：

                一句话总结：${analysis.oneSentenceSummary}
                关键词：$keywords
                分点摘要：
                $bullets
                """.trimIndent()
            }
        }
    }

    fun generateKnowledgeCards(analysis: ArticleAnalysis?): List<KnowledgeCard> {
        if (analysis == null) return emptyList()

        val keywordCards = analysis.keywords.take(3).map { keyword ->
            KnowledgeCard(
                title = keyword,
                type = "概念",
                content = "该关键词是理解文章内容的重要入口，可结合摘要内容进行复习。",
            )
        }

        val summaryCard = KnowledgeCard(
            title = "核心观点",
            type = "核心观点",
            content = analysis.oneSentenceSummary,
        )

        val reviewCards = analysis.bulletSummaries.take(3).mapIndexed { index, bullet ->
            KnowledgeCard(
                title = "复习要点 ${index + 1}",
                type = "复习",
                content = bullet,
            )
        }

        return listOf(summaryCard) + keywordCards + reviewCards
    }

    fun generateQuizQuestions(analysis: ArticleAnalysis?): List<QuizQuestion> {
        if (analysis == null) return emptyList()

        val firstBullet = analysis.bulletSummaries.firstOrNull() ?: analysis.oneSentenceSummary
        return listOf(
            QuizQuestion(
                question = "这篇文章的主要内容是什么？",
                referenceAnswer = analysis.oneSentenceSummary,
            ),
            QuizQuestion(
                question = "文章中最重要的关键词有哪些？",
                referenceAnswer = analysis.keywords.take(5).joinToString("、"),
            ),
            QuizQuestion(
                question = "请概括文章中的一个核心观点。",
                referenceAnswer = firstBullet,
            ),
        )
    }

    private fun String.containsAny(vararg terms: String): Boolean {
        return terms.any { contains(it, ignoreCase = true) }
    }

    private fun List<String>.toNumberedText(): String {
        return take(4).mapIndexed { index, item -> "${index + 1}. $item" }.joinToString("\n")
    }
}

fun extractSentenceFeatures(
    sentence: String,
    index: Int,
    total: Int,
    keywords: List<String>,
): FloatArray {
    return SentenceImportanceAnalyzer.extractSentenceFeatures(sentence, index, total, keywords)
}

@Composable
fun SmartReadAgentApp(modifier: Modifier = Modifier) {
    MaterialTheme(colorScheme = SmartReadColors) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SmartReadScaffold(modifier = modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmartReadScaffold(modifier: Modifier = Modifier) {
    val appContext = LocalContext.current.applicationContext
    val classifier = remember { SentenceImportanceClassifier(appContext) }
    DisposableEffect(classifier) {
        onDispose { classifier.close() }
    }

    fun analyzeWithImportance(text: String): ArticleAnalysis? {
        return TextAnalyzer.analyze(text)?.let { result ->
            SentenceImportanceAnalyzer.attachImportance(result, classifier)
        }
    }

    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var inputText by remember { mutableStateOf(SampleTexts.first()) }
    var analysis by remember { mutableStateOf(analyzeWithImportance(inputText)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var historyRecords by remember { mutableStateOf(HistoryRepository.load(appContext)) }

    fun acceptAnalysis(result: ArticleAnalysis) {
        analysis = result
        historyRecords = HistoryRepository.save(appContext, result)
        errorMessage = null
    }

    fun openHistoryRecord(record: HistoryRecord) {
        inputText = record.originalText
        val result = analyzeWithImportance(record.originalText)
        analysis = result
        errorMessage = null
        currentScreen = Screen.Home
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SmartRead Agent", fontWeight = FontWeight.Bold)
                        Text(
                            text = "V0.5 历史记录与体验优化",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        when (currentScreen) {
            Screen.Home -> HomeScreen(
                inputText = inputText,
                analysis = analysis,
                errorMessage = errorMessage,
                onInputChange = {
                    inputText = it
                    errorMessage = null
                },
                onUseSample = { index ->
                    inputText = SampleTexts[index]
                    analyzeWithImportance(SampleTexts[index])?.let(::acceptAnalysis)
                },
                onAnalyze = {
                    val result = analyzeWithImportance(inputText)
                    if (result == null) {
                        analysis = null
                        errorMessage = "请先输入一段需要分析的文本。"
                    } else {
                        acceptAnalysis(result)
                    }
                },
                onClear = {
                    inputText = ""
                    analysis = null
                    errorMessage = null
                },
                onOpenAgent = { currentScreen = Screen.AgentChat },
                onOpenCards = { currentScreen = Screen.KnowledgeCards },
                onOpenHistory = { currentScreen = Screen.History },
                modifier = contentModifier,
            )

            Screen.AgentChat -> AgentChatScreen(
                analysis = analysis,
                onBack = { currentScreen = Screen.Home },
                modifier = contentModifier,
            )

            Screen.KnowledgeCards -> KnowledgeCardsScreen(
                analysis = analysis,
                onBack = { currentScreen = Screen.Home },
                modifier = contentModifier,
            )

            Screen.History -> HistoryScreen(
                records = historyRecords,
                onBack = { currentScreen = Screen.Home },
                onOpenRecord = ::openHistoryRecord,
                onClearHistory = {
                    historyRecords = HistoryRepository.clear(appContext)
                },
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun HomeScreen(
    inputText: String,
    analysis: ArticleAnalysis?,
    errorMessage: String?,
    onInputChange: (String) -> Unit,
    onUseSample: (Int) -> Unit,
    onAnalyze: () -> Unit,
    onClear: () -> Unit,
    onOpenAgent: () -> Unit,
    onOpenCards: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IntroCard()
        InputCard(
            inputText = inputText,
            errorMessage = errorMessage,
            onInputChange = onInputChange,
            onUseSample = onUseSample,
            onAnalyze = onAnalyze,
            onClear = onClear,
        )
        if (analysis == null) {
            EmptyResultCard(
                onOpenAgent = onOpenAgent,
                onOpenCards = onOpenCards,
                onOpenHistory = onOpenHistory,
            )
        } else {
            ResultSection(
                result = analysis,
                onOpenAgent = onOpenAgent,
                onOpenCards = onOpenCards,
                onOpenHistory = onOpenHistory,
            )
        }
    }
}

@Composable
private fun IntroCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "AI 智读助手",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A),
            )
            Text(
                text = "当前版本支持文本摘要、关键词提取、Agent 问答、知识卡片、复习题生成、LiteRT 端侧评分和历史记录。",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF334155),
            )
        }
    }
}

@Composable
private fun InputCard(
    inputText: String,
    errorMessage: String?,
    onInputChange: (String) -> Unit,
    onUseSample: (Int) -> Unit,
    onAnalyze: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("输入或选择示例文本", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp),
                label = { Text("待分析文本") },
                placeholder = { Text("粘贴教材、文章或课堂资料片段") },
                supportingText = {
                    Text(errorMessage ?: "建议输入 100 字以上，摘要效果更明显。")
                },
                isError = errorMessage != null,
                minLines = 7,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SampleTexts.indices.forEach { index ->
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onUseSample(index) },
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "示例 ${index + 1}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onAnalyze,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("开始智能分析")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onClear,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("清空")
                }
            }
        }
    }
}

@Composable
private fun ResultSection(
    result: ArticleAnalysis,
    onOpenAgent: () -> Unit,
    onOpenCards: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ResultMetricRow(result)
        ResultCard(title = "一句话总结") {
            Text(result.oneSentenceSummary)
        }
        ResultCard(title = "关键词") {
            Text(result.keywords.joinToString(" / "))
        }
        ResultCard(title = "分点摘要") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                result.bulletSummaries.forEachIndexed { index, item ->
                    Text("${index + 1}. $item")
                }
            }
        }
        LiteRtAnalysisCard(result)
        ResultActionCard(
            onOpenAgent = onOpenAgent,
            onOpenCards = onOpenCards,
            onOpenHistory = onOpenHistory,
        )
    }
}

@Composable
private fun LiteRtAnalysisCard(
    result: ArticleAnalysis,
    modifier: Modifier = Modifier,
) {
    ResultCard(title = "LiteRT 端侧分析", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = result.localModelStatus,
                style = MaterialTheme.typography.bodySmall,
                color = if (result.localModelStatus.contains("LiteRT")) {
                    Color(0xFF166534)
                } else {
                    Color(0xFF9A3412)
                },
                fontWeight = FontWeight.Bold,
            )
            if (result.sentenceImportances.isEmpty()) {
                Text("暂无可分析句子。")
            } else {
                result.sentenceImportances.forEach { item ->
                    SentenceImportanceItem(item)
                }
            }
        }
    }
}

@Composable
private fun SentenceImportanceItem(
    item: SentenceImportance,
    modifier: Modifier = Modifier,
) {
    val percent = (item.score * 100f).toInt().coerceIn(0, 100)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "第 ${item.index + 1} 句",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF166534),
            )
            Text(
                text = "${item.level} · $percent%",
                fontWeight = FontWeight.Bold,
                color = when (item.level) {
                    "高" -> Color(0xFFB91C1C)
                    "中" -> Color(0xFFB45309)
                    else -> Color(0xFF047857)
                },
            )
        }
        Text(item.sentence)
        Text(
            text = "来源：${item.source}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
        )
    }
}

@Composable
private fun ResultActionCard(
    onOpenAgent: () -> Unit,
    onOpenCards: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("智能阅读扩展", fontWeight = FontWeight.Bold, color = Color(0xFF166534))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAgent,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("问问 Agent")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenCards,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("生成知识卡片")
                }
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenHistory,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("查看历史记录")
            }
        }
    }
}

@Composable
private fun AgentChatScreen(
    analysis: ArticleAnalysis?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    var messages by remember(analysis?.originalText) {
        mutableStateOf(
            listOf(
                ChatMessage(
                    role = MessageRole.AGENT,
                    content = if (analysis == null) {
                        "请先输入文章并完成摘要分析，再使用 Agent 问答功能。"
                    } else {
                        "可以围绕当前文章提问。我会根据摘要、关键词和分点摘要进行本地规则型回答。"
                    },
                ),
            ),
        )
    }

    fun sendQuestion(question: String) {
        val cleaned = question.trim()
        val answer = LocalReadingAgent.answerQuestion(cleaned, analysis)
        messages = if (cleaned.isBlank()) {
            messages + ChatMessage(MessageRole.AGENT, answer)
        } else {
            messages + ChatMessage(MessageRole.USER, cleaned) + ChatMessage(MessageRole.AGENT, answer)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PageHeader(
            title = "Agent 问答",
            subtitle = "基于当前文章内容进行提问",
            onBack = onBack,
        )
        CurrentSummaryCard(analysis)
        QuickQuestionSection(
            questions = QuickQuestions,
            onQuestionClick = ::sendQuestion,
        )
        ChatHistory(messages = messages)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入问题") },
                    placeholder = { Text("例如：帮我整理复习重点") },
                    minLines = 2,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        sendQuestion(draft)
                        draft = ""
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("发送问题")
                }
            }
        }
    }
}

@Composable
private fun KnowledgeCardsScreen(
    analysis: ArticleAnalysis?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = LocalReadingAgent.generateKnowledgeCards(analysis)
    val quizQuestions = LocalReadingAgent.generateQuizQuestions(analysis)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PageHeader(
            title = "知识卡片",
            subtitle = "根据文章摘要和关键词生成",
            onBack = onBack,
        )

        if (cards.isEmpty()) {
            FriendlyPromptCard("请先完成文章摘要分析，再使用该功能。")
        } else {
            Text("知识卡片", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            cards.forEach { card ->
                KnowledgeCardItem(card)
            }
            QuizSection(quizQuestions)
        }
    }
}

@Composable
private fun HistoryScreen(
    records: List<HistoryRecord>,
    onBack: () -> Unit,
    onOpenRecord: (HistoryRecord) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PageHeader(
            title = "历史记录",
            subtitle = "最近 ${records.size} 条阅读分析",
            onBack = onBack,
        )
        if (records.isEmpty()) {
            FriendlyPromptCard("还没有历史记录。完成一次智能分析后，这里会保存最近的阅读结果。")
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = onClearHistory,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("清空历史")
                }
            }
            records.forEach { record ->
                HistoryRecordItem(
                    record = record,
                    onOpenRecord = onOpenRecord,
                )
            }
        }
    }
}

@Composable
private fun HistoryRecordItem(
    record: HistoryRecord,
    onOpenRecord: (HistoryRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = record.title,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TypeLabel(record.displayTime)
            }
            Text(record.oneSentenceSummary)
            Text(
                text = record.preview,
                color = Color(0xFF64748B),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "关键词：${record.keywords.take(5).joinToString(" / ")}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF475569),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricCard(
                    label = "字符数",
                    value = record.characterCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    label = "句子数",
                    value = record.sentenceCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenRecord(record) },
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("恢复这次分析")
            }
        }
    }
}

@Composable
private fun PageHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onBack) {
                Text("返回摘要页")
            }
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, color = Color(0xFF475569))
        }
    }
}

@Composable
private fun CurrentSummaryCard(analysis: ArticleAnalysis?, modifier: Modifier = Modifier) {
    if (analysis == null) {
        FriendlyPromptCard(
            text = "请先完成摘要分析，再使用该功能。",
            modifier = modifier,
        )
    } else {
        ResultCard(title = "当前文章摘要", modifier = modifier) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(analysis.oneSentenceSummary)
                Text("关键词：${analysis.keywords.take(6).joinToString(" / ")}")
            }
        }
    }
}

@Composable
private fun QuickQuestionSection(
    questions: List<String>,
    onQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("快捷问题", fontWeight = FontWeight.Bold)
            questions.forEach { question ->
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onQuestionClick(question) },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(question)
                }
            }
        }
    }
}

@Composable
private fun ChatHistory(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("聊天记录", fontWeight = FontWeight.Bold)
            messages.forEach { message ->
                ChatBubble(message)
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) Color(0xFFDBEAFE) else Color(0xFFF8FAFC)
    val arrangement = if (isUser) Arrangement.End else Arrangement.Start
    val label = if (isUser) "我的问题" else "Agent 回答"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = arrangement,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
        ) {
            SelectionContainer {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Text(message.content)
                }
            }
        }
    }
}

@Composable
private fun KnowledgeCardItem(card: KnowledgeCard, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = card.title,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    TypeLabel(card.type)
                }
                Text(card.content)
                Text(
                    text = "来源：由当前文本摘要生成",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                )
            }
        }
    }
}

@Composable
private fun TypeLabel(type: String, modifier: Modifier = Modifier) {
    Text(
        text = type,
        modifier = modifier
            .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = Color(0xFF075985),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun QuizSection(questions: List<QuizQuestion>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("复习题", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        questions.forEachIndexed { index, question ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("简答题 ${index + 1}", fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                    Text(question.question)
                    Text("参考答案：${question.referenceAnswer}", color = Color(0xFF475569))
                }
            }
        }
    }
}

@Composable
private fun ResultMetricRow(result: ArticleAnalysis, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MetricCard(
            label = "字符数",
            value = result.characterCount.toString(),
            modifier = Modifier.weight(1f),
        )
        MetricCard(
            label = "句子数",
            value = result.sentenceCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5F0)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(label, color = Color(0xFF47635A), style = MaterialTheme.typography.bodySmall)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun ResultCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(title, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun EmptyResultCard(
    onOpenAgent: () -> Unit,
    onOpenCards: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("等待分析", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
            Text("输入文本后点击“开始智能分析”，这里会显示一句话总结、关键词和分点摘要。")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAgent,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("问问 Agent")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onOpenCards,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("知识卡片")
                }
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenHistory,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("查看历史记录")
            }
        }
    }
}

@Composable
private fun FriendlyPromptCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            color = Color(0xFF9A3412),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun SmartReadAgentPreview() {
    SmartReadAgentApp()
}
