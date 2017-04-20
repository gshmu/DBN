package com.dci.intellij.dbn.object.common;

import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.ChangeTimestamp;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBObjectVirtualFile;

public interface DBSchemaObject extends DBObject {
    List<DBObject> getReferencedObjects();
    List<DBObject> getReferencingObjects();
    DBContentType getContentType();
    boolean isEditable(DBContentType contentType);

    @NotNull
    ChangeTimestamp loadChangeTimestamp(DBContentType contentType) throws SQLException;

    String loadCodeFromDatabase(DBContentType contentType) throws SQLException;
    DBLanguage getCodeLanguage(DBContentType contentType);
    String getCodeParseRootId(DBContentType contentType);

    void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException;
    DDLFileType getDDLFileType(DBContentType contentType);
    DDLFileType[] getDDLFileTypes();

    DBObjectStatusHolder getStatus();

    @NotNull
    DBObjectVirtualFile getVirtualFile();

    DBEditableObjectVirtualFile getEditableVirtualFile();

    @Nullable
    DBEditableObjectVirtualFile getCachedVirtualFile();

    List<DBSchema> getReferencingSchemas() throws SQLException;
}
