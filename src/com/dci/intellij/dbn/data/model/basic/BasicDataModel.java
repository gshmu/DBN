package com.dci.intellij.dbn.data.model.basic;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.list.FiltrableListImpl;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettingsListener;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.DisposableLazyValue;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.data.find.DataSearchResult;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModel;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.dci.intellij.dbn.data.model.DataModelListener;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.DataModelState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public class BasicDataModel<T extends DataModelRow> implements DataModel<T> {
    private DataModelHeader header;
    private DataModelState state;
    private Set<TableModelListener> tableModelListeners = new HashSet<TableModelListener>();
    private Set<DataModelListener> dataModelListeners = new HashSet<DataModelListener>();
    private List<T> rows = new ArrayList<T>();
    private Project project;
    private Filter<T> filter;
    private Formatter formatter;

    private RegionalSettingsListener regionalSettingsListener = new RegionalSettingsListener() {
        @Override
        public void settingsChanged() {
            BasicDataModel.this.formatter = Formatter.getInstance(project).clone();
        }
    };

    private LazyValue<BasicDataGutterModel> listModel = new DisposableLazyValue<BasicDataGutterModel>(this) {
        @Override
        protected BasicDataGutterModel load() {
            return new BasicDataGutterModel(BasicDataModel.this);
        }
    };

    private LazyValue<DataSearchResult> searchResult = new DisposableLazyValue<DataSearchResult>(this) {
        @Override
        protected DataSearchResult load() {
            DataSearchResult dataSearchResult = new DataSearchResult();
            Disposer.register(this, dataSearchResult);
            return dataSearchResult;
        }
    };

    public BasicDataModel(Project project) {
        this.project = project;
        this.formatter = Formatter.getInstance(project).clone();
        EventUtil.subscribe(project, this, RegionalSettingsListener.TOPIC, regionalSettingsListener);
    }

    @Override
    public ListModel getListModel() {
        return listModel.get();
    }

    @NotNull
    public Formatter getFormatter() {
        return formatter;
    }

    @Override
    public boolean isReadonly() {
        return true;
    }

    @NotNull
    public Project getProject() {
        return FailsafeUtil.get(project);
    }

    public void setHeader(@NotNull DataModelHeader header) {
        this.header = header;
        Disposer.register(this, header);
    }

    public DataModelHeader getHeader() {
        return header;
    }

    @NotNull
    public DataModelState getState() {
        if (state == null) {
            state = createState();
        }
        return state;
    }

    public void setState(DataModelState state) {
        this.state = state;
    }

    @Override
    public void setFilter(Filter<T> filter) {
        List<T> rows = getRows();
        if (filter == null) {
            if (rows instanceof FiltrableList) {
                FiltrableList<T> filtrableList = (FiltrableList<T>) rows;
                this.rows = filtrableList.getFullList();
            }
        }
        else {
            FiltrableListImpl<T> filtrableList;
            if (rows instanceof FiltrableList) {
                filtrableList = (FiltrableListImpl<T>) rows;
            } else {
                filtrableList = new FiltrableListImpl<T>(rows);
                this.rows = filtrableList;
            }
            filtrableList.setFilter(filter);
        }
        this.filter = filter;
    }

    @Nullable
    @Override
    public Filter<T> getFilter() {
        return filter;
    }

    protected DataModelState createState() {
        return new DataModelState();
    }

    @NotNull
    @Override
    public List<T> getRows() {
        return FailsafeUtil.get(rows);
    }

    public void setRows(List<T> rows) {
        if (filter != null) {
            this.rows = new FiltrableListImpl<T>(rows, filter);
        } else {
            this.rows = rows;
        }

        getState().setRowCount(getRowCount());
    }

    public void addRow(T row) {
        getRows().add(row);
        getState().setRowCount(getRowCount());
    }

    public void addRowAtIndex(int index, T row) {
        getRows().add(index, row);
        updateRowIndexes(index);
        getState().setRowCount(getRowCount());
    }

    public void removeRowAtIndex(int index) {
        DataModelRow row = getRows().remove(index);
        updateRowIndexes(index);
        getState().setRowCount(getRowCount());

        Disposer.dispose(row);
    }

    public T getRowAtIndex(int index) {
        // model may be reloading when this is called, hence
        // IndexOutOfBoundsException is thrown if the range is not checked
        List<T> rows = getRows();
        return index > -1 && rows.size() > index ? rows.get(index) : null;
    }

    public DataModelCell getCellAt(int rowIndex, int columnIndex) {
        return getRows().get(rowIndex).getCellAtIndex(columnIndex);
    }

    public ColumnInfo getColumnInfo(int columnIndex) {
        return getHeader().getColumnInfo(columnIndex);
    }

    public int indexOfRow(T row) {
        return getRows().indexOf(row);
    }

    protected void updateRowIndexes(int startIndex) {
        updateRowIndexes(getRows(), startIndex);
    }

    protected void updateRowIndexes(List<T> rows, int startIndex) {
        for (int i=startIndex; i<rows.size();i++) {
            rows.get(i).setIndex(i);
        }
    }

    /*********************************************************
     *                 Listener notifiers                    *
     *********************************************************/
    public void notifyCellUpdated(int rowIndex, int columnIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, rowIndex, rowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    public void notifyRowUpdated(int rowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, rowIndex, rowIndex);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, rowIndex, rowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    public void notifyRowsDeleted(int fromRowIndex, int toRowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, fromRowIndex, toRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, fromRowIndex, toRowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    public void notifyRowsUpdated(int fromRowIndex, int toRowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, fromRowIndex, toRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, fromRowIndex, toRowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    public void notifyRowsInserted(int fromRowIndex, int toRowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, fromRowIndex, toRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, fromRowIndex, toRowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    private void notifyListeners(final ListDataEvent listDataEvent, final TableModelEvent event) {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                if (listModel.isLoaded()) {
                    listModel.get().notifyListeners(listDataEvent);
                }

                for (TableModelListener tableModelListener: tableModelListeners) {
                    tableModelListener.tableChanged(event);
                }

                for (DataModelListener tableModelListener: dataModelListeners) {
                    tableModelListener.modelChanged();
                }
            }
        }.start();
    }

    /*********************************************************
     *                     DataModel                        *
     *********************************************************/
    public int getRowCount() {
        return getRows().size();
    }

    public int getColumnCount() {
        return disposed ? 0 : getHeader().getColumnCount();
    }

    public String getColumnName(int columnIndex) {
        return getHeader().getColumnName(columnIndex);
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        // model may be reloading when this is called, hence
        // IndexOutOfBoundsException is thrown if the range is not checked
        List<T> rows = getRows();
        return rows.size() > rowIndex && columnIndex > -1 ? rows.get(rowIndex).getCellAtIndex(columnIndex) : null;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {}

    public void addTableModelListener(TableModelListener listener) {
        tableModelListeners.add(listener);
    }

    public void removeTableModelListener(TableModelListener listener) {
        tableModelListeners.remove(listener);
    }

    public void addDataModelListener(DataModelListener listener) {
        dataModelListeners.add(listener);
    }

    @Override
    public void removeDataModelListener(DataModelListener listener) {
        dataModelListeners.remove(listener);
    }

    @Override
    public DataSearchResult getSearchResult() {
        return searchResult.get();
    }
    
    @Override
    public boolean hasSearchResult() {
        return searchResult.isLoaded() && !searchResult.get().isEmpty();
    }

    @Override
    public int getColumnIndex(String columnName) {
        return header.getColumnIndex(columnName);
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            DisposerUtil.dispose(rows);
            tableModelListeners.clear();
            dataModelListeners.clear();
            searchResult = null;
            header = null;
            rows = null;
            project = null;
        }
    }
}
