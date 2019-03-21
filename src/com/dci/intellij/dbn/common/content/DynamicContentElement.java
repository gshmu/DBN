package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.ui.Presentable;

public interface DynamicContentElement extends Disposable, Comparable, Presentable {
    int getOverload();
    void reload();
    void refresh();
    DynamicContentType getDynamicContentType();
}
