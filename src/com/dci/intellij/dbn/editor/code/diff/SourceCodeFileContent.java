package com.dci.intellij.dbn.editor.code.diff;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.contents.FileDocumentContentImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SourceCodeFileContent extends FileDocumentContentImpl implements DocumentContent {
    public SourceCodeFileContent(Project project, @NotNull DBVirtualFileImpl sourceCodeFile) {
        super(project, loadDocument(sourceCodeFile), sourceCodeFile);


        //boolean readonly = EnvironmentManager.getInstance(project).isReadonly(sourceCodeFile);
        //setReadOnly(readonly);
    }

    @NotNull
    private static Document loadDocument(@NotNull DBVirtualFileImpl sourceCodeFile) {
        return Failsafe.nn(DocumentUtil.getDocument(sourceCodeFile));
    }


}
