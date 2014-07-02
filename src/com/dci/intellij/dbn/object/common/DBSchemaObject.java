package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public interface DBSchemaObject extends DBObject {
    List<DBObject> getReferencedObjects();
    List<DBObject> getReferencingObjects();
    DBContentType getContentType();
    boolean isEditable(DBContentType contentType);

    Timestamp loadChangeTimestamp(DBContentType contentType) throws SQLException;
    DBObjectTimestampLoader getTimestampLoader(DBContentType contentType);

    String loadCodeFromDatabase(DBContentType contentType) throws SQLException;
    DBLanguage getCodeLanguage(DBContentType contentType);
    String getCodeParseRootId(DBContentType contentType);

    void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException;
    String createDDLStatement(String code);
    DDLFileType getDDLFileType(DBContentType contentType);
    DDLFileType[] getDDLFileTypes();

    DBObjectStatusHolder getStatus();

    @NotNull
    DatabaseEditableObjectFile getVirtualFile();
}
