package com.dci.intellij.dbn.debugger;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.dci.intellij.dbn.vfs.SourceCodeFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;

public class DBProgramDebugUtil {

    public static @Nullable DBSchemaObject getObject(@Nullable XSourcePosition sourcePosition) {
        if (sourcePosition != null) {
            VirtualFile virtualFile = sourcePosition.getFile();
            if (virtualFile instanceof DatabaseEditableObjectFile) {
                DatabaseEditableObjectFile databaseFile = (DatabaseEditableObjectFile) virtualFile;
                return databaseFile.getObject();
            }

            if (virtualFile instanceof SourceCodeFile) {
                SourceCodeFile sourceCodeFile = (SourceCodeFile) virtualFile;
                return sourceCodeFile.getDatabaseFile().getObject();
            }
        }
        return null;
    }

    public static SourceCodeFile getSourceCodeFile(XSourcePosition sourcePosition) {
        if (sourcePosition != null) {
            VirtualFile virtualFile = sourcePosition.getFile();
            if (virtualFile instanceof DatabaseEditableObjectFile) {
                DatabaseEditableObjectFile databaseFile = (DatabaseEditableObjectFile) virtualFile;
                return (SourceCodeFile) databaseFile.getMainContentFile();
            }

            if (virtualFile instanceof SourceCodeFile) {
                return (SourceCodeFile) virtualFile;
            }
        }
        return null;
    }
}
