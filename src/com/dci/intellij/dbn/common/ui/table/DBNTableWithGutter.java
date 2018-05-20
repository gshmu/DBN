package com.dci.intellij.dbn.common.ui.table;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

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
