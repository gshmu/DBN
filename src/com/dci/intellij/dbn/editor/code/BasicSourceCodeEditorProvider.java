package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.editor.BasicTextEditorProvider;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeEditorActionsPanel;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.ide.DataManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public abstract class BasicSourceCodeEditorProvider extends BasicTextEditorProvider implements DumbAware {
    @Override
    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        DBEditableObjectVirtualFile databaseFile;

        if (file instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) file;
            databaseFile = sourceCodeFile.getMainDatabaseFile();
        } else {
            databaseFile = (DBEditableObjectVirtualFile) file;
        }

        DBSourceCodeVirtualFile sourceCodeFile = Failsafe.nn(getSourceCodeFile(databaseFile));
        boolean isMainEditor = sourceCodeFile.getContentType() == databaseFile.getMainContentType();

        String editorName = getName();
        SourceCodeEditor sourceCodeEditor = isMainEditor ?
                new SourceCodeMainEditor(project, sourceCodeFile, editorName, getEditorProviderId()) :
                new SourceCodeEditor(project, sourceCodeFile, editorName, getEditorProviderId());

        updateEditorActions(sourceCodeEditor);
        Document document = sourceCodeEditor.getEditor().getDocument();

        EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
        if (environmentManager.isReadonly(sourceCodeFile) || !sourceCodeFile.isLoaded()) {
            EditorUtil.setEditorReadonly(sourceCodeEditor, true);
        }

        int documentTracking = document.hashCode();
        if (document.hashCode() != sourceCodeFile.getDocumentHashCode()) {
            document.addDocumentListener(sourceCodeFile);
            sourceCodeFile.setDocumentHashCode(documentTracking);
        }

        Icon icon = getIcon();
        if (icon != null) {
            updateTabIcon(databaseFile, sourceCodeEditor, icon);
        }
        return sourceCodeEditor;
    }

    @Override
    public VirtualFile getContentVirtualFile(VirtualFile virtualFile) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile objectVirtualFile = (DBEditableObjectVirtualFile) virtualFile;
            return objectVirtualFile.getContentFile(getContentType());
        }
        return super.getContentVirtualFile(virtualFile);
    }

    private BasicTextEditor lookupExistingEditor(Project project, DBEditableObjectVirtualFile databaseFile) {
        FileEditor[] fileEditors = FileEditorManager.getInstance(project).getEditors(databaseFile);
        if (fileEditors.length > 0) {
            for (FileEditor fileEditor : fileEditors) {
                if (fileEditor instanceof SourceCodeEditor) {
                    SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                    if (sourceCodeEditor.getVirtualFile().getContentType() == getContentType()) {
                        return sourceCodeEditor;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private DBSourceCodeVirtualFile getSourceCodeFile(DBEditableObjectVirtualFile databaseFile) {
        return (DBSourceCodeVirtualFile) databaseFile.getContentFile(getContentType());
    }

    public abstract DBContentType getContentType();

    public abstract String getName();

    public abstract Icon getIcon();

    private static void updateEditorActions(@NotNull SourceCodeEditor sourceCodeEditor) {
        Editor editor = sourceCodeEditor.getEditor();
        JComponent editorComponent = editor.getComponent();
        SourceCodeEditorActionsPanel actionsPanel = new SourceCodeEditorActionsPanel(sourceCodeEditor);
        DataManager.registerDataProvider(actionsPanel.getComponent(), sourceCodeEditor);
        //FileEditorManager.getInstance(editor.getProject()).addTopComponent(fileEditor, actionToolbar.getComponent());
        editorComponent.getParent().add(actionsPanel.getComponent(), BorderLayout.NORTH);
        Disposer.register(sourceCodeEditor, actionsPanel);
    }

    @Override
    public void disposeEditor(@NotNull FileEditor editor) {
        Disposer.dispose(editor);
    }
}
