package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.action.DumbAwareContextAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AnObjectAction<T extends DBObject> extends DumbAwareContextAction<T> {
    private DBObjectRef<T> objectRef;
    private boolean custom;

    public AnObjectAction(String text, Icon icon, @NotNull T object) {
        super(text, null, icon);
        objectRef = DBObjectRef.of(object);
        custom = true;
    }
    public AnObjectAction(@NotNull T object) {
        super(object.getName(), null, object.getIcon());
        objectRef = DBObjectRef.of(object);
    }

    @Override
    protected T getTarget(@NotNull AnActionEvent e) {
        return getTarget();
    }

    public T getTarget() {
        return DBObjectRef.get(objectRef);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable T target) {
        if (!custom) {
            if (target != null) {
                presentation.setText(target.getName(), false);
            }
        }
    }
}
