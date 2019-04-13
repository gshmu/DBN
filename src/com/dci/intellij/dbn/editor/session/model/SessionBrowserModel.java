package com.dci.intellij.dbn.editor.session.model;

import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterType;
import com.dci.intellij.dbn.editor.session.SessionBrowserState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionBrowserModel extends ResultSetDataModel<SessionBrowserModelRow, SessionBrowserModelCell>{
    private long timestamp = System.currentTimeMillis();
    private String loadError;

    public SessionBrowserModel(ConnectionHandler connectionHandler) {
        super(connectionHandler);
        setHeader(new SessionBrowserModelHeader());
    }

    public SessionBrowserModel(ConnectionHandler connectionHandler, DBNResultSet resultSet) throws SQLException {
        super(connectionHandler);
        setHeader(new SessionBrowserModelHeader(connectionHandler, resultSet));
        checkDisposed();
        setResultSet(resultSet);
        setResultSetExhausted(false);
        fetchNextRecords(10000, true);
    }

    public String getLoadError() {
        return loadError;
    }

    public void setLoadError(String loadError) {
        this.loadError = loadError;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Nullable
    @Override
    public SessionBrowserFilterState getFilter() {
        return (SessionBrowserFilterState) super.getFilter();
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
        sort();
    }

    @NotNull
    @Override
    public SessionBrowserModelHeader getHeader() {
        return (SessionBrowserModelHeader) super.getHeader();
    }

    @Override
    protected SessionBrowserModelRow createRow(int resultSetRowIndex) throws SQLException {
        return new SessionBrowserModelRow(this, getResultSet(), resultSetRowIndex);
    }

    public List<String> getDistinctValues(SessionBrowserFilterType filterType, String selectedValue) {
        switch (filterType) {
            case USER: return getDistinctValues("USER", selectedValue);
            case HOST: return getDistinctValues("HOST", selectedValue);
            case STATUS: return getDistinctValues("STATUS", selectedValue);
        }
        return null;
    }

    private List<String> getDistinctValues(String columnName, String selectedValue) {
        ArrayList<String> values = new ArrayList<String>();
        List<SessionBrowserModelRow> rows = getRows();
        if (rows instanceof FiltrableList) {
            FiltrableList<SessionBrowserModelRow> filtrableList = (FiltrableList<SessionBrowserModelRow>) rows;
            rows  = filtrableList.getFullList();
        }
        for (SessionBrowserModelRow row : rows) {
            String value = (String) row.getCellValue(columnName);
            if (StringUtil.isNotEmpty(value) && !values.contains(value)) {
                values.add(value);
            }
        }
        if (StringUtil.isNotEmpty(selectedValue) && !values.contains(selectedValue)) {
            values.add(selectedValue);
        }
        Collections.sort(values);
        return values;
    }


    /*********************************************************
     *                      DataModel                       *
     *********************************************************/
    @Override
    public SessionBrowserModelCell getCellAt(int rowIndex, int columnIndex) {
        return (SessionBrowserModelCell) super.getCellAt(rowIndex, columnIndex);
    }
}
