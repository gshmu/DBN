package com.dci.intellij.dbn.common.action;

import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DumbAwareProjectAction extends ProjectAction implements DumbAware {

    public DumbAwareProjectAction() {
    }

    public DumbAwareProjectAction(@Nullable String text) {
        super(text);
    }

    public DumbAwareProjectAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }
}
