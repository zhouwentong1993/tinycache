package com.wentong.util;

import java.util.SplittableRandom;

public class RandomUtil {

    private RandomUtil(){}

    private static final SplittableRandom random = new SplittableRandom();

    private static final char[] arr = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static int LEN = arr.length;

    public static String generateRandomString(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(arr[random.nextInt(LEN)]);
        }
        return sb.toString();
    }

}
