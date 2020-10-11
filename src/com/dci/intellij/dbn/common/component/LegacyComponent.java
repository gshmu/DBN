package com.dci.intellij.dbn.common.component;

import com.intellij.openapi.components.BaseComponent;

public interface LegacyComponent extends BaseComponent {
    @Override
    default void initComponent() {

    }

    @Override
    default void disposeComponent() {

    }
}
