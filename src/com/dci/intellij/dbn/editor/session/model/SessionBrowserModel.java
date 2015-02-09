package com.dci.intellij.dbn.editor.session.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;

public class SessionBrowserModel extends ResultSetDataModel<SessionBrowserModelRow>{
    private SessionBrowser sessionBrowser;

    public SessionBrowserModel(SessionBrowser sessionBrowser, ResultSet resultSet) throws SQLException {
        super(sessionBrowser.getConnectionHandler());
        this.sessionBrowser = sessionBrowser;
        setHeader(new SessionBrowserModelHeader(sessionBrowser, resultSet));
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
        }
    }

    private ResultSet loadResultSet() throws SQLException {
        return sessionBrowser.getDatabaseFile().read();
    }

    @Override
    public SessionBrowserModelHeader getHeader() {
        return (SessionBrowserModelHeader) super.getHeader();
    }

    @Override
    protected SessionBrowserModelRow createRow(int resultSetRowIndex) throws SQLException {
        return new SessionBrowserModelRow(this, resultSet, resultSetRowIndex);
    }

    @Nullable
    public SessionBrowserTable getEditorTable() {
        return sessionBrowser == null ? null : sessionBrowser.getEditorTable();
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
            sessionBrowser = null;
        }
    }
}
