/**
 * Copyright 2017 Kailash Dabhi (Kingbull Technology)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.real_time_speech_command;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

/**
 * @author Kailash Dabhi
 * @date 18-07-2016. Copyright (c) 2017 Kingbull Technology. All rights reserved.
 */
public class WavRecorderActivity extends AppCompatActivity {
    Recorder recorder;
    ImageView recordButton;
    CheckBox skipSilence;
    private Button pauseResumeButton;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        setupRecorder();
        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                recorder.startRecording();
                skipSilence.setEnabled(false);
            }
        });
        findViewById(R.id.stopButton).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                try {
                    recorder.stopRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                skipSilence.setEnabled(true);
            }
        });
        pauseResumeButton = findViewById(R.id.pauseResumeButton);
        pauseResumeButton.setOnClickListener(new View.OnClickListener() {
            boolean isPaused = false;

            @Override public void onClick(View view) {
                if (recorder == null) {
                    Toast.makeText(WavRecorderActivity.this, "Please start recording first!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isPaused) {
                    pauseResumeButton.setText(getString(R.string.resume_recording));
                    recorder.pauseRecording();
                } else {
                    pauseResumeButton.setText(getString(R.string.pause_recording));
                    recorder.resumeRecording();
                }
                isPaused = !isPaused;
            }
        });
    }

    private void setupRecorder() {
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                    }
                }), file());
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 16000
                )
        );
    }

    @NonNull
    private File file() {
        return new File(Environment.getExternalStorageDirectory(), "kailashdabhi.wav");
    }
}