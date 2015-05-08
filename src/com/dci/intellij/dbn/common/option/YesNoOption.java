package com.dci.intellij.dbn.common.option;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum YesNoOption implements InteractiveOption {
    YES("Yes", true),
    NO("No", true);

    private String name;
    private boolean persistable;

    YesNoOption(String name, boolean persistable) {
        this.name = name;
        this.persistable = persistable;
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
        return false;
    }

    @Override
    public boolean isAsk() {
        return false;
    }


    public static YesNoOption get(String name) {
        for (YesNoOption option : YesNoOption.values()) {
            if (option.name.equals(name) || option.name().equals(name)) {
                return option;
            }
        }
        return null;
    }}
