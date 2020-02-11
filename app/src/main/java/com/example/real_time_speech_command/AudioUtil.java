package com.example.real_time_speech_command;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AudioUtil {
    private static final String TAG = "AudioUtil";

    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1500;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int CHANNEL_COUNT = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final String RECORD_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/c_4-O-ZaRkHG8_36..wav";
    private static final int BUFFER_SIZE = SAMPLE_RATE * 2;
    public static final int TYPE_RAW = 0;

    private AudioRecord mAudioRecord = null;
    private Thread mRecordThread = null;
    private Thread mPlayThread = null;

    private Context context;    //UI를 활용할 때 사용
    public static final int TYPE_WAV = 1;
    //    private static final int RECORDING_LENGTH = SAMPLE_RATE * SAMPLE_DURATION_MS / 1000;
    private static final int RECORDING_LENGTH = SAMPLE_RATE * 5;

    AudioUtil(Context context) {
        this.context = context;
    }

    void play() {
        mPlayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                AudioTrack mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AUDIO_FORMAT, BUFFER_SIZE, AudioTrack.MODE_STREAM);
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

    /**
     * 녹음을 하여 RECORD_FILE_PATH 경로에 모노 방식으로 저장한다.
     *
     * @param type 녹음 데이터를 저장할 방식
     *             AudioUtil.TYPE_RAW : 0
     *             AudioUtil.TYPE_WAV : 1
     */
    void record(final int type) throws InterruptedException {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        mAudioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_COUNT, AUDIO_FORMAT, BUFFER_SIZE);
        mAudioRecord.startRecording();

        mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] readData = new byte[BUFFER_SIZE];
//                FileOutputStream fos = null;
//                try {
//                    fos = new FileOutputStream(RECORD_FILE_PATH);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                assert fos != null;
//
//                if (type == TYPE_WAV) {
//                    try {
//                        writeWavHeader(fos, (short) 1 /* Mono */, SAMPLE_RATE, (short) 16);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

                int cnt = 0;

                while (cnt <= RECORDING_LENGTH) {
                    int ret = mAudioRecord.read(readData, 0, BUFFER_SIZE);
                    Log.d(TAG, "read bytes is " + ret);
                    Log.d(TAG, "read data : " + Arrays.toString(readData));
                    cnt += ret;
//                    try {
//                        fos.write(readData, 0, BUFFER_SIZE);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }

                mAudioRecord.stop();
                mAudioRecord.release();

                //TODO 이거 반환 bytesToDoubles(readData);
//                try {
//                    fos.close();
//                    if (type == TYPE_WAV) {
//                        updateWavHeader(new File(RECORD_FILE_PATH));
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });

        //TODO 이미 실행중인 쓰레드가 정상 종료되었는지 확인 필요
        mRecordThread.start();
        mRecordThread.join();
    }

    /**
     * wav 파일을 읽는다.
     *
     * @return librosa 방법으로 읽은 wav파일 배열
     */
    static double[] readWav() {
        byte[] inputBuffer = new byte[0];

        try {
            FileInputStream fis = new FileInputStream(AudioUtil.RECORD_FILE_PATH);
            inputBuffer = IOUtils.toByteArray(fis);
            String str = Arrays.toString(inputBuffer);
            BufferedWriter writer = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ff.txt"));
            writer.write(str);
            writer.close();

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytesToDoubles(inputBuffer);
    }

    private static void updateWavHeader(File wav) throws IOException {
        byte[] sizes = ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                // 아마 이 두 개를 계산할 때 좀 더 좋은 방법이 있을거라 생각하지만..
                .putInt((int) (wav.length() - 8)) // ChunkSize
                .putInt((int) (wav.length() - 44)) // Chunk Size
                .array();
        try (RandomAccessFile accessWave = new RandomAccessFile(wav, "rw")) {
            // 읽기-쓰기 모드로 인스턴스 생성
            // ChunkSize
            accessWave.seek(4); // 4바이트 지점으로 가서
            accessWave.write(sizes, 0, 4); // 사이즈 채움
            // Chunk Size
            accessWave.seek(40); // 40바이트 지점으로 가서
            accessWave.write(sizes, 4, 4); // 채움
        }
    }

    private static double[] bytesToDoubles(byte[] inputBuffer) {
        double[] doubleInputBuffer = new double[(inputBuffer.length - 44) / 2];

        // We need to feed in float values between -1.0 and 1.0, so divide the
        // signed 16-bit inputs.
        for (int i = 0; i < doubleInputBuffer.length * 2; i += 2) {
            doubleInputBuffer[i / 2] = inputBuffer[i + 44] / 32768.0;
            doubleInputBuffer[i / 2] += inputBuffer[i + 45] / 32768.0 * 256;
        }
        return doubleInputBuffer;
    }

    private static void writeWavHeader(FileOutputStream out, short channels, int sampleRate, short bitDepth) throws IOException {
        // WAV 포맷에 필요한 little endian 포맷으로 다중 바이트의 수를 raw byte로 변환한다.
        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();
        // 최고를 생성하지는 않겠지만, 적어도 쉽게만 가자.
        out.write(new byte[]{
                'R', 'I', 'F', 'F', // Chunk ID
                0, 0, 0, 0, // Chunk Size (나중에 업데이트 될것)
                'W', 'A', 'V', 'E', // Format
                'f', 'm', 't', ' ', //Chunk ID
                16, 0, 0, 0, // Chunk Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // Num of Channels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // Byte Rate
                littleBytes[10], littleBytes[11], // Block Align
                littleBytes[12], littleBytes[13], // Bits Per Sample
                'd', 'a', 't', 'a', // Chunk ID
                0, 0, 0, 0, //Chunk Size (나중에 업데이트 될 것)
        });
    }
}
