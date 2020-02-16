package com.example.real_time_speech_command.utility;

import java.util.Arrays;

public class ArrayUtil {
    /**
     * Created by KYHyeon on 2020/2/2.
     * np.pad(array, pad_width, mode='reflect') implements for JAVA
     *
     * @param array     The array to pad.
     * @param pad_width Number of values padded to the edges
     * @return Padded array of rank equal to array with shape increased according to pad_width.
     */
    public static double[] pad_reflect(double[] array, int pad_width) {
        double[] ret = new double[array.length + pad_width * 2];

        if (array.length == 0) {
            throw new IllegalArgumentException("can't extend empty axis 0 using modes other than 'constant' or 'empty'");
        }

        //Exception if only one element exists
        if (array.length == 1) {
            Arrays.fill(ret, array[0]);
            return ret;
        }

        //Left_Pad
        int pos = 0;
        int dis = -1;
        for (int i = 0; i < pad_width; i++) {
            if (pos == array.length - 1 || pos == 0) {
                dis = -dis;
            }
            pos += dis;
            ret[pad_width - i - 1] = array[pos];
        }

        System.arraycopy(array, 0, ret, pad_width, array.length);

        //Right_Pad
        pos = array.length - 1;
        dis = 1;
        for (int i = 0; i < pad_width; i++) {
            if (pos == array.length - 1 || pos == 0) {
                dis = -dis;
            }
            pos += dis;
            ret[pad_width + array.length + i] = array[pos];
        }
        return ret;
    }

    /**
     * Created by KYHyeon on 2020/2/15.
     * np.pad(array, pad_width, mode='constant') implements for JAVA
     *
     * @param array      The array to pad.
     * @param lpad_width Number of values padded to the left edge
     * @param rpad_width Number of values padded to the right edge
     * @return Padded array of rank equal to array with shape increased according to pad_width.
     */
    public static double[] pad_constant(double[] array, int lpad_width, int rpad_width) {
        double[] ret = new double[array.length + lpad_width + rpad_width];
        System.arraycopy(array, 0, ret, lpad_width, array.length);
        return ret;
    }

    /**
     * Return evenly spaced values within a given interval.
     *
     * @param start Start of interval. The interval includes this value.
     * @param stop  End of interval. The interval does not include this value,
     *              except in some cases where step is not an integer and floating point round-off
     *              affects the length of out.
     * @param step  Spacing between values. For any output out,
     *              this is the distance between two adjacent values, out[i+1] - out[i].
     * @return Array of evenly spaced values.
     * For floating point arguments, the length of the result is ceil((stop - start)/step).
     * Because of floating point overflow,
     * this rule may result in the last element of out being greater than stop.
     */
    public static double[] arange(int start, int stop, double step) {
        double[] ret = new double[(int) ((stop - start) / step)];
        int cnt = 0;
        for (int i = start; i < stop; i += step) {
            ret[cnt++] = i;
        }
        return ret;
    }

    public static double[][] zeros(int r, int c) {
        return new double[r][c];
    }

//    /**
//     * Return evenly spaced numbers over a specified interval.
//     *
//     * @param start The starting value of the sequence.
//     * @param stop  The end value of the sequence, unless endpoint is set to False.
//     *              In that case, the sequence consists of all
//     *              but the last of num + 1 evenly spaced samples, so that stop is excluded.
//     *              Note that the step size changes when endpoint is False.
//     * @param num   Number of samples to generate. Default is 50. Must be non-negative.
//     * @return There are num equally spaced samples in the closed interval [start, stop]
//     */
//    public static double[] linspace(double start, double stop, int num) {
//        double step = (stop - start) / (num - 1);
//        double[] ret = new double[num];
//        for (int i = 0; i < num; i++) {
////ret[i] = start *
//            //TODO 미구현.
//        }
//    }
}
