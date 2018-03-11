package com.dci.intellij.dbn.common.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {
    public static int ONE_SECOND = 1000;
    public static int TEN_SECONDS = 10 * ONE_SECOND;
    public static int THIRTY_SECONDS = 30 * ONE_SECOND;
    public static int ONE_MINUTE = 60 * ONE_SECOND;
    public static int ONE_HOUR = 60 * ONE_MINUTE;
    public static int THREE_MINUTES = 3 * ONE_MINUTE;
    public static int FIVE_MINUTES = 5 * ONE_MINUTE;

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

}
