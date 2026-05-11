package com.smartread.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartReadAgentApp()
        }
    }
}

data class AnalysisResult(
    val oneSentenceSummary: String,
    val keywords: List<String>,
    val bulletSummary: List<String>,
    val sentenceCount: Int,
    val characterCount: Int,
)

private data class SentenceScore(
    val sentence: String,
    val score: Int,
    val index: Int,
)

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

    fun analyze(rawText: String): AnalysisResult? {
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

        return AnalysisResult(
            oneSentenceSummary = normalizeSentence(bestSentence, maxLength = 86),
            keywords = keywords,
            bulletSummary = bullets,
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

@Composable
fun SmartReadAgentApp(modifier: Modifier = Modifier) {
    MaterialTheme(colorScheme = SmartReadColors) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SmartReadHome(modifier = modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmartReadHome(modifier: Modifier = Modifier) {
    var inputText by remember { mutableStateOf(SampleTexts.first()) }
    var analysisResult by remember { mutableStateOf(TextAnalyzer.analyze(inputText)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SmartRead Agent", fontWeight = FontWeight.Bold)
                        Text(
                            text = "V0.2 摘要 MVP",
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IntroCard()
            InputCard(
                inputText = inputText,
                errorMessage = errorMessage,
                onInputChange = {
                    inputText = it
                    errorMessage = null
                },
                onUseSample = { index ->
                    inputText = SampleTexts[index]
                    analysisResult = TextAnalyzer.analyze(SampleTexts[index])
                    errorMessage = null
                },
                onAnalyze = {
                    val result = TextAnalyzer.analyze(inputText)
                    if (result == null) {
                        analysisResult = null
                        errorMessage = "请输入一段需要分析的文本。"
                    } else {
                        analysisResult = result
                        errorMessage = null
                    }
                },
                onClear = {
                    inputText = ""
                    analysisResult = null
                    errorMessage = null
                },
            )
            analysisResult?.let { ResultSection(it) } ?: EmptyResultCard()
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
                text = "当前版本先完成文本输入、示例文本、本地摘要、关键词提取和结果展示。LiteRT 与 Agent 问答留到后续版本接入。",
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
                    Text("开始分析")
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
private fun ResultSection(result: AnalysisResult, modifier: Modifier = Modifier) {
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
                result.bulletSummary.forEachIndexed { index, item ->
                    Text("${index + 1}. $item")
                }
            }
        }
    }
}

@Composable
private fun ResultMetricRow(result: AnalysisResult, modifier: Modifier = Modifier) {
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
private fun EmptyResultCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("等待分析", fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
            Text("输入文本后点击“开始分析”，这里会显示一句话总结、关键词和分点摘要。")
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun SmartReadAgentPreview() {
    SmartReadAgentApp()
}
