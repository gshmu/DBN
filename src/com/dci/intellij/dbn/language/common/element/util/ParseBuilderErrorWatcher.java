package com.dci.intellij.dbn.language.common.element.util;

public class ParseBuilderErrorWatcher {
    private static final ThreadLocal<Integer> offset = new ThreadLocal<>();
    private static final ThreadLocal<Long> timestamp = new ThreadLocal<>();

    public static boolean show(int offset, long timestamp) {
        boolean show =
                ParseBuilderErrorWatcher.offset.get() == null ||
                ParseBuilderErrorWatcher.offset.get() != offset ||
                ParseBuilderErrorWatcher.timestamp.get() == null ||
                ParseBuilderErrorWatcher.timestamp.get() != timestamp;
        if (show) {
            ParseBuilderErrorWatcher.offset.set(offset);
            ParseBuilderErrorWatcher.timestamp.set(timestamp);
        }
        return show;
    }
}
