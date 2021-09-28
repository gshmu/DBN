package com.dci.intellij.dbn.environment;

public final class Environment {
    public static boolean DEVELOPER_MODE = false;
    public static boolean PARSER_DEBUG_MODE = false;
    public static boolean DATABASE_ACCESS_DEBUG_MODE = false;
    public static boolean DATABASE_RESOURCE_DEBUG_MODE = false;
    public static boolean DATABASE_LAGGING_MODE = false;


    public static void updateDeveloperMode(boolean state) {
        DEVELOPER_MODE = state;
        if (!state) {
            PARSER_DEBUG_MODE = false;
            DATABASE_ACCESS_DEBUG_MODE = false;
            DATABASE_RESOURCE_DEBUG_MODE = false;
            DATABASE_LAGGING_MODE = false;
        }
    }
}
