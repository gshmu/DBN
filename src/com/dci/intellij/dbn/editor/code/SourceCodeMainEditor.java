package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.vfs.SourceCodeVirtualFile;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;

/**
 * Only main editor extends TextEditor to force navigation of breakpoints to the right editor (e.g. package body)
 */
public class SourceCodeMainEditor extends SourceCodeEditor implements TextEditor {
    public SourceCodeMainEditor(Project project, SourceCodeVirtualFile sourceCodeFile, String name) {
        super(project, sourceCodeFile, name);
    }
}
