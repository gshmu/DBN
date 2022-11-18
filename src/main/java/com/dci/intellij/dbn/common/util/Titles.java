package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.connection.ConnectionHandler;

public final class Titles {
    public static final String PRODUCT_NAME = "DB Navigator";
    public static final String TITLE_PREFIX = PRODUCT_NAME + " - ";

    private Titles() {}

    public static String signed(String title) {
        return TITLE_PREFIX + title;
    }

    public static String suffixed(String title, ConnectionHandler connection) {
        return title + " (" + connection.getName() + ")";
    }
}
