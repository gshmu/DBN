package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.ui.Presentable;

public interface DynamicContentElement extends StatefulDisposable, Comparable, Presentable {
    short getOverload();
    void reload();
    void refresh();
    DynamicContentType getDynamicContentType();
}
