package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.SourceCodeOffsets;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DatabaseDDLInterfaceImpl extends DatabaseInterfaceImpl implements DatabaseDDLInterface {
    public DatabaseDDLInterfaceImpl(String fileName, DatabaseInterfaceProvider provider) {
        super(fileName, provider);
    }

    public boolean includesTypeAndNameInSourceContent(DatabaseObjectTypeId objectTypeId) {
        return
                objectTypeId == DatabaseObjectTypeId.FUNCTION ||
                objectTypeId == DatabaseObjectTypeId.PROCEDURE ||
                objectTypeId == DatabaseObjectTypeId.PACKAGE ||
                objectTypeId == DatabaseObjectTypeId.TRIGGER ||
                objectTypeId == DatabaseObjectTypeId.TYPE;

    }

    protected final void execute(String statementText, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(20);
        statement.execute(statementText);

    }

    protected final String getSingleValue(Connection connection, String loaderId, Object... arguments) throws SQLException {
        ResultSet resultSet = executeQuery(connection, loaderId, arguments);
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return null;
    }

    /*********************************************************
     *                   CREATE statements                   *
     *********************************************************/
    public void createView(String viewName, String code, Connection connection) throws SQLException {
        executeUpdate(connection, "create-view", viewName, code);
    }

    public void createObject(String code, Connection connection) throws SQLException {
        executeUpdate(connection, "create-object", code);
    }


   /*********************************************************
    *                   DROP statements                     *
    *********************************************************/
   public void dropObject(String objectType, String objectName, Connection connection) throws SQLException {
       executeUpdate(connection, "drop-object", objectType, objectName);
   }

    @Override
    public void computeSourceCodeOffsets(SourceCodeContent content, DatabaseObjectTypeId objectTypeId, String objectName) {
        String sourceCode = content.getSourceCode();
        int gbEndOffset = sourceCode.indexOf(SourceCodeOffsets.GUARDED_BLOCK_END_OFFSET_MARKER);
        if (gbEndOffset > -1) {
            content.getOffsets().setGuardedBlockEndOffset(gbEndOffset);
            sourceCode =
                    sourceCode.substring(0, gbEndOffset) +
                    sourceCode.substring(gbEndOffset + SourceCodeOffsets.GUARDED_BLOCK_END_OFFSET_MARKER.length());
            content.setSourceCode(sourceCode);
        }
    }
}
