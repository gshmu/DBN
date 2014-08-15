package com.dci.intellij.dbn.debugger;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.SourceCodeVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;

public class DBProgramDebugUtil {

    public static @Nullable DBSchemaObject getObject(@Nullable XSourcePosition sourcePosition) {
        if (sourcePosition != null) {
            VirtualFile virtualFile = sourcePosition.getFile();
            if (virtualFile instanceof DatabaseEditableObjectVirtualFile) {
                DatabaseEditableObjectVirtualFile databaseFile = (DatabaseEditableObjectVirtualFile) virtualFile;
                return databaseFile.getObject();
            }

            if (virtualFile instanceof SourceCodeVirtualFile) {
                SourceCodeVirtualFile sourceCodeFile = (SourceCodeVirtualFile) virtualFile;
                return sourceCodeFile.getDatabaseFile().getObject();
            }
        }
        return null;
    }

    public static SourceCodeVirtualFile getSourceCodeFile(XSourcePosition sourcePosition) {
        if (sourcePosition != null) {
            VirtualFile virtualFile = sourcePosition.getFile();
            if (virtualFile instanceof DatabaseEditableObjectVirtualFile) {
                DatabaseEditableObjectVirtualFile databaseFile = (DatabaseEditableObjectVirtualFile) virtualFile;
                return (SourceCodeVirtualFile) databaseFile.getMainContentFile();
            }

            if (virtualFile instanceof SourceCodeVirtualFile) {
                return (SourceCodeVirtualFile) virtualFile;
            }
        }
        return null;
    }
}
