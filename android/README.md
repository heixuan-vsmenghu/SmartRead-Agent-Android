# SmartRead Agent Android 工程

本目录为 SmartRead Agent V0.2 Android/Kotlin 摘要 MVP 工程。

## 当前实现

V0.2 已完成一个可运行的 Jetpack Compose Demo，功能包括：

- 文本粘贴输入。
- 3 条示例文本选择。
- 本地文本分析。
- 一句话总结。
- 关键词提取。
- 分点摘要。
- 清空和重新分析。

当前版本不接入云端 API，不写入任何 API Key，也不集成 LiteRT。LiteRT 端侧模型计划留到 V0.4。

## 技术栈

- Kotlin
- Android Gradle Plugin 8.9.3
- Kotlin Plugin 2.0.21
- Jetpack Compose
- Material 3
- 本地文本处理算法

## 运行方式

在本目录执行：

```powershell
.\gradlew.bat :app:assembleDebug
```

Debug APK 输出位置：

```text
android\app\build\outputs\apk\debug\app-debug.apk
```

已复制一份课程演示用 APK 到：

```text
release\SmartReadAgent-v0.2-debug.apk
```

`*.apk` 按 `.gitignore` 规则不提交到 Git 仓库。

## 验证记录

- 2026-05-12：`.\gradlew.bat :app:assembleDebug` 构建成功。
- 2026-05-12：已在 `Pixel_8` Android 模拟器中安装并启动。
- 2026-05-12：已生成首页和摘要结果页截图。
- 2026-05-12：空文本边界测试通过，页面显示输入提示且未崩溃。
- 2026-05-12：长文本边界测试通过，约 537 字文本可完成摘要、关键词和分点摘要展示。

## 后续计划

- V0.3：Agent 问答与知识卡片。
- V0.4：LiteRT 端侧句子分析模型集成。
- V0.5：历史记录和体验优化。
