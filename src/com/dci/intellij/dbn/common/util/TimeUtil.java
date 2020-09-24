package com.dci.intellij.dbn.common.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {
    public interface Millis{
        long ONE_SECOND = 1000;
        long FIVE_SECONDS = 5 * ONE_SECOND;
        long TEN_SECONDS = 10 * ONE_SECOND;
        long THIRTY_SECONDS = 30 * ONE_SECOND;
        long ONE_MINUTE = 60 * ONE_SECOND;
        long ONE_HOUR = 60 * ONE_MINUTE;
        long THREE_MINUTES = 3 * ONE_MINUTE;
        long FIVE_MINUTES = 5 * ONE_MINUTE;
        long TEN_MINUTES = 10 * ONE_MINUTE;
    }

    public static int getMinutes(int seconds) {
        return seconds / 60;
    }

    public static int getSeconds(int minutes) {
        return minutes * 60;
    }

    public static boolean isOlderThan(long timestamp, long millis) {
        return System.currentTimeMillis() - millis > timestamp;
    }

    public static boolean isOlderThan(long timestamp, long time, TimeUnit timeUnit) {
        return System.currentTimeMillis() - timeUnit.toMillis(time) > timestamp;
    }

    public static long millisSince(long start) {
        return System.currentTimeMillis() - start;
    }
}
