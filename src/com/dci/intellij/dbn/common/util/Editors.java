package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.ddl.DDLFileEditor;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBDatasetVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.JBTabsImpl;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class Editors {
    public static FileEditor selectEditor(@NotNull Project project, @Nullable FileEditor fileEditor, @NotNull VirtualFile virtualFile, EditorProviderId editorProviderId, NavigationInstructions instructions) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        if (fileEditor != null) {
            if (fileEditor instanceof DDLFileEditor) {
                DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                DBSchemaObject editableObject = fileAttachmentManager.getEditableObject(virtualFile);
                if (editableObject != null) {
                    virtualFile = editableObject.getVirtualFile();
                }
            }
            fileEditorManager.openFile(virtualFile, instructions.isFocus());

            if (fileEditor instanceof BasicTextEditor) {
                BasicTextEditor<?> basicTextEditor = (BasicTextEditor<?>) fileEditor;
                editorProviderId = basicTextEditor.getEditorProviderId();
                if (editorProviderId != null) {
                    fileEditorManager.setSelectedEditor(virtualFile, editorProviderId.getId());
                }
            }
        } else if (editorProviderId != null) {
            DBEditableObjectVirtualFile editableObjectFile = null;
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
            } else if (virtualFile.isInLocalFileSystem()) {
                DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                DBSchemaObject schemaObject = fileAttachmentManager.getEditableObject(virtualFile);
                if (schemaObject != null) {
                    editableObjectFile = schemaObject.getEditableVirtualFile();
                }
            }

            if (editableObjectFile != null && editableObjectFile.isValid()) {
                FileEditor[] fileEditors = instructions.isOpen() ?
                        fileEditorManager.openFile(editableObjectFile, instructions.isFocus()) :
                        fileEditorManager.getEditors(editableObjectFile);

                if (fileEditors.length > 0) {
                    fileEditorManager.setSelectedEditor(editableObjectFile, editorProviderId.getId());

                    for (FileEditor openFileEditor : fileEditors) {
                        if (openFileEditor instanceof BasicTextEditor) {
                            BasicTextEditor<?> basicTextEditor = (BasicTextEditor<?>) openFileEditor;
                            if (basicTextEditor.getEditorProviderId().equals(editorProviderId)) {
                                fileEditor = basicTextEditor;
                                break;
                            }
                        }
                    }
                }

            }
        } else if (virtualFile.isInLocalFileSystem()) {
            FileEditor[] fileEditors = instructions.isOpen() ?
                    fileEditorManager.openFile(virtualFile, instructions.isFocus()) :
                    fileEditorManager.getEditors(virtualFile);
            if (fileEditors.length > 0) {
                fileEditor = fileEditors[0];
            }
        }

        if (instructions.isFocus() && fileEditor != null) {
            focusEditor(fileEditor);
        }

        return fileEditor;
    }

    public static void setEditorIcon(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor, Icon icon) {
        JBTabsImpl tabs = getEditorTabComponent(project, virtualFile, fileEditor);
        if (tabs != null) {
            TabInfo tabInfo = getEditorTabInfo(tabs, fileEditor.getComponent());
            if (tabInfo != null) {
                tabInfo.setIcon(icon);
            }
        }
    }

    @Nullable
    private static JBTabsImpl getEditorTabComponent(@NotNull Project project, @NotNull VirtualFile virtualFile, FileEditor fileEditor) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = fileEditorManager.getSelectedEditor(virtualFile);
        if (selectedEditor == null) {
            if (virtualFile.isInLocalFileSystem()) {
                DDLFileAttachmentManager ddlFileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                DBSchemaObject schemaObject = ddlFileAttachmentManager.getEditableObject(virtualFile);
                if (schemaObject != null) {
                    DBEditableObjectVirtualFile objectVirtualFile = schemaObject.getEditableVirtualFile();
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
    public static BasicTextEditor<?> getTextEditor(DBSourceCodeVirtualFile sourceCodeVirtualFile) {
        DBEditableObjectVirtualFile databaseFile = sourceCodeVirtualFile.getMainDatabaseFile();
        Project project = databaseFile.getProject();
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        FileEditor[] fileEditors = editorManager.getEditors(databaseFile);
        for (FileEditor fileEditor : fileEditors) {
            if (fileEditor instanceof BasicTextEditor) {
                BasicTextEditor<?> basicTextEditor = (BasicTextEditor<?>) fileEditor;
                VirtualFile file = FileDocumentManager.getInstance().getFile(basicTextEditor.getEditor().getDocument());
                if (file!= null && file.equals(sourceCodeVirtualFile)) {
                    return basicTextEditor;
                }
            }
        }
        return null;
    }

    @Nullable
    public static Editor getEditor(FileEditor fileEditor) {
        Editor editor = null;
        if (fileEditor instanceof TextEditor) {
            TextEditor textEditor = (TextEditor) fileEditor;
            editor = textEditor.getEditor();
        } else if (fileEditor instanceof BasicTextEditor) {
            BasicTextEditor<?> textEditor = (BasicTextEditor<?>) fileEditor;
            editor = textEditor.getEditor();

        }
        return editor != null && !editor.isDisposed() ? editor : null;
    }

    public static FileEditor getFileEditor(@Nullable Editor editor) {
        if (editor != null) {
            Project project = editor.getProject();
            if (project != null) {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                FileEditor[] allEditors = fileEditorManager.getAllEditors();
                for (FileEditor fileEditor : allEditors) {
                    if (editor == getEditor(fileEditor)) {
                        return fileEditor;
                    }
                }
            }
        }
        return null;
    }

    public static void initEditorHighlighter(
            @NotNull Editor editor,
            @NotNull TextContentType contentType) {
        if (editor instanceof EditorEx) {
            EditorEx editorEx = (EditorEx) editor;
            SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(contentType.getFileType(), editor.getProject(), null);
            EditorColorsScheme colorsScheme = editor.getColorsScheme();
            EditorHighlighter highlighter = HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme);
            editorEx.setHighlighter(highlighter);
        }
    }

    public static void initEditorHighlighter(
            @NotNull Editor editor,
            @NotNull DBLanguage language,
            @Nullable ConnectionHandler connection) {
        DBLanguageDialect languageDialect = connection == null ?
                        language.getMainLanguageDialect() :
                        connection.getLanguageDialect(language);

        initEditorHighlighter(editor, languageDialect);
    }

    public static void initEditorHighlighter(
            @NotNull Editor editor,
            @NotNull DBLanguage language,
            @NotNull DBObject object) {
        DBLanguageDialect languageDialect = object.getLanguageDialect(language);
        initEditorHighlighter(editor, languageDialect);
    }

    private static void initEditorHighlighter(Editor editor, DBLanguageDialect languageDialect) {
        if (editor instanceof EditorEx) {
            EditorEx editorEx = (EditorEx) editor;
            SyntaxHighlighter syntaxHighlighter = languageDialect.getSyntaxHighlighter();

            EditorColorsScheme colorsScheme = editorEx.getColorsScheme();
            EditorHighlighter highlighter = HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme);
            editorEx.setHighlighter(highlighter);
        }
    }

    public static void setEditorReadonly(Editor editor, boolean readonly) {
        if (editor instanceof EditorEx) {
            EditorEx editorEx = (EditorEx) editor;
            editorEx.setViewer(readonly);
            EditorColorsScheme scheme = editor.getColorsScheme();
            Dispatch.runConditional(() -> {
                Color defaultBackground = Colors.getEditorBackground();
                editorEx.setBackgroundColor(readonly ? Colors.lafDarker(defaultBackground, 1) : defaultBackground);
                scheme.setColor(EditorColors.CARET_ROW_COLOR, readonly ?
                        Colors.lafDarker(defaultBackground, 3) :
                        Colors.getEditorCaretRowBackground());
            });
        }
    }

    public static void setEditorsReadonly(DBContentVirtualFile contentFile, boolean readonly) {
        Project project = Failsafe.nn(contentFile.getProject());

        if (contentFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) contentFile;
            Read.run(() -> {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                FileEditor[] allEditors = fileEditorManager.getAllEditors();
                Dispatch.run(() -> {
                    for (FileEditor fileEditor : allEditors) {
                        if (fileEditor instanceof SourceCodeEditor) {
                            SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                            DBSourceCodeVirtualFile virtualFile = sourceCodeEditor.getVirtualFile();
                            if (virtualFile.equals(sourceCodeFile)) {
                                setEditorReadonly(sourceCodeEditor.getEditor(), readonly);
                            }
                        }
                    }
                });
            });
        } else if (contentFile instanceof DBDatasetVirtualFile) {
            DBDatasetVirtualFile datasetFile = (DBDatasetVirtualFile) contentFile;
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            FileEditor[] allEditors = fileEditorManager.getAllEditors();
            for (FileEditor fileEditor : allEditors) {
                if (fileEditor instanceof DatasetEditor) {
                    DatasetEditor datasetEditor = (DatasetEditor) fileEditor;
                    if (datasetEditor.getDatabaseFile().equals(datasetFile.getMainDatabaseFile())) {
                        datasetEditor.getEditorTable().cancelEditing();
                        datasetEditor.setEnvironmentReadonly(readonly);
                    }
                }
            }
        }
    }

    @Nullable
    public static BasicTextEditor<?> getTextEditor(DBConsoleVirtualFile consoleVirtualFile) {
        Project project = consoleVirtualFile.getProject();
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        FileEditor[] fileEditors = editorManager.getEditors(consoleVirtualFile);
        for (FileEditor fileEditor : fileEditors) {
            if (fileEditor instanceof BasicTextEditor) {
                BasicTextEditor<?> basicTextEditor = (BasicTextEditor<?>) fileEditor;
                VirtualFile file = FileDocumentManager.getInstance().getFile(basicTextEditor.getEditor().getDocument());
                if (file!= null && file.equals(consoleVirtualFile)) {
                    return basicTextEditor;
                }
            }
        }
        return null;
    }

    /**
     * get all open editors for a virtual file including the attached ddl files
     */
    public static List<FileEditor> getScriptFileEditors(Project project, VirtualFile virtualFile) {
        assert virtualFile.isInLocalFileSystem();

        List<FileEditor> scriptFileEditors = new ArrayList<>();
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        FileEditor[] fileEditors = editorManager.getAllEditors(virtualFile);
        for (FileEditor fileEditor : fileEditors) {
            if (fileEditor instanceof TextEditor) {
                TextEditor textEditor = (TextEditor) fileEditor;
                scriptFileEditors.add(textEditor);
            }
        }
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        DBSchemaObject schemaObject = fileAttachmentManager.getEditableObject(virtualFile);
        if (schemaObject != null) {
            DBEditableObjectVirtualFile editableObjectFile = schemaObject.getEditableVirtualFile();
            fileEditors = editorManager.getAllEditors(editableObjectFile);
            for (FileEditor fileEditor : fileEditors) {
                if (fileEditor instanceof DDLFileEditor) {
                    DDLFileEditor ddlFileEditor = (DDLFileEditor) fileEditor;
                    Editor editor = ddlFileEditor.getEditor();
                    PsiFile psiFile = PsiUtil.getPsiFile(project, editor.getDocument());
                    if (psiFile != null && psiFile.getVirtualFile().equals(virtualFile)) {
                        scriptFileEditors.add(ddlFileEditor);
                    }
                }
            }
        }

        return scriptFileEditors;
    }

    public static Editor getSelectedEditor(Project project) {
        if (project != null) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            FileEditor[] fileEditors = fileEditorManager.getSelectedEditors();
            if (fileEditors.length == 1) {
                if (fileEditors[0] instanceof BasicTextEditor) {
                    BasicTextEditor<?> textEditor = (BasicTextEditor<?>) fileEditors[0];
                    return textEditor.getEditor();
                }
            }
            return fileEditorManager.getSelectedTextEditor();
        }
        return null;
    }

    public static Editor getSelectedEditor(Project project, FileType fileType){
        final Editor editor = Editors.getSelectedEditor(project);
        if (editor != null) {
            VirtualFile virtualFile = Documents.getVirtualFile(editor);
            if (virtualFile != null && virtualFile.getFileType().equals(fileType)) {
                return editor;
            }
        }
        return null;
    }

    private static void focusEditor(@Nullable FileEditor fileEditor) {
        if (fileEditor != null) {
            Editor editor = getEditor(fileEditor);
            focusEditor(editor);
        }
    }
    public static void focusEditor(@Nullable Editor editor) {
        if (editor != null) {
            Dispatch.run(() -> {
                Project project = editor.getProject();
                IdeFocusManager ideFocusManager = IdeFocusManager.getInstance(project);
                ideFocusManager.requestFocus(editor.getContentComponent(), true);
            });
        }
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
                    BasicTextEditor<?> basicTextEditor = (BasicTextEditor<?>) fileEditors[0];
                    return basicTextEditor.getVirtualFile();
                }
            }

            Editor editor = fileEditorManager.getSelectedTextEditor();
            if (editor != null) {
                return Documents.getVirtualFile(editor);
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

    public static void releaseEditor(@Nullable Editor editor) {
        if (editor != null) {
            Dispatch.runConditional(() -> {
                EditorFactory editorFactory = EditorFactory.getInstance();
                editorFactory.releaseEditor(editor);
            });
        }

    }

}
