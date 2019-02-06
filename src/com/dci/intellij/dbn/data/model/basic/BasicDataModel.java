package com.dci.intellij.dbn.data.model.basic;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.latent.DisposableLatent;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.ThreadLocalLatent;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.list.FiltrableListImpl;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettingsListener;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.data.find.DataSearchResult;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModel;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.dci.intellij.dbn.data.model.DataModelListener;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.editor.data.model.RecordStatus;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicDataModel<T extends DataModelRow> extends PropertyHolderImpl<RecordStatus> implements DataModel<T> {
    private DataModelHeader<? extends ColumnInfo> header;
    private DataModelState state;
    private Set<TableModelListener> tableModelListeners = new HashSet<>();
    private Set<DataModelListener> dataModelListeners = new HashSet<>();
    private List<T> rows = new ArrayList<>();
    private Project project;
    private Filter<T> filter;
    private ThreadLocalLatent<Formatter> formatter;
    private boolean isEnvironmentReadonly;

    private RegionalSettingsListener regionalSettingsListener = new RegionalSettingsListener() {
        @Override
        public void settingsChanged() {
            formatter = ThreadLocalLatent.create(() -> Formatter.getInstance(project).clone());
        }
    };

    private Latent<BasicDataGutterModel> listModel = DisposableLatent.create(this, () -> new BasicDataGutterModel(BasicDataModel.this));
    private Latent<DataSearchResult> searchResult = DisposableLatent.create(this, DataSearchResult::new);

    public BasicDataModel(Project project) {
        this.project = project;
        formatter = ThreadLocalLatent.create(() -> Formatter.getInstance(project).clone());
        EventUtil.subscribe(project, this, RegionalSettingsListener.TOPIC, regionalSettingsListener);
    }

    @Override
    protected RecordStatus[] properties() {
        return RecordStatus.values();
    }

    public boolean isEnvironmentReadonly() {
        return isEnvironmentReadonly;
    }

    public void setEnvironmentReadonly(boolean environmentReadonly) {
        isEnvironmentReadonly = environmentReadonly;
    }

    @Override
    public ListModel getListModel() {
        return listModel.get();
    }

    @NotNull
    public Formatter getFormatter() {
        return formatter.get();
    }

    @Override
    public boolean isReadonly() {
        return true;
    }

    @Override
    @NotNull
    public Project getProject() {
        return FailsafeUtil.get(project);
    }

    public void setHeader(@NotNull DataModelHeader<? extends ColumnInfo> header) {
        DataModelHeader oldHeader = this.header;
        this.header = header;
        Disposer.register(this, header);
        DisposerUtil.dispose(oldHeader);
    }

    @Override
    public DataModelHeader<? extends ColumnInfo> getHeader() {
        return header;
    }

    @Override
    @NotNull
    public DataModelState getState() {
        if (state == null) {
            state = createState();
        }
        return state;
    }

    @Override
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
                filtrableList = new FiltrableListImpl<>(rows);
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
        FailsafeUtil.ensure(this);
        return FailsafeUtil.get(rows);
    }

    public void setRows(List<T> rows) {
        if (filter != null) {
            this.rows = new FiltrableListImpl<>(rows, filter);
        } else {
            this.rows = rows;
        }

        getState().setRowCount(getRowCount());
    }

    public void addRow(T row) {
        getRows().add(row);
        getState().setRowCount(getRowCount());
    }

    protected void addRowAtIndex(int index, T row) {
        getRows().add(index, row);
        updateRowIndexes(index);
        getState().setRowCount(getRowCount());
    }

    protected void removeRowAtIndex(int index) {
        DataModelRow row = getRows().remove(index);
        updateRowIndexes(index);
        getState().setRowCount(getRowCount());

        Disposer.dispose(row);
    }

    @Override
    public T getRowAtIndex(int index) {
        // model may be reloading when this is called, hence
        // IndexOutOfBoundsException is thrown if the range is not checked
        List<T> rows = getRows();
        return index > -1 && rows.size() > index ? rows.get(index) : null;
    }

    public DataModelCell getCellAt(int rowIndex, int columnIndex) {
        return getRows().get(rowIndex).getCellAtIndex(columnIndex);
    }

    @Override
    public ColumnInfo getColumnInfo(int columnIndex) {
        return getHeader().getColumnInfo(columnIndex);
    }

    @Override
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

    protected void notifyRowUpdated(int rowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, rowIndex, rowIndex);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, rowIndex, rowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    protected void notifyRowsDeleted(int fromRowIndex, int toRowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, fromRowIndex, toRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, fromRowIndex, toRowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    protected void notifyRowsUpdated(int fromRowIndex, int toRowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, fromRowIndex, toRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, fromRowIndex, toRowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    protected void notifyRowsInserted(int fromRowIndex, int toRowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, fromRowIndex, toRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, fromRowIndex, toRowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    protected void notifyListeners(@Nullable ListDataEvent listDataEvent, @Nullable TableModelEvent modelEvent) {
        SimpleLaterInvocator.invoke(() -> {
            if (listDataEvent != null) {
                if (listModel.loaded()) {
                    listModel.get().notifyListeners(listDataEvent);
                }
            }

            if (modelEvent != null) {
                for (TableModelListener tableModelListener: tableModelListeners) {
                    tableModelListener.tableChanged(modelEvent);
                }

                for (DataModelListener tableModelListener: dataModelListeners) {
                    tableModelListener.modelChanged();
                }
            }
        });
    }

    /*********************************************************
     *                     DataModel                        *
     *********************************************************/
    @Override
    public int getRowCount() {
        return getRows().size();
    }

    @Override
    public int getColumnCount() {
        return isDisposed() ? 0 : getHeader().getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return getHeader().getColumnName(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // model may be reloading when this is called, hence
        // IndexOutOfBoundsException is thrown if the range is not checked
        List<T> rows = getRows();
        return rows.size() > rowIndex && columnIndex > -1 ? rows.get(rowIndex).getCellAtIndex(columnIndex) : null;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {}

    @Override
    public void addTableModelListener(TableModelListener listener) {
        tableModelListeners.add(listener);
    }

    @Override
    public void removeTableModelListener(TableModelListener listener) {
        tableModelListeners.remove(listener);
    }

    @Override
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
        return searchResult.loaded() && !searchResult.get().isEmpty();
    }

    @Override
    public int getColumnIndex(String columnName) {
        return header.getColumnIndex(columnName);
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    @Override
    public void dispose() {
        if (!isDisposed()) {
            set(RecordStatus.DISPOSED, true);
            DisposerUtil.dispose(rows);
            tableModelListeners.clear();
            dataModelListeners.clear();
            searchResult = null;
            header = null;
            rows = null;
            project = null;
        }
    }

    @Override
    public boolean isDisposed() {
        return is(RecordStatus.DISPOSED);
    }
}
