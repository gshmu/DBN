package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.ui.form.DBNForm;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;

import java.awt.*;

public interface Context {
    static DataContext getDataContext(DBNForm form) {
        return getDataContext(form.getComponent());
    }

    static DataContext getDataContext(Editor editor) {
        return getDataContext(editor.getComponent());
    }

    static DataContext getDataContext(Component component) {
        return DataManager.getInstance().getDataContext(component);
    }
}
