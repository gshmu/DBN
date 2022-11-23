package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.ui.Presentable;

public interface DynamicContentElement extends StatefulDisposable, Comparable, Presentable {

    default short getOverload() { return 0; }
    default void reload() {}
    default void refresh() {}
    DynamicContentType getDynamicContentType();
}
