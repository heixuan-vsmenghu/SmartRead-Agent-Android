# SmartRead Agent Android 工程

本目录为 SmartRead Agent V0.3 Android/Kotlin 工程。

Android 工程已创建，当前版本 V0.3 已实现摘要 MVP、Agent 问答、知识卡片和复习题生成，可通过 Gradle Wrapper 构建 Debug APK。

## 当前实现

V0.3 已完成一个可运行的 Jetpack Compose Demo，功能包括：

- 文本粘贴输入。
- 3 条示例文本选择。
- 本地文本分析。
- 一句话总结。
- 关键词提取。
- 分点摘要。
- 从摘要页进入 Agent 问答页。
- Agent 快捷问题和手动输入问题。
- 本地规则型 Agent 回答。
- 知识卡片展示。
- 3 道复习题和参考答案展示。
- 清空和重新分析。

当前版本不接入云端 API，不写入任何 API Key，也不集成 LiteRT。LiteRT 端侧模型计划留到 V0.4。

## 技术栈

- Kotlin
- Android Gradle Plugin 8.9.3
- Kotlin Plugin 2.0.21
- Jetpack Compose
- Material 3
- 本地文本处理算法
- 本地规则型 Agent

## 运行方式

在本目录执行：

```powershell
.\gradlew.bat :app:assembleDebug
```

如需要重新生成干净 APK，可执行：

```powershell
.\gradlew.bat clean :app:assembleDebug
```

Debug APK 输出位置：

```text
android\app\build\outputs\apk\debug\app-debug.apk
```

已复制一份课程演示用 APK 到：

```text
release\SmartReadAgent-v0.2-debug.apk
release\SmartReadAgent-v0.3-debug.apk
```

`*.apk` 按 `.gitignore` 规则不提交到 Git 仓库。

## 验证记录

- 2026-05-12：`.\gradlew.bat :app:assembleDebug` 构建成功。
- 2026-05-12：已在 `Pixel_8` Android 模拟器中安装并启动。
- 2026-05-12：已生成首页和摘要结果页截图。
- 2026-05-12：空文本边界测试通过，页面显示输入提示且未崩溃。
- 2026-05-12：长文本边界测试通过，约 537 字文本可完成摘要、关键词和分点摘要展示。
- 2026-05-12：V0.3 `clean assembleDebug` 构建成功。
- 2026-05-12：V0.3 APK 已复制到 `release\SmartReadAgent-v0.3-debug.apk`。
- 2026-05-12：已在 `Pixel_8` Android 模拟器中验证 Agent 问答页、快捷问题、手动提问、空问题提示、知识卡片页和复习题展示。

当前已验证功能：

- 文本输入。
- 示例文本选择。
- 本地摘要生成。
- 关键词提取。
- 分点摘要展示。
- 空文本提示。
- 长文本分析。
- Agent 问答。
- 快捷问题。
- 手动问题输入。
- 知识卡片。
- 复习题生成。
- Debug APK 构建。

## 后续计划

- V0.4：LiteRT 端侧句子分析模型集成。
- V0.5：历史记录和体验优化。
