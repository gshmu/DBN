package com.dci.intellij.dbn.editor.code.diff;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.diff.SimpleContent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;

public class SourceCodeFileContent extends SimpleContent {
    DBSourceCodeVirtualFile sourceCodeFile;
    public SourceCodeFileContent(Project project, @NotNull DBSourceCodeVirtualFile sourceCodeFile) {
        super(sourceCodeFile.getContent().toString());
        this.sourceCodeFile = sourceCodeFile;
        boolean readonly = EnvironmentManager.getInstance(project).isReadonly(sourceCodeFile);
        setReadOnly(readonly);
    }

    @Override
    public FileType getContentType() {
        return sourceCodeFile.getFileType();
    }
}
