package com.dci.intellij.dbn.editor.code.options;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.option.InteractiveOption;

public enum CodeEditorChangesOption implements InteractiveOption {
    ASK("Ask"),
    SAVE("Save"),
    DISCARD("Discard"),
    SHOW("Show Changes"),
    CANCEL("Cancel");

    private String name;

    CodeEditorChangesOption(String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
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

    @Override
    public boolean isCancel() {
        return this == CANCEL;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }


    public static CodeEditorChangesOption get(String name) {
        for (CodeEditorChangesOption option : CodeEditorChangesOption.values()) {
            if (option.name.equals(name) || option.name().equals(name)) {
                return option;
            }
        }
        return null;
    }}
