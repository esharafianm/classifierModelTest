package com.tmsimple.testformlmodel;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;



import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TensorFlow Lite interpreter
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize UI components
        EditText editTextThighMax = findViewById(R.id.editTextThighMax);
        EditText editTextThighMin = findViewById(R.id.editTextThighMin);
        EditText editTextFootMax = findViewById(R.id.editTextFootMax);
        EditText editTextFootMin = findViewById(R.id.editTextFootMin);
        TextView output = findViewById(R.id.output);
        TextView output2 = findViewById(R.id.output2);
        Button buttonPredict = findViewById(R.id.buttonPredict);

        // Setup button click listener
        buttonPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float thighMax = Float.parseFloat(editTextThighMax.getText().toString());
                float thighMin = Float.parseFloat(editTextThighMin.getText().toString());
                float footMax = Float.parseFloat(editTextFootMax.getText().toString());
                float footMin = Float.parseFloat(editTextFootMin.getText().toString());

                // Prepare input data for the model
                float[][] input = new float[1][4];
                input[0][0] = thighMax;
                input[0][1] = thighMin;
                input[0][2] = footMax;
                input[0][3] = footMin;

                // Array to hold model output
                float[][] outputVal = new float[1][5];

                // Run inference
                if (tflite != null) {
                    tflite.run(input, outputVal);
                }

                // Interpret the output
                String[] classLabels = {"Downstairs", "Sitting", "Standing", "Upstairs", "Walking"};
                int maxIndex = 0;
                for (int i = 1; i < outputVal[0].length; i++) {
                    if (outputVal[0][i] > outputVal[0][maxIndex]) {
                        maxIndex = i;
                    }
                }
                String result = "Predicted Activity: " + classLabels[maxIndex];

                // Display the results
                output.setText(result);


                // Build a string to show all outputs
                StringBuilder allOutputs = new StringBuilder();
                for (int i = 0; i < outputVal[0].length; i++) {
                    allOutputs.append(classLabels[i]).append(": ").append(String.format("%.8f", outputVal[0][i])).append("\n");
                }

                // Display all the outputs
                output2.setText(allOutputs.toString());

            }
        });
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model_v2.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}