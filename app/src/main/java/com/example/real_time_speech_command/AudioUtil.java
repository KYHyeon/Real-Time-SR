package com.example.real_time_speech_command;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AudioUtil {
    private static final String TAG = "AudioUtil";

    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1500;
    private static final int RECORDING_LENGTH = SAMPLE_RATE * SAMPLE_DURATION_MS / 1000;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int CHANNEL_COUNT = AudioFormat.CHANNEL_IN_STEREO; //TODO CHANNEL 이 MONO 일 경우 재생이 안됨
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_COUNT, AUDIO_FORMAT);

    private static final String RECORD_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record.pcm";

    private AudioRecord mAudioRecord = null;
    private Thread mRecordThread = null;
    private Thread mPlayThread = null;

    private Context context;    //UI를 활용할 때 사용

    AudioUtil(Context context) {
        this.context = context;
    }

    void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        mAudioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_COUNT, AUDIO_FORMAT, BUFFER_SIZE);
        mAudioRecord.startRecording();

        mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[BUFFER_SIZE];
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(RECORD_FILE_PATH);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                assert fos != null;

                int cnt = 0;

                //TODO RECORDING_LENGTH 가 너무 짧은거 같음.
                while (cnt <= RECORDING_LENGTH) {
                    int ret = mAudioRecord.read(readData, 0, BUFFER_SIZE);
                    Log.d(TAG, "read bytes is " + ret);
                    Log.d(TAG, "read data : " + Arrays.toString(readData));
                    cnt += ret;
                    try {
                        fos.write(readData, 0, BUFFER_SIZE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                mAudioRecord.stop();
                mAudioRecord.release();

                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //TODO 이미 실행중인 쓰레드가 정상 종료되었는지 확인 필요
        mRecordThread.start();
    }

    void play() {
        mPlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AudioTrack mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, CHANNEL_COUNT, AUDIO_FORMAT, BUFFER_SIZE, AudioTrack.MODE_STREAM);
                byte[] writeData = new byte[BUFFER_SIZE];
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(RECORD_FILE_PATH);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                assert fis != null;

                DataInputStream dis = new DataInputStream(fis);
                mAudioTrack.play();

                while (true) {
                    try {
                        int ret = dis.read(writeData, 0, BUFFER_SIZE);
                        if (ret <= 0) {
                            break;
                        }
                        mAudioTrack.write(writeData, 0, ret);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                mAudioTrack.stop();
                mAudioTrack.release();

                try {
                    dis.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mPlayThread.start();
    }
}
