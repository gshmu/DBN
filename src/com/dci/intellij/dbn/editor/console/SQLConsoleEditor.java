package com.dci.intellij.dbn.editor.console;

import com.dci.intellij.dbn.common.editor.BasicTextEditorImpl;
import com.dci.intellij.dbn.common.editor.BasicTextEditorState;
import com.dci.intellij.dbn.vfs.SQLConsoleVirtualFile;
import com.intellij.openapi.project.Project;

public class SQLConsoleEditor extends BasicTextEditorImpl<SQLConsoleVirtualFile>{
    public SQLConsoleEditor(Project project, SQLConsoleVirtualFile sqlConsoleFile, String name) {
        super(project, sqlConsoleFile, name);
    }

    @Override
    protected BasicTextEditorState createEditorState() {
        return new SQLConsoleEditorState();
    }

}
