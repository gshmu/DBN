package com.dci.intellij.dbn.editor.session.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.SessionBrowserState;

public class SessionBrowserModel extends ResultSetDataModel<SessionBrowserModelRow>{

    public SessionBrowserModel(ConnectionHandler connectionHandler, ResultSet resultSet, SessionBrowserState state) throws SQLException {
        super(connectionHandler);
        setHeader(new SessionBrowserModelHeader(connectionHandler, resultSet));
        setState(state);
        load();
    }

    public SessionBrowserModel(ConnectionHandler connectionHandler, SessionBrowserState state) {
        super(connectionHandler);
        setHeader(new SessionBrowserModelHeader());
        setState(state);
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

    @Nullable
    @Override
    public SessionBrowserFilterState getFilter() {
        return (SessionBrowserFilterState) super.getFilter();
    }

    private ResultSet loadResultSet() throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadSessions(connectionHandler.getStandaloneConnection());
        }
        return null;
    }

    @Override
    protected SortableDataModelState createState() {
        return new SessionBrowserState();
    }

    @NotNull
    @Override
    public SessionBrowserState getState() {
        return (SessionBrowserState) super.getState();
    }

    @Override
    public void setState(DataModelState state) {
        super.setState(state);
        if (state instanceof SessionBrowserState) {
            SessionBrowserState sessionBrowserState = (SessionBrowserState) state;
            setFilter(sessionBrowserState.getFilterState());
        }
    }

    @Override
    public SessionBrowserModelHeader getHeader() {
        return (SessionBrowserModelHeader) super.getHeader();
    }

    @Override
    protected SessionBrowserModelRow createRow(int resultSetRowIndex) throws SQLException {
        return new SessionBrowserModelRow(this, resultSet);
    }

    public List<String> getDistinctUsers(String selectedValue) {
        return getDistinctValues("USER", selectedValue);
    }

    public List<String> getDistinctHosts(String selectedValue) {
        return getDistinctValues("HOST", selectedValue);
    }

    public List<String> getDistinctStatuses(String selectedValue) {
        return getDistinctValues("STATUS", selectedValue);
    }

    public List<String> getDistinctValues(String columnName, String selectedValue) {
        ArrayList<String> values = new ArrayList<>();
        List<SessionBrowserModelRow> rows = getRows();
        if (rows instanceof FiltrableList) {
            FiltrableList<SessionBrowserModelRow> filtrableList = (FiltrableList<SessionBrowserModelRow>) rows;
            rows  = filtrableList.getFullList();
        }
        for (SessionBrowserModelRow row : rows) {
            String value = (String) row.getCellValue(columnName);
            if (value != null && !values.contains(value)) {
                values.add(value);
            }
        }
        if (StringUtils.isNotEmpty(selectedValue) && !values.contains(selectedValue)) {
            values.add(selectedValue);
        }
        Collections.sort(values);
        return values;
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
