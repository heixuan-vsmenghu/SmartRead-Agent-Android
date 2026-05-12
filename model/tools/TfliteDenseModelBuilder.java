import com.google.flatbuffers.FlatBufferBuilder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.tensorflow.lite.schema.ActivationFunctionType;
import org.tensorflow.lite.schema.Buffer;
import org.tensorflow.lite.schema.BuiltinOperator;
import org.tensorflow.lite.schema.BuiltinOptions;
import org.tensorflow.lite.schema.FullyConnectedOptions;
import org.tensorflow.lite.schema.FullyConnectedOptionsWeightsFormat;
import org.tensorflow.lite.schema.Model;
import org.tensorflow.lite.schema.Operator;
import org.tensorflow.lite.schema.OperatorCode;
import org.tensorflow.lite.schema.SubGraph;
import org.tensorflow.lite.schema.Tensor;
import org.tensorflow.lite.schema.TensorType;

public final class TfliteDenseModelBuilder {
    private TfliteDenseModelBuilder() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                "Usage: TfliteDenseModelBuilder <weights.properties> <output.tflite>"
            );
        }
        Properties weights = new Properties();
        try (var input = Files.newInputStream(Path.of(args[0]))) {
            weights.load(input);
        }
        byte[] model = buildModel(
            floats(weights, "w1", 5 * 8),
            floats(weights, "b1", 8),
            floats(weights, "w2", 8 * 4),
            floats(weights, "b2", 4),
            floats(weights, "w3", 4),
            floats(weights, "b3", 1)
        );
        Files.write(Path.of(args[1]), model);
        System.out.println("Wrote " + args[1] + " (" + model.length + " bytes)");
    }

    private static byte[] buildModel(
        float[] w1,
        float[] b1,
        float[] w2,
        float[] b2,
        float[] w3,
        float[] b3
    ) {
        FlatBufferBuilder builder = new FlatBufferBuilder(8192);

        int emptyBuffer = Buffer.createBuffer(builder, 0);
        int w1Buffer = createFloatBuffer(builder, w1);
        int b1Buffer = createFloatBuffer(builder, b1);
        int w2Buffer = createFloatBuffer(builder, w2);
        int b2Buffer = createFloatBuffer(builder, b2);
        int w3Buffer = createFloatBuffer(builder, w3);
        int b3Buffer = createFloatBuffer(builder, b3);
        int buffers = Model.createBuffersVector(
            builder,
            new int[] { emptyBuffer, w1Buffer, b1Buffer, w2Buffer, b2Buffer, w3Buffer, b3Buffer }
        );

        int input = createTensor(builder, "features", new int[] { 1, 5 }, TensorType.FLOAT32, 0);
        int w1Tensor = createTensor(builder, "dense1_weights", new int[] { 8, 5 }, TensorType.FLOAT32, 1);
        int b1Tensor = createTensor(builder, "dense1_bias", new int[] { 8 }, TensorType.FLOAT32, 2);
        int dense1 = createTensor(builder, "dense1_relu", new int[] { 1, 8 }, TensorType.FLOAT32, 0);
        int w2Tensor = createTensor(builder, "dense2_weights", new int[] { 4, 8 }, TensorType.FLOAT32, 3);
        int b2Tensor = createTensor(builder, "dense2_bias", new int[] { 4 }, TensorType.FLOAT32, 4);
        int dense2 = createTensor(builder, "dense2_relu", new int[] { 1, 4 }, TensorType.FLOAT32, 0);
        int w3Tensor = createTensor(builder, "score_weights", new int[] { 1, 4 }, TensorType.FLOAT32, 5);
        int b3Tensor = createTensor(builder, "score_bias", new int[] { 1 }, TensorType.FLOAT32, 6);
        int dense3 = createTensor(builder, "score_logit", new int[] { 1, 1 }, TensorType.FLOAT32, 0);
        int output = createTensor(builder, "importance_score", new int[] { 1, 1 }, TensorType.FLOAT32, 0);
        int tensors = SubGraph.createTensorsVector(
            builder,
            new int[] { input, w1Tensor, b1Tensor, dense1, w2Tensor, b2Tensor, dense2, w3Tensor, b3Tensor, dense3, output }
        );

        int graphInputs = SubGraph.createInputsVector(builder, new int[] { 0 });
        int graphOutputs = SubGraph.createOutputsVector(builder, new int[] { 10 });

        int fcRelu1 = createFullyConnectedOperator(builder, 0, new int[] { 0, 1, 2 }, new int[] { 3 }, ActivationFunctionType.RELU);
        int fcRelu2 = createFullyConnectedOperator(builder, 0, new int[] { 3, 4, 5 }, new int[] { 6 }, ActivationFunctionType.RELU);
        int fcLinear = createFullyConnectedOperator(builder, 0, new int[] { 6, 7, 8 }, new int[] { 9 }, ActivationFunctionType.NONE);
        int logistic = createSimpleOperator(builder, 1, new int[] { 9 }, new int[] { 10 });
        int operators = SubGraph.createOperatorsVector(builder, new int[] { fcRelu1, fcRelu2, fcLinear, logistic });

        int graphName = builder.createString("sentence_importance");
        int subgraph = SubGraph.createSubGraph(builder, tensors, graphInputs, graphOutputs, operators, graphName);
        int subgraphs = Model.createSubgraphsVector(builder, new int[] { subgraph });

        int fcCode = OperatorCode.createOperatorCode(
            builder,
            (byte) BuiltinOperator.FULLY_CONNECTED,
            0,
            1,
            BuiltinOperator.FULLY_CONNECTED
        );
        int logisticCode = OperatorCode.createOperatorCode(
            builder,
            (byte) BuiltinOperator.LOGISTIC,
            0,
            1,
            BuiltinOperator.LOGISTIC
        );
        int opCodes = Model.createOperatorCodesVector(builder, new int[] { fcCode, logisticCode });

        int description = builder.createString("SmartRead Agent V0.4 sentence importance model");
        int model = Model.createModel(builder, 3, opCodes, subgraphs, description, buffers, 0, 0, 0);
        Model.finishModelBuffer(builder, model);
        ByteBuffer data = builder.dataBuffer();
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        return bytes;
    }

    private static int createTensor(
        FlatBufferBuilder builder,
        String name,
        int[] shape,
        byte type,
        long bufferIndex
    ) {
        int shapeVector = Tensor.createShapeVector(builder, shape);
        int nameOffset = builder.createString(name);
        return Tensor.createTensor(builder, shapeVector, type, bufferIndex, nameOffset, 0, false, 0, 0);
    }

    private static int createFullyConnectedOperator(
        FlatBufferBuilder builder,
        long opcodeIndex,
        int[] inputs,
        int[] outputs,
        byte activation
    ) {
        int inputVector = Operator.createInputsVector(builder, inputs);
        int outputVector = Operator.createOutputsVector(builder, outputs);
        int options = FullyConnectedOptions.createFullyConnectedOptions(
            builder,
            activation,
            FullyConnectedOptionsWeightsFormat.DEFAULT,
            false,
            false
        );
        return Operator.createOperator(
            builder,
            opcodeIndex,
            inputVector,
            outputVector,
            BuiltinOptions.FullyConnectedOptions,
            options,
            0,
            (byte) 0,
            0,
            0
        );
    }

    private static int createSimpleOperator(
        FlatBufferBuilder builder,
        long opcodeIndex,
        int[] inputs,
        int[] outputs
    ) {
        int inputVector = Operator.createInputsVector(builder, inputs);
        int outputVector = Operator.createOutputsVector(builder, outputs);
        return Operator.createOperator(
            builder,
            opcodeIndex,
            inputVector,
            outputVector,
            BuiltinOptions.NONE,
            0,
            0,
            (byte) 0,
            0,
            0
        );
    }

    private static int createFloatBuffer(FlatBufferBuilder builder, float[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (float value : values) {
            buffer.putFloat(value);
        }
        return Buffer.createBuffer(builder, Buffer.createDataVector(builder, buffer.array()));
    }

    private static float[] floats(Properties properties, String key, int expectedLength) {
        String raw = properties.getProperty(key);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        String[] parts = raw.split(",");
        if (parts.length != expectedLength) {
            throw new IllegalArgumentException(
                "Key " + key + " expected " + expectedLength + " values, found " + parts.length
            );
        }
        float[] values = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = Float.parseFloat(parts[i].trim());
        }
        return values;
    }
}
