package com.dci.intellij.dbn.plugin;

public enum DBPluginStatus {
    MISSING, // not installed
    PASSIVE, // installed but no connections configured
    ACTIVE   // installed and connections configured
}
