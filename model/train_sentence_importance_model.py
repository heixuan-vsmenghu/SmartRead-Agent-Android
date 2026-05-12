from __future__ import annotations

import json
import hashlib
import math
import os
import pathlib
import subprocess
import sys
from dataclasses import dataclass

import numpy as np


MODEL_DIR = pathlib.Path(__file__).resolve().parent
EXPORT_DIR = MODEL_DIR / "exports"
TOOLS_DIR = MODEL_DIR / "tools"
MODEL_PATH = EXPORT_DIR / "sentence_importance_model.tflite"
LABELS_PATH = EXPORT_DIR / "labels.txt"
WEIGHTS_PATH = EXPORT_DIR / "sentence_importance_weights.properties"
METRICS_PATH = EXPORT_DIR / "sentence_importance_training_summary.json"
METADATA_PATH = EXPORT_DIR / "model_metadata.json"


@dataclass
class TrainingResult:
    w1: np.ndarray
    b1: np.ndarray
    w2: np.ndarray
    b2: np.ndarray
    w3: np.ndarray
    b3: np.ndarray
    loss: float
    accuracy: float


def make_weak_supervised_data(seed: int = 42) -> tuple[np.ndarray, np.ndarray]:
    rng = np.random.default_rng(seed)
    rows = []
    labels = []
    for _ in range(960):
        length = rng.beta(2.2, 3.0)
        keyword_overlap = rng.beta(1.6, 4.2)
        position = rng.beta(2.0, 2.8)
        punctuation = rng.choice([0.0, 0.35, 0.7, 1.0], p=[0.52, 0.24, 0.18, 0.06])
        cue = rng.choice([0.0, 0.45, 0.8, 1.0], p=[0.58, 0.22, 0.16, 0.04])

        score = (
            0.22 * length
            + 0.31 * keyword_overlap
            + 0.20 * position
            + 0.12 * punctuation
            + 0.25 * cue
            - 0.05
        )
        score += rng.normal(0.0, 0.035)
        label = np.clip(score, 0.02, 0.98)
        rows.append([length, keyword_overlap, position, punctuation, cue])
        labels.append([label])
    return np.asarray(rows, dtype=np.float32), np.asarray(labels, dtype=np.float32)


def sigmoid(x: np.ndarray) -> np.ndarray:
    return 1.0 / (1.0 + np.exp(-np.clip(x, -40.0, 40.0)))


def train_numpy_model(x: np.ndarray, y: np.ndarray, seed: int = 7) -> TrainingResult:
    rng = np.random.default_rng(seed)
    n = x.shape[0]
    w1 = rng.normal(0.0, math.sqrt(2 / 5), size=(5, 8)).astype(np.float32)
    b1 = np.zeros((1, 8), dtype=np.float32)
    w2 = rng.normal(0.0, math.sqrt(2 / 8), size=(8, 4)).astype(np.float32)
    b2 = np.zeros((1, 4), dtype=np.float32)
    w3 = rng.normal(0.0, math.sqrt(1 / 4), size=(4, 1)).astype(np.float32)
    b3 = np.zeros((1, 1), dtype=np.float32)

    lr = 0.035
    last_loss = 0.0
    for epoch in range(1400):
        z1 = x @ w1 + b1
        a1 = np.maximum(z1, 0.0)
        z2 = a1 @ w2 + b2
        a2 = np.maximum(z2, 0.0)
        logits = a2 @ w3 + b3
        pred = sigmoid(logits)

        eps = 1e-7
        last_loss = float(-np.mean(y * np.log(pred + eps) + (1.0 - y) * np.log(1.0 - pred + eps)))

        dlogits = (pred - y) / n
        dw3 = a2.T @ dlogits
        db3 = np.sum(dlogits, axis=0, keepdims=True)
        da2 = dlogits @ w3.T
        dz2 = da2 * (z2 > 0)
        dw2 = a1.T @ dz2
        db2 = np.sum(dz2, axis=0, keepdims=True)
        da1 = dz2 @ w2.T
        dz1 = da1 * (z1 > 0)
        dw1 = x.T @ dz1
        db1 = np.sum(dz1, axis=0, keepdims=True)

        w3 -= lr * dw3.astype(np.float32)
        b3 -= lr * db3.astype(np.float32)
        w2 -= lr * dw2.astype(np.float32)
        b2 -= lr * db2.astype(np.float32)
        w1 -= lr * dw1.astype(np.float32)
        b1 -= lr * db1.astype(np.float32)

        if epoch in (500, 900):
            lr *= 0.55

    pred = sigmoid(np.maximum(x @ w1 + b1, 0.0) @ w2 + b2)
    pred = sigmoid(np.maximum(np.maximum(x @ w1 + b1, 0.0) @ w2 + b2, 0.0) @ w3 + b3)
    accuracy = float(np.mean((pred >= 0.5) == (y >= 0.5)))
    return TrainingResult(
        w1=w1,
        b1=b1.reshape(-1),
        w2=w2,
        b2=b2.reshape(-1),
        w3=w3,
        b3=b3.reshape(-1),
        loss=last_loss,
        accuracy=accuracy,
    )


def save_weights(result: TrainingResult) -> None:
    EXPORT_DIR.mkdir(parents=True, exist_ok=True)
    # TFLite FULLY_CONNECTED stores weights as [units, input_size].
    values = {
        "w1": result.w1.T.reshape(-1),
        "b1": result.b1,
        "w2": result.w2.T.reshape(-1),
        "b2": result.b2,
        "w3": result.w3.T.reshape(-1),
        "b3": result.b3,
    }
    with WEIGHTS_PATH.open("w", encoding="utf-8") as f:
        f.write("# SmartRead Agent sentence importance model weights\n")
        for key, array in values.items():
            encoded = ",".join(f"{float(v):.8f}" for v in array)
            f.write(f"{key}={encoded}\n")


def find_first(pattern: str, base: pathlib.Path) -> pathlib.Path | None:
    matches = list(base.glob(pattern))
    return matches[0] if matches else None


def build_tflite_with_java_schema() -> None:
    gradle_cache = pathlib.Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1"
    schema_jar = find_first(
        "org.tensorflow/tensorflow-lite-metadata/0.2.0/**/*.jar",
        gradle_cache,
    )
    flatbuffers_jar = find_first(
        "com.google.flatbuffers/flatbuffers-java/**/*.jar",
        gradle_cache,
    )
    if schema_jar is None or flatbuffers_jar is None:
        raise RuntimeError("TensorFlow Lite schema jar or FlatBuffers jar was not found in Gradle cache.")

    build_dir = TOOLS_DIR / "build"
    build_dir.mkdir(parents=True, exist_ok=True)
    java_file = TOOLS_DIR / "TfliteDenseModelBuilder.java"
    classpath = os.pathsep.join([str(schema_jar), str(flatbuffers_jar)])

    subprocess.run(
        ["javac", "-encoding", "UTF-8", "-cp", classpath, "-d", str(build_dir), str(java_file)],
        check=True,
    )
    subprocess.run(
        [
            "java",
            "-cp",
            os.pathsep.join([classpath, str(build_dir)]),
            "TfliteDenseModelBuilder",
            str(WEIGHTS_PATH),
            str(MODEL_PATH),
        ],
        check=True,
    )


def write_labels() -> None:
    LABELS_PATH.write_text("low\nmedium\nhigh\n", encoding="utf-8")


def model_metadata(note: str) -> dict[str, object]:
    size_bytes = MODEL_PATH.stat().st_size if MODEL_PATH.exists() else None
    sha256 = hashlib.sha256(MODEL_PATH.read_bytes()).hexdigest() if MODEL_PATH.exists() else None
    return {
        "model_name": "sentence_importance_model",
        "version": "v0.4",
        "input_shape": [1, 5],
        "output_shape": [1, 1],
        "features": [
            "sentenceLengthNorm",
            "keywordOverlapScore",
            "positionScore",
            "punctuationScore",
            "summaryCueScore",
        ],
        "output": "sentence importance score from 0.0 to 1.0",
        "android_asset_path": "app/src/main/assets/sentence_importance_model.tflite",
        "note": note,
        "file_size_bytes": size_bytes,
        "sha256": sha256,
    }


def write_model_metadata(note: str) -> None:
    METADATA_PATH.write_text(
        json.dumps(model_metadata(note), ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )


def write_summary(result: TrainingResult, sample_score: float, note: str) -> None:
    payload = {
        "project": "SmartRead Agent",
        "version": "v0.4",
        "note": note,
        "features": [
            "sentenceLengthNorm",
            "keywordOverlapScore",
            "positionScore",
            "punctuationScore",
            "summaryCueScore",
        ],
        "architecture": "Input(5) -> Dense(8, relu) -> Dense(4, relu) -> Dense(1, sigmoid)",
        "loss": result.loss,
        "weak_label_accuracy": result.accuracy,
        "sample_features": [0.62, 0.70, 0.85, 0.35, 0.80],
        "sample_score": sample_score,
        "output_model": str(MODEL_PATH),
    }
    METRICS_PATH.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")


def predict_numpy(result: TrainingResult, features: np.ndarray) -> float:
    a1 = np.maximum(features @ result.w1 + result.b1, 0.0)
    a2 = np.maximum(a1 @ result.w2 + result.b2, 0.0)
    return float(sigmoid(a2 @ result.w3 + result.b3)[0, 0])


def train_with_tensorflow_if_available(x: np.ndarray, y: np.ndarray) -> tuple[bool, float | None, str]:
    try:
        import tensorflow as tf  # type: ignore
    except Exception as exc:
        return False, None, f"TensorFlow is not available in this environment: {type(exc).__name__}: {exc}"

    tf.random.set_seed(7)
    model = tf.keras.Sequential(
        [
            tf.keras.layers.Input(shape=(5,), name="sentence_features"),
            tf.keras.layers.Dense(8, activation="relu"),
            tf.keras.layers.Dense(4, activation="relu"),
            tf.keras.layers.Dense(1, activation="sigmoid", name="importance_score"),
        ],
        name="sentence_importance_model",
    )
    model.compile(optimizer=tf.keras.optimizers.Adam(0.015), loss="binary_crossentropy", metrics=["accuracy"])
    history = model.fit(x, y, epochs=80, batch_size=32, verbose=0)
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    MODEL_PATH.write_bytes(converter.convert())
    write_labels()
    sample = np.asarray([[0.62, 0.70, 0.85, 0.35, 0.80]], dtype=np.float32)
    sample_score = float(model.predict(sample, verbose=0)[0][0])
    payload = {
        "project": "SmartRead Agent",
        "version": "v0.4",
        "note": "TensorFlow/Keras weak-supervision training was used and converted with TFLiteConverter.",
        "features": model_metadata("Lightweight model for course project demo. It is used for on-device sentence importance analysis.")["features"],
        "architecture": "Input(5) -> Dense(8, relu) -> Dense(4, relu) -> Dense(1, sigmoid)",
        "loss": float(history.history["loss"][-1]),
        "weak_label_accuracy": float(history.history.get("accuracy", [0.0])[-1]),
        "sample_features": [0.62, 0.70, 0.85, 0.35, 0.80],
        "sample_score": sample_score,
        "output_model": str(MODEL_PATH),
    }
    METRICS_PATH.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    write_model_metadata("Lightweight model for course project demo. It is used for on-device sentence importance analysis.")
    print("Training backend: TensorFlow/Keras")
    print(f"Training loss: {payload['loss']:.4f}")
    print(f"Weak-label accuracy: {payload['weak_label_accuracy']:.3f}")
    print(f"Sample score: {sample_score:.3f}")
    return True, sample_score, "TensorFlow/Keras export completed."


def train_with_numpy_fallback(x: np.ndarray, y: np.ndarray) -> float:
    result = train_numpy_model(x, y)
    sample = np.asarray([[0.62, 0.70, 0.85, 0.35, 0.80]], dtype=np.float32)
    sample_score = predict_numpy(result, sample)
    save_weights(result)
    write_labels()
    build_tflite_with_java_schema()
    note = "NumPy weak-supervision training was used because TensorFlow is not available in this local Python environment."
    write_summary(result, sample_score, note)
    write_model_metadata("Lightweight model for course project demo. It is used for on-device sentence importance analysis.")
    print("Training backend: NumPy + TensorFlow Lite FlatBuffer schema")
    print(f"Training loss: {result.loss:.4f}")
    print(f"Weak-label accuracy: {result.accuracy:.3f}")
    print(f"Sample score: {sample_score:.3f}")
    return sample_score


def main() -> None:
    print("SmartRead Agent V0.4 sentence importance training")
    print(f"Python: {sys.version.split()[0]}")
    EXPORT_DIR.mkdir(parents=True, exist_ok=True)
    x, y = make_weak_supervised_data()
    ok, _, message = train_with_tensorflow_if_available(x, y)
    if not ok:
        print(message)
        train_with_numpy_fallback(x, y)
    print(f"Saved model: {MODEL_PATH}")
    print(f"Saved labels: {LABELS_PATH}")
    print(f"Saved metadata: {METADATA_PATH}")
    print(f"Model size: {MODEL_PATH.stat().st_size} bytes")
    print(f"Model SHA256: {hashlib.sha256(MODEL_PATH.read_bytes()).hexdigest()}")
    print("Input tensor shape: [1, 5]")
    print("Output tensor shape: [1, 1]")


if __name__ == "__main__":
    main()
