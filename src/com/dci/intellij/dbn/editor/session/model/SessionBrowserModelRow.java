package com.dci.intellij.dbn.editor.session.model;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelRow;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.session.SessionStatus;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionBrowserModelRow
        extends ResultSetDataModelRow<SessionBrowserModel, SessionBrowserModelCell> {

    public SessionBrowserModelRow(SessionBrowserModel model, ResultSet resultSet, int resultSetRowIndex) throws SQLException {
        super(model, resultSet, resultSetRowIndex);
    }

    @NotNull
    @Override
    protected SessionBrowserModelCell createCell(ResultSet resultSet, ColumnInfo columnInfo) throws SQLException {
        return new SessionBrowserModelCell(this, resultSet, (ResultSetColumnInfo) columnInfo);
    }

    @NotNull
    @Override
    public SessionBrowserModel getModel() {
        return super.getModel();
    }

    public String getUser() {
        return (String) getCellValue("USER");
    }

    public String getHost() {
        return (String) getCellValue("HOST");
    }

    public String getStatus() {
        return (String) getCellValue("STATUS");
    }

    public Object getSessionId() {
        return getCellValue("SESSION_ID");
    }

    public Object getSerialNumber() {
        return getCellValue("SERIAL_NUMBER");
    }

    public String getSchema() {
        return (String) getCellValue("SCHEMA");
    }

    public SessionStatus getSessionStatus() {
        ConnectionHandler connection = getModel().getConnectionHandler();
        DatabaseCompatibilityInterface compatibilityInterface = connection.getInterfaceProvider().getCompatibilityInterface();
        return compatibilityInterface.getSessionStatus(getStatus());
    }

}
