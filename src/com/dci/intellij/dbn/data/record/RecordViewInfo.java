package com.dci.intellij.dbn.data.record;

import javax.swing.*;

public class RecordViewInfo {
    private String title;
    private Icon icon;

    public RecordViewInfo(String title, Icon icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public Icon getIcon() {
        return icon;
    }
}
