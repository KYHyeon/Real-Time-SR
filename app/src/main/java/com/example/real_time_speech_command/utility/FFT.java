package com.example.real_time_speech_command.utility;

import com.example.real_time_speech_command.data.Complex;

public class FFT {
    private static double[] real;
    private static double[] imag;

    /**
     * Performs Fast Fourier Transformation in place.
     */
    public static Complex[] process(double[] signal) {
        final int numPoints = signal.length;
        // initialize real & imag array
        real = signal;
        imag = new double[numPoints];

        // perform FFT using the real & imag array
        final double pi = Math.PI;
        final int numStages = (int) (Math.log(numPoints) / Math.log(2));
        final int halfNumPoints = numPoints >> 1;
        int j = halfNumPoints;
        // FFT time domain decomposition carried out by "bit reversal sorting"
        // algorithm
        int k;
        for (int i = 1; i < numPoints - 2; i++) {
            if (i < j) {
                // swap
                double tempReal = real[j];
                double tempImag = imag[j];
                real[j] = real[i];
                imag[j] = imag[i];
                real[i] = tempReal;
                imag[i] = tempImag;
            }
            k = halfNumPoints;
            while (k <= j) {
                j -= k;
                k >>= 1;
            }
            j += k;
        }

        // loop for each stage
        for (int stage = 1; stage <= numStages; stage++) {
            int LE = 1;
            for (int i = 0; i < stage; i++) {
                LE <<= 1;
            }
            final int LE2 = LE >> 1;
            double UR = 1;
            double UI = 0;
            // calculate sine & cosine values
            final double SR =  Math.cos(pi / LE2);
            final double SI = -Math.sin(pi / LE2);
            // loop for each sub DFT
            for (int subDFT = 1; subDFT <= LE2; subDFT++) {
                // loop for each butterfly
                for (int butterfly = subDFT - 1; butterfly <= numPoints - 1; butterfly += LE) {
                    int ip = butterfly + LE2;
                    // butterfly calculation
                    double tempReal = (double) (real[ip] * UR - imag[ip] * UI);
                    double tempImag = (double) (real[ip] * UI + imag[ip] * UR);
                    real[ip] = real[butterfly] - tempReal;
                    imag[ip] = imag[butterfly] - tempImag;
                    real[butterfly] += tempReal;
                    imag[butterfly] += tempImag;
                }

                double tempUR = UR;
                UR = tempUR * SR - UI * SI;
                UI = tempUR * SI + UI * SR;
            }
        }

        Complex[] ret = new Complex[real.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new Complex(real[i],imag[i]);
        }
        return ret;
    }
}