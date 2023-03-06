package com.dci.intellij.dbn.object.properties;

import com.intellij.pom.Navigatable;

import javax.swing.*;

import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;

public abstract class PresentableProperty {
    public abstract String getName();

    public abstract String getValue();

    public abstract Icon getIcon();

    public String toString() {
        return guarded("DISPOSED", this, p -> p.getName() + ": " + p.getValue());
    }

    public abstract Navigatable getNavigatable();
}
