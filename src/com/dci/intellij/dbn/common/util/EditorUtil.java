package com.dci.intellij.dbn.common.util;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectVirtualFile;
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

public class EditorUtil {
    public static void selectEditor(DatabaseEditableObjectVirtualFile databaseFile, FileEditor fileEditor) {
        JBTabsImpl tabs = getEditorTabComponent(databaseFile);
        TabInfo tabInfo = getEditorTabInfo(tabs, fileEditor);
        if (tabInfo != null) {
            tabs.getJBTabs().select(tabInfo, true);
        }
    }

    public static void setEditorIcon(DatabaseEditableObjectVirtualFile databaseFile, FileEditor fileEditor, Icon icon) {
        JBTabsImpl tabs = getEditorTabComponent(databaseFile);
        TabInfo tabInfo = getEditorTabInfo(tabs, fileEditor);
        if (tabInfo != null) {
            tabInfo.setIcon(icon);
        }
    }

    private static JBTabsImpl getEditorTabComponent(DatabaseEditableObjectVirtualFile databaseFile) {
        FileEditor selectedEditor = getSelectedEditor(databaseFile);
        return selectedEditor == null ? null : (JBTabsImpl) getParentComponent(selectedEditor.getComponent(), JBTabsImpl.class);
    }

    private static TabInfo getEditorTabInfo(JBTabsImpl tabs, FileEditor fileEditor) {
        if (tabs != null) {
            Component editorComponent = getParentComponent(fileEditor.getComponent(), TabbedPaneWrapper.TabWrapper.class);
            List<TabInfo> tabInfos = tabs.getTabs();
            for (TabInfo tabInfo : tabInfos) {
                if (tabInfo.getComponent() == editorComponent) {
                    return tabInfo;
                }
            }
        }
        return null;
    }


    private static Component getParentComponent(Component component, Class parentClass) {
        while (component != null && /*!parentClass.isAssignableFrom(component.getClass())*/ parentClass != component.getClass()) {
            component = component.getParent();
        }
        return component;
    }

    public static BasicTextEditor getFileEditor(DatabaseEditableObjectVirtualFile databaseFile, VirtualFile virtualFile) {
        FileEditorManager editorManager = FileEditorManager.getInstance(databaseFile.getProject());
        FileEditor[] fileEditors = editorManager.getEditors(databaseFile);
        for (FileEditor fileEditor : fileEditors) {
            if (fileEditor instanceof BasicTextEditor) {
                BasicTextEditor basicTextEditor = (BasicTextEditor) fileEditor;
                VirtualFile file = FileDocumentManager.getInstance().getFile(basicTextEditor.getEditor().getDocument());
                if (file!= null && file.equals(virtualFile)) {
                    return basicTextEditor;
                }
            }
        }
        return null;
    }

    public static FileEditor getSelectedEditor(DatabaseEditableObjectVirtualFile databaseFile) {
        return FileEditorManager.getInstance(databaseFile.getProject()).getSelectedEditor(databaseFile);
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
