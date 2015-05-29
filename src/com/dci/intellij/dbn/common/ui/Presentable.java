package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Presentable {
    @NotNull
    String getName();

    @Nullable
    String getDescription();

    @Nullable
    Icon getIcon();

    Presentable UNKNOWN = new Presentable() {
        @NotNull
        @Override
        public String getName() {
            return "Unknown";
        }

        @Nullable
        @Override
        public String getDescription() {
            return null;
        }

        @Nullable
        @Override
        public Icon getIcon() {
            return null;
        }
    };
}
