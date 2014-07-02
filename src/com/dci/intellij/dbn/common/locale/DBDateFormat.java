package com.dci.intellij.dbn.common.locale;

import java.text.DateFormat;

public enum DBDateFormat {
    SHORT(DateFormat.SHORT),
    MEDIUM(DateFormat.MEDIUM),
    LONG(DateFormat.LONG),
    CUSTOM(0);

    private int dateFormat;

    private DBDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    public int getDateFormat() {
        return dateFormat;
    }
}
