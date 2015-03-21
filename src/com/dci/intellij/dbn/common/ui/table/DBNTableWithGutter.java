package com.dci.intellij.dbn.common.ui.table;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.Project;

public class DBNTableWithGutter<T extends DBNTableWithGutterModel> extends DBNTable<T>{
    public DBNTableWithGutter(Project project, T tableModel, boolean showHeader) {
        super(project, tableModel, showHeader);
    }

    @NotNull
    @Override
    public T getModel() {
        return super.getModel();
    }
}
