package com.dci.intellij.dbn.common.component;

import com.dci.intellij.dbn.common.dispose.Nullifier;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.util.Disposer;

public interface LegacyComponent extends BaseComponent, Disposable {
    @Override
    default void initComponent() {

    }

    @Override
    default void disposeComponent() {
        Disposer.dispose(this);
        Nullifier.nullify(this);
    }
}
