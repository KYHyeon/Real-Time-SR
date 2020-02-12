package com.example.real_time_speech_command;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION_CODE = 1;

    private static final String MODEL_FILENAME = "resnet18_traced.pt";
    private static final String INPUT_DATA_NAME = "Placeholder:0";
    private static final String OUTPUT_SCORES_NAME = "output";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private SpeechRecognizer speechRecognizer;
    //    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    //Module module = Module.load("./dsadas.ff");
    Module module = null;
    AudioUtil audioUtil = new AudioUtil(this);
    TextView result = null;
    private String CLASSES[] = {"silence", "갑자기", "마그네슘", "진통제",
            "타이레놀", "바이러스", "내시경", "비타민", "고혈압",
            "단백질", "스트레스", "카페인", "다이어트", "부작용",
            "에너지", "아스피린"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int[] colors = {
                ContextCompat.getColor(this, R.color.color1),
                ContextCompat.getColor(this, R.color.color2),
                ContextCompat.getColor(this, R.color.color3),
                ContextCompat.getColor(this, R.color.color4),
                ContextCompat.getColor(this, R.color.color5)
        };

        int[] heights = {60, 72, 54, 69, 48};

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final RecognitionProgressView recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);
        recognitionProgressView.setSpeechRecognizer(speechRecognizer);
        recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
            @Override
            public void onResults(Bundle results) {
                showResults(results);
            }
        });
        recognitionProgressView.setColors(colors);
        recognitionProgressView.setBarMaxHeightsInDp(heights);
        recognitionProgressView.setCircleRadiusInDp(6);
        recognitionProgressView.setSpacingInDp(6);
        recognitionProgressView.setIdleStateAmplitudeInDp(6);
        recognitionProgressView.setRotationRadiusInDp(20);
        recognitionProgressView.play();

        try {
            module = Module.load(assetFilePath(this, MODEL_FILENAME));
            Log.d(LOG_TAG, "success open model");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error reading assets", e);
            finish();
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
    }

    public void onRecord(View view) throws InterruptedException {
//        audioUtil.record(AudioUtil.TYPE_RAW);
//        AudioUtil.readWav();
//        startRecognition(AudioUtil.input);
        Validation validation = new Validation(this);
    }

//    public void onPlay(View view) {
//        audioUtil.play();
//    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }

    public void startRecognition(double[] input) {
        Log.d(LOG_TAG, "Start recognition");
//        double[] doubleInputBuffer = AudioUtil.readWav();

//        try {
//            FileWriter writer = new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaOut.csv");
//            for (double v : doubleInputBuffer) {
//                writer.append(String.valueOf(v));
//                writer.append("\n");
//            }
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //MFCC java library.
        MFCC mfccConvert = new MFCC();
        float[] mfccInput = mfccConvert.process(input);
        Log.d(LOG_TAG, "MFCC Input======> " + Arrays.toString(mfccInput));
        Log.d(LOG_TAG, "MFCC Input======> " + mfccInput.length);
        long shape[] = {1, 1, 40, 32};
        Tensor inputTensor = Tensor.fromBlob(mfccInput, new long[]{1, 1, 40, 32});
        //Log.v(LOG_TAG, "Tensor Input======> " + inputTensor.toString());
        //Log.v(LOG_TAG, "Tensor Input======> " + Arrays.toString(inputTensor.getDataAsFloatArray()));
        Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
        float[] scores = outputTensor.getDataAsFloatArray();
        float maxScore = -Float.MAX_VALUE;
        int maxScoreIdx = -1;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxScoreIdx = i;
            }
        }
        String className = CLASSES[maxScoreIdx];
        Log.d(LOG_TAG, className);
        TextView tv = new TextView(this);
        tv.setText("인식결과: " + className);
        tv.setHeight(100);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);
        tv.setTextColor(Color.RED);
        tv.setTypeface(null, Typeface.BOLD);
        LinearLayout ll = new LinearLayout(this.getApplicationContext());
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ll.setBackgroundResource(R.drawable.customts);
        ll.setPadding(30, 0, 30, 0);
        ll.setGravity(Gravity.CENTER);
        ll.addView(tv);
        Toast t = Toast.makeText(this.getApplicationContext(), "", Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.setView(ll);
        t.show();
        //recordingOffset = 0;
    }

    private void showResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Toast.makeText(this, matches.get(0), Toast.LENGTH_LONG).show();
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "Requires RECORD_AUDIO permission", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    REQUEST_PERMISSION_CODE);
        }
    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

}
