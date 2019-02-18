package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AnObjectAction<T extends DBObject> extends DumbAwareAction {
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
    public void update(@NotNull AnActionEvent e) {
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
}
