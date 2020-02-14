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
     * @param array    The array to pad.
     * @param width_tl Number of values padded top and left to the edges
     * @param width_br Number of values padded bottom and right  to the edges
     * @return Padded array of rank equal to array with shape increased according to pad_width.
     */
    public static double[][] pad_constant(double[][] array, int width_tl, int width_br) {
        double[][] ret = new double[array.length + width_tl + width_br][array[0].length + width_tl + width_br];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, ret[width_tl + i], width_tl, array[i].length);
        }
        return ret;
    }
}
