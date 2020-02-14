package com.example.real_time_speech_command.utility;

import java.util.Random;

public class RandomUtil extends Random {

    double uniform(double low, double high) {
        //Draw samples from a uniform distribution.
        return nextDouble() * (high - low) + low;
    }

    double[][] choice(double[][][] samples) {
        int idx = nextInt(samples.length);
        return samples[idx];
    }
}
