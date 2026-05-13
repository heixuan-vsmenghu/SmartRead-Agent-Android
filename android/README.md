# SmartRead Agent Android 工程

本目录为 SmartRead Agent V1.0 Android/Kotlin 工程。

Android 工程已创建，当前版本 V1.0 已实现摘要 MVP、Agent 问答、知识卡片、复习题生成、LiteRT 端侧句子重要性分析和本地历史记录，可通过 Gradle Wrapper 构建 Debug APK。

## 当前实现

V1.0 已完成一个可运行的 Jetpack Compose Demo，功能包括：

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
- LiteRT 本地模型加载。
- 文章句子重要性评分和来源展示。
- 最近 12 条历史记录保存。
- 从历史记录恢复原文并重新分析。
- 清空历史记录。
- 清空和重新分析。

当前版本不接入云端 API，不写入任何 API Key。LiteRT 模型文件位于 `app/src/main/assets/sentence_importance_model.tflite`。

## 技术栈

- Kotlin
- Android Gradle Plugin 8.9.3
- Kotlin Plugin 2.0.21
- Jetpack Compose
- Material 3
- 本地文本处理算法
- 本地规则型 Agent
- TensorFlow Lite / LiteRT 端侧推理

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
release\SmartReadAgent-v0.4-debug.apk
release\SmartReadAgent-v0.5-debug.apk
release\SmartReadAgent-v1.0-debug.apk
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
- 2026-05-12：V0.4 已加入 `org.tensorflow:tensorflow-lite:2.16.1` 依赖。
- 2026-05-12：V0.4 已将 `sentence_importance_model.tflite` 放入 Android assets。
- 2026-05-12：V0.4 `:app:assembleDebug` 构建成功，APK 已复制到 `release\SmartReadAgent-v0.4-debug.apk`。
- 2026-05-12：V0.4 APK 已安装到 `Pixel_8` 模拟器并启动，已保存 LiteRT 首页和端侧分析截图。
- 2026-05-12：V0.5 已实现 SharedPreferences 历史记录保存和历史记录页。
- 2026-05-12：V0.5 `:app:assembleDebug` 构建成功，APK 已复制到 `release\SmartReadAgent-v0.5-debug.apk`。
- 2026-05-12：V0.5 APK 已安装到 `Pixel_8` 模拟器，已验证历史记录页和恢复历史分析流程。
- 2026-05-13：V1.0 已将 Android 版本号更新为 `versionCode = 10`、`versionName = "1.0"`。
- 2026-05-13：V1.0 App 顶部文案已更新为 `V1.0 期末演示稳定版`。
- 2026-05-13：V1.0 `:app:clean :app:assembleDebug` 构建通过，APK 输出到 `android\app\build\outputs\apk\debug\app-debug.apk`，并复制为 `release\SmartReadAgent-v1.0-debug.apk`。

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
- LiteRT 端侧句子重要性评分。
- 历史记录保存和恢复。
- Debug APK 构建。

## 后续计划

- 手动补充 V1.0 App 最终截图。
- 手动补充真机安装和完整演示流程复测记录。
