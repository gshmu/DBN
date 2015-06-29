package com.dci.intellij.dbn.debugger;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;

public class DBProgramDebugUtil {

    public static @Nullable DBSchemaObject getObject(@Nullable XSourcePosition sourcePosition) {
        if (sourcePosition != null) {
            VirtualFile virtualFile = sourcePosition.getFile();
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
                return databaseFile.getObject();
            }

            if (virtualFile instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
                return sourceCodeFile.getObject();
            }
        }
        return null;
    }

    public static VirtualFile getSourceCodeFile(XSourcePosition sourcePosition) {
        if (sourcePosition != null) {
            VirtualFile virtualFile = sourcePosition.getFile();
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
                return databaseFile.getMainContentFile();
            }

            return virtualFile;
        }
        return null;
    }
}
