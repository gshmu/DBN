package com.dci.intellij.dbn.common.navigation;

import com.dci.intellij.dbn.common.property.Property;

public enum NavigationInstruction implements Property{
    OPEN,
    FOCUS,
    SCROLL,
    SELECT,
    RESET;
}
