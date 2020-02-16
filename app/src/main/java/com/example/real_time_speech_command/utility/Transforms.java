package com.example.real_time_speech_command.utility;

import java.util.Arrays;

public class Transforms {
    /**
     * Created by KYHyeon on 2020/2/14.
     * <p>
     * Transforms on raw wav samples.
     */

    private RandomUtil random = new RandomUtil();

    boolean should_apply_transform() {
        return should_apply_transform(0.5);
    }

    boolean should_apply_transform(double prob) {
        //    """Transforms are only randomly applied with the given probability."""
        return random.nextDouble() < prob;
    }

    //class LoadAudio(object):
    //    """Loads an audio into a numpy array."""
    //
    //    def __init__(self, sample_rate=16000):
    //        self.sample_rate = sample_rate
    //
    //    def __call__(self, data):
    //        path = data['path']
    //        if path:
    //            samples, sample_rate = librosa.load(path, self.sample_rate)
    //        else:
    //            # silence
    //            sample_rate = self.sample_rate
    //            samples = np.zeros(sample_rate, dtype=np.float32)
    //        data['samples'] = samples
    //        data['sample_rate'] = sample_rate
    //        return data
    //

    //class FixAudioLength(object):
    //    """Either pads or truncates an audio into a fixed length."""
    double[] FixAudioLength(double[] samples, int sample_rate) {
        return FixAudioLength(samples, sample_rate, 1);
    }

    double[] FixAudioLength(double[] samples, int sample_rate, int time) {
        //"""Either pads or truncates an audio into a fixed length."""
        int length = time * sample_rate;
        if (length < samples.length) {
            samples = Arrays.copyOfRange(samples, 0, length);
        } else if (length > samples.length) {
            samples = ArrayUtil.pad_constant(samples, 0, length - samples.length);
        }
        return samples;
    }

    //class ChangeAmplitude(object):
    //    """Changes amplitude of an audio randomly."""


    double[][] ChangeAmplitude(double[][] samples, double low, double high) {
        for (int i = 0; i < samples.length; i++) {
            for (int j = 0; j < samples[i].length; j++) {
                samples[i][j] *= random.uniform(low, high);
            }
        }
        return samples;
    }

    //class ChangeSpeedAndPitchAudio(object):
    //    """Change the speed of an audio. This transform also changes the pitch of the audio."""

    double[] ChangeSpeedAndPitchAudio(double[] samples, double sample_rate) {
        return ChangeSpeedAndPitchAudio(samples, sample_rate, 0.2);
    }

    double[] ChangeSpeedAndPitchAudio(double[] samples, double sample_rate, double max_scale) {
        if (!should_apply_transform())
        {
            return samples;
        }
        double scale = random.uniform(-max_scale, max_scale);
        double speed_fac = 1.0 / (1 + scale);
//        if not should_apply_transform():
//            return data
//
//        samples = data['samples']
//        sample_rate = data['sample_rate']
//        scale = random.uniform(-self.max_scale, self.max_scale)
//        speed_fac = 1.0  / (1 + scale)
//        data['samples'] = np.interp(np.arange(0, len(samples), speed_fac), np.arange(0,len(samples)), samples).astype(np.float32)
//        return data
        return samples;
    }
    //class StretchAudio(object):
    //    """Stretches an audio randomly."""
    //
    //    def __init__(self, max_scale=0.2):
    //        self.max_scale = max_scale
    //
    //    def __call__(self, data):
    //        if not should_apply_transform():
    //            return data
    //
    //        scale = random.uniform(-self.max_scale, self.max_scale)
    //        data['samples'] = librosa.effects.time_stretch(data['samples'], 1+scale)
    //        return data
    //

    //class TimeshiftAudio(object):
    //    """Shifts an audio randomly."""
    double[] TimeshiftAudio(double[] samples, int sample_rate, double max_shift_seconds) {
        int max_shift = (int) (sample_rate * max_shift_seconds);
        int shift = random.nextInt(2 * max_shift) + max_shift;
        int a = -Math.min(0, shift);
        int b = Math.max(0, shift);
        samples = ArrayUtil.pad_constant(samples, a, b);
        if (a != 0) {
            samples = Arrays.copyOfRange(samples, 0, samples.length - a); //samples[:len(samples) - a]
        } else {
            samples = Arrays.copyOfRange(samples, b, samples.length); //samples[b:]
        }
        return samples;
    }

    double[] TimeshiftAudio(double[] samples, int sample_rate) {
        return TimeshiftAudio(samples, sample_rate, 0.2);
    }

    //class AddBackgroundNoise(Dataset):
    //    """Adds a random background noise."""
    double[] AddBackgroundNoise(double[] samples, double[][] bg_dataset_samples) {
        return AddBackgroundNoise(samples, bg_dataset_samples, 0.45);
    }

    double[] AddBackgroundNoise(double[] samples, double[][] bg_dataset_samples, double max_percentage) {
        if (!should_apply_transform()) return samples;
        double[] noise = random.choice(bg_dataset_samples);
        double percentage = random.uniform(0, max_percentage);
        for (int i = 0; i < samples.length; i++) {
                //TODO noise 의 크기와 samples 의 크기가 다르다면?
                samples[i] = samples[i] * (1 - percentage) + noise[i] * percentage;
        }
        return samples;
    }
    //class ToMelSpectrogram(object):
    //    """Creates the mel spectrogram from an audio. The result is a 32x32 matrix."""
    //
    //    def __init__(self, n_mels=32):
    //        self.n_mels = n_mels
    //
    //    def __call__(self, data):
    //        samples = data['samples']
    //        sample_rate = data['sample_rate']
    //        s = librosa.feature.melspectrogram(samples, sr=sample_rate, n_mels=self.n_mels)
    //        data['spectrogram'] = s
    //        data['mel_spectrogram'] = librosa.power_to_db(s, ref=np.max)
    //        return data
    //

    //class ToTensor(object):
    //    """Converts into a tensor."""
    //
    //    def __init__(self, np_name, tensor_name, normalize=None):
    //        self.np_name = np_name
    //        self.tensor_name = tensor_name
    //        self.normalize = normalize
    //
    //    def __call__(self, data):
    //        tensor = torch.FloatTensor(data[self.np_name])
    //        if self.normalize is not None:
    //            mean, std = self.normalize
    //            tensor -= mean
    //            tensor /= std
    //        data[self.tensor_name] = tensor
    //        return data

}
