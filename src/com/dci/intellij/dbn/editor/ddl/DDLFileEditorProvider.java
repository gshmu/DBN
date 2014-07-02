package com.dci.intellij.dbn.editor.ddl;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.editor.BasicTextEditorProvider;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class DDLFileEditorProvider extends BasicTextEditorProvider implements DumbAware {

    private int index;
    private String componentName;

    public DDLFileEditorProvider(int index, String componentName) {
        this.index = index;
        this.componentName = componentName;
    }

    public int getIndex() {
        return index;
    }

    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof DatabaseEditableObjectFile) {
            DatabaseEditableObjectFile databaseFile = (DatabaseEditableObjectFile) virtualFile;
            List<VirtualFile> ddlFiles = databaseFile.getBoundDDLFiles();
            return ddlFiles != null && ddlFiles.size() > index;
        }
        return false;
    }

    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        DatabaseEditableObjectFile databaseFile = (DatabaseEditableObjectFile) file;
        VirtualFile virtualFile = databaseFile.getBoundDDLFiles().get(index);

        BasicTextEditor textEditor = new DDLFileEditor(project, virtualFile);
        updateTabIcon(databaseFile, textEditor, VirtualFileUtil.getIcon(virtualFile));
        return textEditor;
    }

    public void disposeEditor(@NotNull FileEditor editor) {
        DDLFileEditor sourceEditor = (DDLFileEditor) editor;
        Document document = sourceEditor.getEditor().getDocument();
        //DocumentUtil.removeGuardedBlock(document);
        sourceEditor.dispose();
    }

    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;

    }

    @NotNull
    @NonNls
    public String getEditorTypeId() {
        return "" + (index + 3);
    }

    public String getName() {
        return null;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/

    @NonNls
    @NotNull
    public String getComponentName() {
        return componentName;
    }
}
