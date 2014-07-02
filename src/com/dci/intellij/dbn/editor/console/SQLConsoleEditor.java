package com.dci.intellij.dbn.editor.console;

import com.dci.intellij.dbn.common.editor.BasicTextEditorImpl;
import com.dci.intellij.dbn.common.editor.BasicTextEditorState;
import com.dci.intellij.dbn.vfs.SQLConsoleFile;
import com.intellij.openapi.project.Project;

public class SQLConsoleEditor extends BasicTextEditorImpl<SQLConsoleFile>{
    public SQLConsoleEditor(Project project, SQLConsoleFile sqlConsoleFile, String name) {
        super(project, sqlConsoleFile, name);
    }

    @Override
    protected BasicTextEditorState createEditorState() {
        return new SQLConsoleEditorState();
    }

}
