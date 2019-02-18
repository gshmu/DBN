package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.ui.Presentable;
import com.intellij.openapi.Disposable;

public interface DynamicContentElement extends Disposable, Comparable, Presentable {
    boolean isDisposed();
    int getOverload();
    void reload();
    void refresh();
    DynamicContentType getDynamicContentType();
}
