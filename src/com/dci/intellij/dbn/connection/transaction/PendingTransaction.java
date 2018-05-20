package com.dci.intellij.dbn.connection.transaction;

import javax.swing.*;

public class PendingTransaction {
    private String filePath;
    private String displayFilePath;
    private Icon icon;
    private int changesCount = 0;

    public PendingTransaction(String filePath, String displayFilePath, Icon icon) {
        this.filePath = filePath;
        this.displayFilePath = displayFilePath;
        this.icon = icon;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDisplayFilePath() {
        return displayFilePath;
    }

    public Icon getIcon() {
        return icon;
    }

    public int getChangesCount() {
        return changesCount;
    }

    public void incrementChangesCount() {
        changesCount++;
    }
}
