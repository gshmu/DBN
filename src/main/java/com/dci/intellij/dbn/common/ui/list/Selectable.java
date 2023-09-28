package com.dci.intellij.dbn.common.ui.list;

import com.dci.intellij.dbn.common.ui.Presentable;

public interface Selectable<T> extends Presentable, Comparable<T> {
    String getError();
    boolean isSelected();
    boolean isMasterSelected();
    void setSelected(boolean selected);
}
