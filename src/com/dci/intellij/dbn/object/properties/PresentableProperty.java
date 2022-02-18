package com.dci.intellij.dbn.object.properties;

import com.dci.intellij.dbn.common.util.Cancellable;
import com.intellij.pom.Navigatable;

import javax.swing.Icon;

public abstract class PresentableProperty {
    public abstract String getName();

    public abstract String getValue();

    public abstract Icon getIcon();

    public String toString() {
        return Cancellable.call("DISPOSED", () -> getName() + ": " + getValue());
    }

    public abstract Navigatable getNavigatable();
}
