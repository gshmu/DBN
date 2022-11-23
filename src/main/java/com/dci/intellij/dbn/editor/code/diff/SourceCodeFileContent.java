package com.dci.intellij.dbn.editor.code.diff;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.vfs.DBVirtualFileBase;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.FileDocumentContentImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SourceCodeFileContent extends FileDocumentContentImpl implements DocumentContent {
    public SourceCodeFileContent(Project project, @NotNull DBVirtualFileBase sourceCodeFile) {
        super(project, loadDocument(sourceCodeFile), sourceCodeFile);


        //boolean readonly = EnvironmentManager.getInstance(project).isReadonly(sourceCodeFile);
        //setReadOnly(readonly);
    }

    @NotNull
    private static Document loadDocument(@NotNull DBVirtualFileBase sourceCodeFile) {
        return Failsafe.nn(Documents.getDocument(sourceCodeFile));
    }


}
