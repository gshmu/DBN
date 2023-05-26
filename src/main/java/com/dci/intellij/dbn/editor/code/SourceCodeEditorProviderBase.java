package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.editor.BasicTextEditorProvider;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.common.exception.ProcessDeferredException;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.DatabaseFileEditorManager;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

abstract class SourceCodeEditorProviderBase extends BasicTextEditorProvider implements DumbAware {
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof DBSourceCodeVirtualFile) {
            // accept provider if invoked for a child file (ide invocations)
            //  => custom handling when createEditor(...) is invoked
            DBSourceCodeVirtualFile sourceCodeVirtualFile = (DBSourceCodeVirtualFile) virtualFile;
            DBContentType contentType = sourceCodeVirtualFile.getContentType();
            return contentType == getContentType();
        }
        return false;
    }


    @Override
    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        DBEditableObjectVirtualFile databaseFile;

        EditorProviderId editorProviderId = getEditorProviderId();
        if (file instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) file;
            databaseFile = sourceCodeFile.getMainDatabaseFile();
            DBSchemaObject object = databaseFile.getObject();

            DatabaseFileEditorManager editorManager = DatabaseFileEditorManager.getInstance(project);
            editorManager.connectAndOpenEditor(object, editorProviderId, false, true);
            throw new ProcessDeferredException();
        } else {
            databaseFile = (DBEditableObjectVirtualFile) file;
        }

        DBSourceCodeVirtualFile sourceCodeFile = Failsafe.nn(getSourceCodeFile(databaseFile));
        boolean isMainEditor = sourceCodeFile.getContentType() == databaseFile.getMainContentType();

        String editorName = getName();
        SourceCodeEditor sourceCodeEditor = isMainEditor ?
                new SourceCodeMainEditor(project, sourceCodeFile, editorName, editorProviderId) :
                new SourceCodeEditor(project, sourceCodeFile, editorName, editorProviderId);

        Editor editor = sourceCodeEditor.getEditor();
        Document document = editor.getDocument();

        EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
        if (environmentManager.isReadonly(sourceCodeFile) || !sourceCodeFile.isLoaded()) {
            Editors.setEditorReadonly(editor, true);
        }

        int documentSignature = document.hashCode();
        if (document.hashCode() != sourceCodeFile.getDocumentSignature()) {
            document.addDocumentListener(sourceCodeFile);
            sourceCodeFile.setDocumentSignature(documentSignature);
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

    @Nullable
    private DBSourceCodeVirtualFile getSourceCodeFile(DBEditableObjectVirtualFile databaseFile) {
        return databaseFile.getContentFile(getContentType());
    }

    public abstract DBContentType getContentType();

    public abstract String getName();

    public abstract Icon getIcon();

    @Override
    public void disposeEditor(@NotNull FileEditor editor) {
        Disposer.dispose(editor);
    }
}
