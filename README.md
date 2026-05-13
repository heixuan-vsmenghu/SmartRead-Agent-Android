# SmartRead Agent：AI 智读助手

## V1.0 期末演示稳定版

SmartRead Agent 当前进入 V1.0 期末演示稳定版。本阶段功能范围已经冻结，不再新增云端大模型 API、用户登录、OCR、多设备同步、后端服务器、复杂数据库和语音输入等大功能，重点完成构建验证、APK 归档、测试记录、演示文档和双课程最终交付整理。

V1.0 保留并稳定以下功能：文本输入与示例文本、本地摘要、关键词提取、分点摘要、LiteRT 端侧句子重要性分析、Agent 问答、知识卡片、复习题、历史记录保存、历史记录恢复和清空历史记录。

V1.0 Android 版本信息：

```text
versionCode = 10
versionName = "1.0"
```

V1.0 课程演示 APK 路径：

```text
release\SmartReadAgent-v1.0-debug.apk
H:\福建师范大学\大三下\软件实践研发（3）\期末大作业_SmartReadAgent\release\SmartReadAgent-v1.0-debug.apk
```

APK 说明与安装文档：

- `release\APK信息说明.md`：记录 APK 类型、版本、大小、SHA256 和适用范围。
- `release\Android手机安装说明.md`：说明安卓手机安装步骤、推荐测试流程和常见问题。
- `release\v1.0_release_notes.md`：用于 Gitee/GitHub Release 的发布说明。

说明：`SmartReadAgent-v1.0-debug.apk` 是课程演示用 debug APK，可用于课程验收、真机测试和现场演示，不作为应用商店正式商业发布版本。

## 项目简介

SmartRead Agent 是一款 Android/Kotlin 移动端 AI 阅读助手，面向学生阅读教材、文章、课堂资料时“信息量大、理解成本高、复习整理耗时”的问题，提供文本导入、自动摘要、关键词提取、知识卡片、Agent 问答、LiteRT 端侧句子分析和历史记录等功能。

## 双课程交付关系

本项目同时服务两门课程的大作业：

- 《软件开发实践（三）》：完成 Android/Kotlin 移动端 AI App、源码、项目说明文档、演示材料、截图和 APK。
- 《软件项目管理》：围绕同一个项目整理版本管理、开发约定、沟通记录、项目管理平台截图、测试与 Bug 记录、个人项目管理报告等材料。

## 项目背景

在课程学习和资料阅读过程中，学生经常需要阅读较长的教材片段、技术文章、课堂讲义或报告资料。单纯人工整理摘要和复习要点耗时较长，也容易遗漏重点。SmartRead Agent 希望通过移动端 App 提供轻量的 AI 阅读辅助能力，让用户可以快速获得文本摘要、关键词、知识卡片和复习题。

## 项目目标

- 完成一个可演示的 Android/Kotlin 阅读辅助 App。
- 实现文本输入、示例文本、本地摘要、关键词提取和结果展示等 MVP 功能。
- 持续完善 Agent 问答、知识卡片、复习题、历史记录和 LiteRT 端侧分析能力。
- 保留完整的项目管理证据链，便于课程提交和期末展示。

## 核心功能

- 文本导入：支持用户粘贴待分析文本。
- 示例文本选择：提供内置示例，便于课堂演示。
- 一句话总结：快速生成文章主旨。
- 分点摘要：提取文章重点内容。
- 关键词提取：展示文章主题词。
- 知识卡片生成：将重点整理成复习卡片。
- Agent 问答：围绕导入文本进行问答。
- 复习题生成：根据文本生成简单复习题。
- 历史记录：保存用户分析过的内容和结果。
- LiteRT 端侧句子分析：对文章前 5 句进行本地句子重要性评分。

## 技术路线

当前技术路线为 Android/Kotlin + Jetpack Compose + 本地文本处理算法 + 本地规则型 Agent + LiteRT 端侧模型 + 可选云端 Agent 增强。V0.4 阶段已完成轻量句子重要性模型训练、`.tflite` 导出、Android assets 集成和摘要结果页展示。

## 目录结构说明

- `android/`：Android/Kotlin 工程，V0.4 已实现摘要 MVP、Agent 问答、知识卡片、复习题和 LiteRT 端侧分析。
- `model/`：模型训练脚本、Notebook、导出的 LiteRT 模型和标签文件。
- `docs/`：项目需求、技术方案、阶段计划、演示和课程说明文档。
- `docs/project-management/`：项目管理公共材料，可同步用于《软件项目管理》课程。
- `screenshots/`：后续保存 App、模型、仓库和项目管理截图。
- `demo/`：后续保存演示视频或演示素材。
- `release/`：后续保存 APK、发布说明和最终版本材料。

## 阶段计划

- v0.1 项目立项与目录初始化。
- v0.2 文本导入与摘要 MVP。
- v0.3 Agent 问答与知识卡片。
- v0.4 LiteRT 端侧模型集成。
- v0.5 历史记录与体验优化。
- v1.0 期末演示稳定版。

## 初步分工

- 成员 A：项目负责人、Android 核心开发、AI 功能设计、LiteRT 模型集成、文档和演示整理。
- 成员 B：需求反馈、功能体验测试、UI 使用建议、文档校对、演示配合。

本项目为双人小组项目，分工以真实参与情况为准，不夸大组员代码贡献。

## 当前进度

- 已确定项目选题和项目名称。
- 已确定双课程共用同一个项目。
- 已完成 V0.1 目录和文档骨架初始化。
- Gitee 主仓库已创建并完成首次推送。
- GitHub 镜像仓库已创建并完成首次推送。
- v0.1 标签已创建并推送到 Gitee 与 GitHub。
- V0.2 Android/Kotlin 摘要 MVP 已完成本地构建。
- 已在 Pixel_8 Android 模拟器中安装并启动 V0.2 Demo。
- 已生成首页、摘要结果页、空文本提示和长文本测试截图。
- 已完成空文本与长文本边界测试记录。
- 已在 Leangoo 创建项目管理看板并录入阶段任务。
- 已生成 Leangoo 项目首页、任务看板和任务分配截图。
- 已完成 Leangoo V0.2 任务状态更新，并保存状态更新截图。
- 已补充 QQ V0.2 测试反馈截图索引。
- V0.2 阶段验收归档已完成。
- V0.3 Agent 问答页、知识卡片页和复习题生成功能已实现。
- V0.3 `clean assembleDebug` 构建通过，已生成 `SmartReadAgent-v0.3-debug.apk`。
- 已在 Pixel_8 Android 模拟器中完成 V0.3 入口、快捷问题、手动提问、空问题提示、知识卡片和复习题验证。
- 已生成 V0.3 App 截图并更新测试用例记录。
- V0.4 已完成句子重要性训练脚本、Notebook 和真实 `.tflite` 模型导出。
- V0.4 已将 `sentence_importance_model.tflite` 集成到 Android assets，并在摘要结果页展示 LiteRT 端侧分析结果。
- V0.5 已实现本地历史记录保存、历史记录页、恢复历史分析和清空历史功能。

## 后续计划

- 后续补充安卓真机体验测试反馈、Gitee Release 上传截图和课程平台提交截图。
- 根据真实测试结果补充 Bug 记录、截图和项目报告。

## 运行说明

Android 工程位于：

```text
android\
```

构建命令：

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

Debug APK 输出位置：

```text
android\app\build\outputs\apk\debug\app-debug.apk
```

课程演示用 APK 副本：

```text
release\SmartReadAgent-v0.2-debug.apk
release\SmartReadAgent-v0.3-debug.apk
release\SmartReadAgent-v0.4-debug.apk
release\SmartReadAgent-v0.5-debug.apk
release\SmartReadAgent-v1.0-debug.apk
```

完整路径：

```text
H:\福建师范大学\大三下\软件实践研发（3）\期末大作业_SmartReadAgent\release\SmartReadAgent-v0.2-debug.apk
H:\福建师范大学\大三下\软件实践研发（3）\期末大作业_SmartReadAgent\release\SmartReadAgent-v0.3-debug.apk
H:\福建师范大学\大三下\软件实践研发（3）\期末大作业_SmartReadAgent\release\SmartReadAgent-v0.4-debug.apk
H:\福建师范大学\大三下\软件实践研发（3）\期末大作业_SmartReadAgent\release\SmartReadAgent-v0.5-debug.apk
H:\福建师范大学\大三下\软件实践研发（3）\期末大作业_SmartReadAgent\release\SmartReadAgent-v1.0-debug.apk
```

说明：APK 文件按 `.gitignore` 规则不提交到 Git 仓库。

## 截图占位

- App 首页截图：`首页_20260512.png`。
- 摘要结果页截图：`摘要结果页_20260512.png`。
- 空文本提示截图：`空文本提示_20260512.png`。
- 长文本输入截图：`长文本输入_20260512.png`。
- 长文本摘要结果截图：`长文本摘要结果_20260512.png`。
- 长文本摘要详情截图：`长文本摘要详情_20260512.png`。
- 摘要页入口按钮截图：`摘要页入口按钮_20260512.png`。
- Agent 问答页截图：`Agent问答页_20260512.png`。
- Agent 快捷问题截图：`Agent快捷问题_20260512.png`。
- Agent 手动提问截图：`Agent手动提问_20260512.png`。
- Agent 空问题提示截图：`Agent空问题提示_20260512.png`。
- 知识卡片页截图：`知识卡片页_20260512.png`。
- 复习题展示截图：`复习题展示_20260512.png`。
- LiteRT 首页启动截图：`LiteRT首页启动_20260512.png`。
- LiteRT 分析结果截图：`LiteRT端侧分析_20260512.png`。
- V0.4 摘要结果页截图：`V0.4摘要结果页_20260512.png`。
- LiteRT 句子重要性结果截图：`LiteRT句子重要性结果_20260512.png`。
- V0.4 Agent 功能回归截图：`V0.4_Agent功能回归_20260512.png`。
- V0.4 知识卡片功能回归截图：`V0.4知识卡片功能回归_20260512.png`。
- V0.5 历史记录页截图：`V0.5历史记录页_20260512.png`。
- V0.5 历史记录恢复截图：`V0.5历史记录恢复_20260512.png`。
- V1.0 首页截图：`V1.0首页_20260512.png`。
- V1.0 摘要结果截图：`V1.0摘要结果_20260512.png`。
- V1.0 LiteRT 端侧分析截图：`V1.0LiteRT端侧分析_20260512.png`。
- V1.0 Agent 问答截图：`V1.0Agent问答_20260512.png`。
- V1.0 知识卡片截图：`V1.0知识卡片_20260512.png`。
- V1.0 历史记录截图：`V1.0历史记录页_20260512.png`、`V1.0历史记录恢复_20260512.png`、`V1.0清空历史记录_20260512.png`。

## 版本记录占位

| 版本 | 日期 | 内容 | 备注 |
|---|---|---|---|
| v0.1 | 2026-05-11 | 项目立项与目录初始化 | 已完成 |
| v0.2 | 2026-05-12 | 文本导入、示例文本、本地摘要、关键词提取、分点摘要、APK 构建和边界测试 | 已完成验收归档 |
| v0.3 | 2026-05-12 | Agent 问答、知识卡片、复习题生成、APK 构建和模拟器测试 | 已完成 |
| v0.4 | 2026-05-12 | LiteRT 端侧句子重要性模型、Android assets 集成、APK 构建和模拟器截图验证 | 已完成 |
| v0.5 | 2026-05-12 | 本地历史记录、历史页、恢复历史分析、APK 构建和模拟器截图验证 | 已完成初步验证 |
| v1.0 | 2026-05-13 | 期末演示稳定版，冻结功能范围，更新版本号，整理最终 APK、测试记录、项目说明、演示脚本和双课程提交材料 | 收口完成后发布 |
