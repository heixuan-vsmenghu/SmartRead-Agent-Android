# 模型目录

本目录用于放置 SmartRead Agent 的模型相关材料，包括训练脚本、训练 Notebook、实验数据和导出的 LiteRT 模型文件。

## 目录说明

- `notebooks/`：保存模型训练或转换 Notebook。
- `data/`：后续保存可公开使用的小规模文本样例数据。
- `exports/`：保存导出的 `.tflite` 模型、标签文件、训练摘要和权重文件。
- `tools/`：保存受限环境下构造 TFLite FlatBuffer 的 Java 工具。

## V0.4 模型

V0.4 已训练一个轻量句子重要性模型，用于对文章句子进行端侧评分。模型输入为 5 个手工特征：句子长度、关键词重合度、句子位置、标点提示和总结提示词。

当前本机 Python 环境无法安装 TensorFlow，因此训练脚本采用 NumPy 训练同结构网络，再通过 TensorFlow Lite schema 和 FlatBuffers Java runtime 导出真实 `.tflite` 文件。导出结果位于：

- `exports/sentence_importance_model.tflite`
- `exports/labels.txt`
- `exports/sentence_importance_training_summary.json`
- `exports/model_metadata.json`

Android 工程使用 assets 中的同名模型文件进行端侧推理。

可复现命令：

```powershell
python model\train_sentence_importance_model.py
```

脚本会优先尝试 TensorFlow/Keras Sequential 训练与 TFLiteConverter 导出；如果当前环境没有 TensorFlow，则使用 NumPy 训练同结构网络，并通过 TensorFlow Lite schema 与 FlatBuffers Java runtime 生成真实 `.tflite` 文件。当前本机执行结果显示 TensorFlow 不可用，因此采用了 NumPy + TFLite schema 路径。

当前模型元信息：

- 输入 shape：`[1, 5]`
- 输出 shape：`[1, 1]`
- 模型大小：1496 bytes
- SHA256：`11462553e040257c11677bc7824021440761922ff4a8b3ced2bb7d21904b9dde`
