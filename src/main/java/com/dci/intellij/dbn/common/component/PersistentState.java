package com.dci.intellij.dbn.common.component;

import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PersistentState extends PersistentStateComponent<Element> {
    @Override
    @Nullable
    default Element getState() {
        return Unsafe.warned(null, () -> getComponentState());
    }

    @Override
    default void loadState(@NotNull Element state) {
        Unsafe.warned(() -> loadComponentState(state));
    }

    Element getComponentState();

    void loadComponentState(@NotNull Element state);
}
