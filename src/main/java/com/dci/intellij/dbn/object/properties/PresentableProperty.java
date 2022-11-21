package com.dci.intellij.dbn.object.properties;

import com.dci.intellij.dbn.common.util.Guarded;
import com.intellij.pom.Navigatable;

import javax.swing.*;

public abstract class PresentableProperty {
    public abstract String getName();

    public abstract String getValue();

    public abstract Icon getIcon();

    public String toString() {
        return Guarded.call("DISPOSED", () -> getName() + ": " + getValue());
    }

    public abstract Navigatable getNavigatable();
}
