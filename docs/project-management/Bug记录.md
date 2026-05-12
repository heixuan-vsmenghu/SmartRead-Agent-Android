# SmartRead Agent Bug 记录

V0.4 已完成 LiteRT 端侧分析初步验证，当前暂无阻塞性 Bug。

| 编号 | 日期 | 发现人 | 问题描述 | 严重程度 | 状态 | 负责人 | 修复说明 |
|---|---|---|---|---|---|---|---|
| BUG-001 | 2026-05-12 | 项目负责人 | 模型训练脚本首次运行时将导出目录解析到项目根目录，导致生成了临时 `exports/` 目录 | 低 | 已修复 | 项目负责人 | 修正为 `MODEL_DIR / "exports"`，重新训练并导出到 `model/exports/`，清理根目录临时文件 |
| OPT-001 | 2026-05-12 | 项目负责人 | TensorFlow 在线安装受网络和 SSL 握手影响，无法在本机直接跑 Keras 转换 | 建议 | 已处理 | 项目负责人 | 改用 NumPy 训练同结构模型，并通过 TensorFlow Lite schema + FlatBuffers Java runtime 导出真实 `.tflite` 文件 |
