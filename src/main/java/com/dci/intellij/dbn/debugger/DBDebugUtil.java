package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.editor.DatabaseFileEditorManager;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import org.jetbrains.annotations.Nullable;

public class DBDebugUtil {

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
            return sourcePosition.getFile();
        }
        return null;
    }


    @Nullable
    public static DBEditableObjectVirtualFile getMainDatabaseFile(DBMethod method) {
        DBSchemaObject schemaObject = getMainDatabaseObject(method);
        return schemaObject == null ? null : (DBEditableObjectVirtualFile) schemaObject.getVirtualFile();
    }

    @Nullable
    public static DBSchemaObject getMainDatabaseObject(DBMethod method) {
        return method != null && method.isProgramMethod() ? method.getProgram() : method;
    }

    public static void openEditor(VirtualFile virtualFile) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
            Project project = Failsafe.nn(databaseFile.getProject());
            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
            sourceCodeManager.ensureSourcesLoaded(databaseFile.getObject(), false);

            DatabaseFileEditorManager editorManager = DatabaseFileEditorManager.getInstance(project);
            editorManager.connectAndOpenEditor(databaseFile.getObject(), null, false, false);
        } else if (virtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
            DBEditableObjectVirtualFile mainDatabaseFile = sourceCodeFile.getMainDatabaseFile();
            openEditor(mainDatabaseFile);
        }
    }
}
