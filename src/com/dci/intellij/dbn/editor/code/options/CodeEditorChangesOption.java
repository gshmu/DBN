package com.dci.intellij.dbn.editor.code.options;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.option.InteractiveOption;

public enum CodeEditorChangesOption implements InteractiveOption {
    ASK("Ask", false),
    SAVE("Save", true),
    DISCARD("Discard", true),
    SHOW("Show Changes", false),
    CANCEL("Cancel", false);

    private String name;
    private boolean persistable;

    CodeEditorChangesOption(String name, boolean persistable) {
        this.name = name;
        this.persistable = persistable;
    }

    @NotNull
    public String getName() {
        return name;
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
