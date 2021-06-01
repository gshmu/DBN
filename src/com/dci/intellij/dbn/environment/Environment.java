package com.dci.intellij.dbn.environment;

public class Environment {
    public static boolean DEVELOPER_MODE = false;
    public static boolean DEBUG_MODE = false;
    public static boolean SLOW_DB_SIMULATION = false;


    public static boolean isSlowDatabaseModeEnabled() {
        return Environment.DEVELOPER_MODE && Environment.SLOW_DB_SIMULATION;
    }

    public static void setSlowDatabaseModeEnabled(boolean slowDatabaseModeEnabled) {
        Environment.SLOW_DB_SIMULATION = slowDatabaseModeEnabled;
    }
}
