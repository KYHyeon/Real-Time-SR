package com.example.real_time_speech_command;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.real_time_speech_command.utility.AudioUtil;
import com.example.real_time_speech_command.utility.MFCC;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class Validation {
    private Context context;

    private String CLASSES[] = {"silence", "갑자기", "마그네슘", "진통제",
            "타이레놀", "바이러스", "내시경", "비타민", "고혈압",
            "단백질", "스트레스", "카페인", "다이어트", "부작용",
            "에너지", "아스피린"};

    private static final String MODEL_FILENAME = "resnet18_traced.pt";

    Validation(Context context) {
        this.context = context;
        valid();
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

    public void valid() {

        String isDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/valid";

        Module module = null;
        try {
            module = Module.load(assetFilePath(context, MODEL_FILENAME));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int matched = 0;
        int unmatched = 0;

        // 하위의 모든 파일
        for (File file : FileUtils.listFiles(new File(isDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            String[] path = file.getAbsolutePath().split("/");
            String ans = path[path.length - 2];

            System.out.println(file.getAbsolutePath());
            AudioUtil.readWav(file.getAbsolutePath());

            MFCC mfccConvert = new MFCC();
            float[] mfccInput = mfccConvert.process(AudioUtil.input);

            long shape[] = {1, 1, 40, 32};
            Tensor inputTensor = Tensor.fromBlob(mfccInput, new long[]{1, 1, 40, 32});
            Log.v("VALID", "Tensor Input======> " + inputTensor.toString());
            Log.v("VALID", "Tensor Input======> " + Arrays.toString(inputTensor.getDataAsFloatArray()));
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

            if (className.equals(ans))
                matched++;
            else
                unmatched++;
        }

        System.out.println("result : ");
        System.out.println(matched);
        System.out.println(unmatched);
    }

}
