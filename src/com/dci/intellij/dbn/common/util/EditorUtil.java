package com.dci.intellij.dbn.common.util;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.ui.UIUtil;

public class EditorUtil {
    public static void selectEditor(@NotNull Project project, @NotNull VirtualFile virtualFile, @Nullable FileEditor fileEditor, boolean requestFocus) {
        JBTabsImpl tabs = getEditorTabComponent(project, virtualFile);
        if (tabs != null) {
            if (fileEditor != null) {
                TabInfo tabInfo = getEditorTabInfo(tabs, fileEditor.getComponent());
                if (tabInfo != null) {
                    tabs.getJBTabs().select(tabInfo, requestFocus);
                }
            }
        }
    }

    public static void setEditorIcon(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor, Icon icon) {
        JBTabsImpl tabs = getEditorTabComponent(project, virtualFile);
        if (tabs != null) {
            TabInfo tabInfo = getEditorTabInfo(tabs, fileEditor.getComponent());
            if (tabInfo != null) {
                tabInfo.setIcon(icon);
            }
        }
    }

    @Nullable
    private static JBTabsImpl getEditorTabComponent(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = fileEditorManager.getSelectedEditor(virtualFile);
        if (selectedEditor == null) {
            if (virtualFile.isInLocalFileSystem()) {
                DDLFileAttachmentManager ddlFileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                DBSchemaObject editableObject = ddlFileAttachmentManager.getEditableObject(virtualFile);
                if (editableObject != null) {
                    DBEditableObjectVirtualFile objectVirtualFile = editableObject.getVirtualFile();
                    selectedEditor = fileEditorManager.getSelectedEditor(objectVirtualFile);
                }
            }
        }
        if (selectedEditor != null) {
            return UIUtil.getParentOfType(JBTabsImpl.class, selectedEditor.getComponent());
        }
        return null;
    }

    @Nullable
    private static TabInfo getEditorTabInfo(@NotNull JBTabsImpl tabs, JComponent editorComponent) {
        Component wrapperComponent = UIUtil.getParentOfType(TabbedPaneWrapper.TabWrapper.class, editorComponent);
        List<TabInfo> tabInfos = tabs.getTabs();
        for (TabInfo tabInfo : tabInfos) {
            if (tabInfo.getComponent() == wrapperComponent) {
                return tabInfo;
            }
        }
        return null;
    }

    @Nullable
    public static BasicTextEditor getTextEditor(DBEditableObjectVirtualFile databaseFile, DBSourceCodeVirtualFile sourceCodeVirtualFile) {
        FileEditorManager editorManager = FileEditorManager.getInstance(databaseFile.getProject());
        FileEditor[] fileEditors = editorManager.getEditors(databaseFile);
        for (FileEditor fileEditor : fileEditors) {
            if (fileEditor instanceof BasicTextEditor) {
                BasicTextEditor basicTextEditor = (BasicTextEditor) fileEditor;
                VirtualFile file = FileDocumentManager.getInstance().getFile(basicTextEditor.getEditor().getDocument());
                if (file!= null && file.equals(sourceCodeVirtualFile)) {
                    return basicTextEditor;
                }
            }
        }
        return null;
    }

    @Nullable
    public static BasicTextEditor getTextEditor(DBConsoleVirtualFile consoleVirtualFile) {
        FileEditorManager editorManager = FileEditorManager.getInstance(consoleVirtualFile.getProject());
        FileEditor[] fileEditors = editorManager.getEditors(consoleVirtualFile);
        for (FileEditor fileEditor : fileEditors) {
            if (fileEditor instanceof BasicTextEditor) {
                BasicTextEditor basicTextEditor = (BasicTextEditor) fileEditor;
                VirtualFile file = FileDocumentManager.getInstance().getFile(basicTextEditor.getEditor().getDocument());
                if (file!= null && file.equals(consoleVirtualFile)) {
                    return basicTextEditor;
                }
            }
        }
        return null;
    }

    public static Editor getSelectedEditor(Project project) {
        if (project != null) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            FileEditor[] fileEditors = fileEditorManager.getSelectedEditors();
            if (fileEditors.length == 1) {
                if (fileEditors[0] instanceof BasicTextEditor) {
                    BasicTextEditor textEditor = (BasicTextEditor) fileEditors[0];
                    return textEditor.getEditor();
                }
            }
            return fileEditorManager.getSelectedTextEditor();
        }
        return null;
    }

    public static Editor getSelectedEditor(Project project, FileType fileType){
        final Editor editor = EditorUtil.getSelectedEditor(project);
        if (editor != null && DocumentUtil.getVirtualFile(editor).getFileType().equals(fileType)) {
            return editor;
        }
        return null;
    }

    public static VirtualFile getSelectedFile(Project project) {
        if (project != null) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            FileEditor[] fileEditors = fileEditorManager.getSelectedEditors();
            if (fileEditors.length > 0) {
                if (fileEditors[0] instanceof DatasetEditor) {
                    DatasetEditor datasetEditor = (DatasetEditor) fileEditors[0];
                    return datasetEditor.getDatabaseFile();
                } else if (fileEditors[0] instanceof BasicTextEditor) {
                    BasicTextEditor basicTextEditor = (BasicTextEditor) fileEditors[0];
                    return basicTextEditor.getVirtualFile();
                }
            }

            Editor editor = fileEditorManager.getSelectedTextEditor();
            if (editor != null) {
                return DocumentUtil.getVirtualFile(editor);
            }
        }
        return null;
    }

    public static boolean hasEditingHistory(VirtualFile virtualFile, Project project) {
        FileEditorProviderManager editorProviderManager = FileEditorProviderManager.getInstance();
        FileEditorProvider[] providers = editorProviderManager.getProviders(project, virtualFile);
        FileEditorState editorState = EditorHistoryManager.getInstance(project).getState(virtualFile, providers[0]);
        return editorState != null;
    }

    public static Dimension calculatePreferredSize(Editor editor) {
        int maxLength = 0;

        Document document = editor.getDocument();
        for (int i=0; i< document.getLineCount(); i++) {
            int length = document.getLineEndOffset(i) - document.getLineStartOffset(i);
            if (length > maxLength) {
                maxLength = length;
            }
        }

        int charWidth = com.intellij.openapi.editor.ex.util.EditorUtil.getSpaceWidth(Font.PLAIN, editor);

        int width = (charWidth + 1) * maxLength; // mono spaced fonts here
        int height = (editor.getLineHeight()) * document.getLineCount();
        return new Dimension(width, height);
    }
}
