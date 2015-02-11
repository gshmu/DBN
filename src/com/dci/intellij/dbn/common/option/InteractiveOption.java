package com.dci.intellij.dbn.common.option;

import javax.swing.Icon;

public interface InteractiveOption {
    String getName();

    Icon getIcon();

    boolean isPersistable();
}
