package com.dci.intellij.dbn.editor.session.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;

public class SessionBrowserModel extends ResultSetDataModel<SessionBrowserModelRow>{

    public SessionBrowserModel(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler);
        setHeader(new SessionBrowserModelHeader(connectionHandler, resultSet));
        load();
    }

    public void load() throws SQLException {
        ResultSet newResultSet;
        synchronized (DISPOSE_LOCK) {
            checkDisposed();

            ConnectionUtil.closeResultSet(resultSet);
            newResultSet = loadResultSet();
        }

        if (newResultSet != null) {
            synchronized (DISPOSE_LOCK) {
                checkDisposed();

                resultSet = newResultSet;
                resultSetExhausted = false;
            }
            fetchNextRecords(10000, true);
            ConnectionUtil.closeResultSet(resultSet);
            resultSet = null;
        }
    }

    private ResultSet loadResultSet() throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadUserSessions(connectionHandler.getStandaloneConnection());
        }
        return null;
    }

    @Override
    public SessionBrowserModelHeader getHeader() {
        return (SessionBrowserModelHeader) super.getHeader();
    }

    @Override
    protected SessionBrowserModelRow createRow(int resultSetRowIndex) throws SQLException {
        return new SessionBrowserModelRow(this, resultSet);
    }

    /*********************************************************
     *                      DataModel                       *
     *********************************************************/
    public SessionBrowserModelCell getCellAt(int rowIndex, int columnIndex) {
        return (SessionBrowserModelCell) super.getCellAt(rowIndex, columnIndex);
    }

    /*********************************************************
     *                       Disposable                      *
     *********************************************************/
    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
        }
    }
}
