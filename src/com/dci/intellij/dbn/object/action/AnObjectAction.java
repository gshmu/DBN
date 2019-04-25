package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AnObjectAction<T extends DBObject> extends DumbAwareProjectAction {
    private DBObjectRef<T> objectRef;
    private boolean custom;

    public AnObjectAction(String text, Icon icon, @NotNull T object) {
        super(text, null, icon);
        objectRef = DBObjectRef.from(object);
        custom = true;
    }
    public AnObjectAction(@NotNull T object) {
        super(object.getName(), null, object.getIcon());
        objectRef = DBObjectRef.from(object);
    }

    @Override
    protected final void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        T object = getObject();
        if (Failsafe.check(object)) {
            actionPerformed(e, project, object);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        if (!custom) {
            T object = getObject();
            if (object != null) {
                e.getPresentation().setText(object.getName(), false);
            }
        }
    }

    @Nullable
    public T getObject() {
        return DBObjectRef.get(objectRef);
    }

    protected abstract void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull T object);
}
