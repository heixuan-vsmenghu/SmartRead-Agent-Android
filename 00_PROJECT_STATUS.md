# SmartRead Agent 项目状态

## 当前版本

v1.0 期末演示稳定版。

## 当前日期

2026-05-13。

## 远程仓库

- Gitee 主仓库：https://gitee.com/black-xuan-club/smart-read-agent-android
- GitHub 镜像仓库：https://github.com/heixuan-vsmenghu/SmartRead-Agent-Android

## 当前分支

- `main`：稳定版本分支，V0.3 验证通过后合并。
- `dev`：日常开发分支，V0.3 验证通过后合并。
- `feature/agent-cards`：V0.3 Agent 问答与知识卡片功能分支。
- `feature/litert-sentence-model`：V0.4 LiteRT 端侧句子分析功能分支。
- `feature/history-records`：V0.5 历史记录与体验优化功能分支。
- `feature/final-release-v1.0`：V1.0 期末演示稳定版收尾分支。

## 最近一次提交

- 初始化提交：`docs: 初始化SmartRead Agent项目立项材料`
- 收口提交：`docs: 补充V0.1远程仓库与项目管理索引`
- V0.2 功能提交：`feat: 完成V0.2文本导入与摘要MVP`
- V0.2 标签：`v0.2`
- V0.2 验收归档提交：`docs: 完成V0.2验收归档并准备V0.3设计`
- V0.3 功能提交：`feat: 完成V0.3 Agent问答与知识卡片功能`
- V0.3 标签：`v0.3`

## 当前阶段状态

V0.1 已完成，V0.2 摘要 MVP 已完成验收归档，V0.3 Agent 问答与知识卡片功能已完成代码开发和模拟器初步验证，V0.4 LiteRT 端侧句子分析集成已完成构建和模拟器初步验证，V0.5 历史记录与体验优化已完成。当前进入 V1.0 期末演示稳定版收口阶段，功能范围冻结，重点整理最终 APK、测试记录、项目说明文档、演示材料和双课程提交包。

已完成内容：

- 项目目录初始化。
- Markdown 文档骨架创建。
- Git 初始化。
- `main` / `dev` 分支创建。
- 本地首次 commit。
- Gitee/GitHub 远程仓库地址记录。
- Gitee 首次推送完成。
- GitHub 首次推送完成。
- `v0.1` 标签已创建并推送到 Gitee 与 GitHub。
- 本地项目目录截图和 QQ 立项截图索引已补充。
- Android/Kotlin Compose 工程已创建。
- 文本输入、示例文本、本地摘要、关键词提取和摘要结果展示已实现。
- Debug APK 构建成功。
- APK 路径：`H:\福建师范大学\大三下\软件实践研发（3）\期末大作业_SmartReadAgent\release\SmartReadAgent-v0.2-debug.apk`。
- 已在 Pixel_8 Android 模拟器中安装并启动。
- App 首页和摘要结果页截图已生成。
- 空文本边界测试已完成，提示信息正常。
- 长文本边界测试已完成，约 537 字文本可正常分析。
- Leangoo 项目管理看板已创建。
- Leangoo 项目首页、任务看板和任务分配截图已生成。
- Leangoo V0.2 任务状态已更新，搭建 Android Kotlin 项目、设计首页 UI、文本导入、示例文本、本地摘要、关键词提取、摘要结果页面和第一轮功能测试等任务已归入“已完成”。
- QQ V0.2 测试反馈截图已补充，并在项目管理材料中建立索引。
- V0.2 最新提交已推送到 Gitee 和 GitHub。
- `v0.2` 标签已推送到 Gitee 和 GitHub。
- V0.3 Agent 问答与知识卡片设计文档已创建。
- V0.3 已实现从摘要页进入 Agent 问答页和知识卡片页。
- V0.3 已实现快捷问题、手动输入问题、本地规则型回答和空问题提示。
- V0.3 已实现知识卡片和 3 道复习题生成。
- V0.3 `clean assembleDebug` 构建成功。
- APK 路径：`H:\福建师范大学\大三下\软件实践研发（3）\期末大作业_SmartReadAgent\release\SmartReadAgent-v0.3-debug.apk`。
- 已在 Pixel_8 Android 模拟器中完成 TC-007 至 TC-014 初步验证。
- V0.3 App 截图已生成并归档到软件项目管理材料目录。
- V0.4 已完成 `model/train_sentence_importance_model.py` 训练脚本。
- V0.4 已完成 `model/notebooks/train_sentence_importance_model.ipynb` 模型说明 Notebook。
- V0.4 已导出真实 `model/exports/sentence_importance_model.tflite` 模型文件。
- V0.4 已将模型复制到 `android/app/src/main/assets/sentence_importance_model.tflite`。
- V0.4 已新增 `SentenceImportanceClassifier.kt` 并接入摘要结果页。
- V0.4 `:app:assembleDebug` 构建成功。
- V0.4 APK 已复制到 `release\SmartReadAgent-v0.4-debug.apk`。
- V0.4 APK 已安装到 Pixel_8 模拟器并启动。
- V0.4 LiteRT 端侧分析截图已生成：`LiteRT端侧分析_20260512.png`。
- V0.5 已设计本地历史记录数据结构。
- V0.5 已实现 SharedPreferences 历史记录保存。
- V0.5 已实现历史记录页、恢复历史分析和清空历史功能。
- V0.5 `:app:assembleDebug` 构建成功。
- V0.5 APK 已复制到 `release\SmartReadAgent-v0.5-debug.apk`。
- V0.5 APK 已安装到 Pixel_8 模拟器，历史记录页和恢复历史分析流程已验证。
- V0.5 历史记录截图已生成：`V0.5历史记录页_20260512.png`、`V0.5历史记录恢复_20260512.png`。

## V1.0 收口目标

V1.0 期末演示稳定版。

- 功能范围冻结，不再新增云端大模型 API、登录、OCR、多设备同步、后端服务器、复杂数据库和语音输入。
- Android 版本号更新为 `versionCode = 10`、`versionName = "1.0"`。
- App 顶部文案更新为 `V1.0 期末演示稳定版`。
- V1.0 APK 路径：`release\SmartReadAgent-v1.0-debug.apk`。
- V1.0 模拟器截图已生成并同步到 `screenshots\app\`。
- 技术归档包路径：`H:\福建师范大学\大三下\软件实践研发（3）\期末大作业_SmartReadAgent_V1.0_归档.zip`。
- 项目管理归档包路径：`H:\福建师范大学\大三下\软件项目管理\期末大作业_SmartReadAgent_项目管理_V1.0_归档.zip`。

## 最终材料状态

- Gitee 仓库首页、提交记录、分支、标签、访问统计、仓库数据统计、网络图和发行版截图：已生成。
- GitHub 仓库首页、提交记录和标签截图：已生成。
- 项目管理平台地址：https://lg.team/kanban/board/go/1f14d5c7-ffd8-6bb0-b95b-0242c0a8d007/#/board_view
- Leangoo V1.0 最终状态截图：已生成，已完成列为 29，其余列为 0。
- 小组成员：A03 组，林立洲（121072021030）、江轩宇（121052023075），福建师范大学 2023 级软件工程 2 班。
- Agent 问答截图：已生成。
- 知识卡片截图：已生成。
- LiteRT 分析截图：已生成。
- V1.0 App 最终截图：已生成。
- V1.0 安卓真机体验反馈截图：已归档到项目管理材料 `03_QQ沟通记录/`，覆盖安装启动、摘要、LiteRT、Agent、知识卡片和历史记录流程。
- V1.0 期末演示 PPT 和项目说明 docx：已生成。
