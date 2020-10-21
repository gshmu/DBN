package com.dci.intellij.dbn.data.model.basic;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.list.FiltrableListImpl;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettingsListener;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.property.DisposablePropertyHolder;
import com.dci.intellij.dbn.common.thread.Dispatch;
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

public class BasicDataModel<
        R extends DataModelRow<? extends BasicDataModel<R, C>, C>,
        C extends DataModelCell<R, ? extends BasicDataModel<R, C>>>
        extends DisposablePropertyHolder<RecordStatus>
        implements DataModel<R,C> {

    private final ProjectRef project;
    private DataModelHeader<? extends ColumnInfo> header;
    private DataModelState state;
    private List<R> rows = new ArrayList<>();
    private Filter<R> filter;
    private boolean environmentReadonly;
    private Latent<Formatter> formatter;

    private final Set<TableModelListener> tableModelListeners = new HashSet<>();
    private final Set<DataModelListener> dataModelListeners = new HashSet<>();
    private final Latent<BasicDataGutterModel> listModel = Latent.basic(() -> new BasicDataGutterModel(BasicDataModel.this));
    private final Latent<DataSearchResult> searchResult = Latent.basic(() -> new DataSearchResult());


    private final RegionalSettingsListener regionalSettingsListener = new RegionalSettingsListener() {
        @Override
        public void settingsChanged() {
            formatter = Latent.thread(() -> {
                Project project = getProject();
                Formatter formatter = Formatter.getInstance(project);
                return formatter.clone();
            });
        }
    };


    public BasicDataModel(Project project) {
        this.project = ProjectRef.of(project);
        formatter = Latent.thread(() -> Formatter.getInstance(project).clone());
        ProjectEvents.subscribe(project, this, RegionalSettingsListener.TOPIC, regionalSettingsListener);
    }

    @Override
    protected RecordStatus[] properties() {
        return RecordStatus.values();
    }

    public boolean isEnvironmentReadonly() {
        return environmentReadonly;
    }

    public void setEnvironmentReadonly(boolean environmentReadonly) {
        this.environmentReadonly = environmentReadonly;
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
        return project.ensure();
    }

    public void setHeader(@NotNull DataModelHeader<? extends ColumnInfo> header) {
        this.header = SafeDisposer.replace(this.header, header, false);
        SafeDisposer.register(this, this.header);
    }

    @Override
    @NotNull
    public DataModelHeader<? extends ColumnInfo> getHeader() {
        return Failsafe.nn(header);
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
    public void setFilter(Filter<R> filter) {
        List<R> rows = getRows();
        if (filter == null) {
            if (rows instanceof FiltrableList) {
                FiltrableList<R> filtrableList = (FiltrableList<R>) rows;
                this.rows = filtrableList.getFullList();
            }
        }
        else {
            FiltrableListImpl<R> filtrableList;
            if (rows instanceof FiltrableList) {
                filtrableList = (FiltrableListImpl<R>) rows;
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
    public Filter<R> getFilter() {
        return filter;
    }

    protected DataModelState createState() {
        return new DataModelState();
    }

    @NotNull
    @Override
    public List<R> getRows() {
        return Failsafe.nn(rows);
    }

    public void setRows(List<R> rows) {
        if (filter != null) {
            this.rows = new FiltrableListImpl<>(rows, filter);
        } else {
            this.rows = rows;
        }

        getState().setRowCount(getRowCount());
    }

    public void addRow(R row) {
        getRows().add(row);
        getState().setRowCount(getRowCount());
    }

    protected void addRowAtIndex(int index, R row) {
        getRows().add(index, row);
        updateRowIndexes(index);
        getState().setRowCount(getRowCount());
    }

    protected void removeRowAtIndex(int index) {
        DataModelRow row = getRows().remove(index);
        updateRowIndexes(index);
        getState().setRowCount(getRowCount());

        SafeDisposer.dispose(row, false);
    }

    @Nullable
    @Override
    public R getRowAtIndex(int index) {
        // model may be reloading when this is called, hence
        // IndexOutOfBoundsException is thrown if the range is not checked
        List<R> rows = getRows();
        return index > -1 && rows.size() > index ? rows.get(index) : null;
    }

    @Nullable
    public C getCellAt(int rowIndex, int columnIndex) {
        R row = getRowAtIndex(rowIndex);
        return row == null ? null : row.getCellAtIndex(columnIndex);
    }

    @Override
    public ColumnInfo getColumnInfo(int columnIndex) {
        return getHeader().getColumnInfo(columnIndex);
    }

    @Override
    public int indexOfRow(R row) {
        return getRows().indexOf(row);
    }

    protected void updateRowIndexes(int startIndex) {
        updateRowIndexes(getRows(), startIndex);
    }

    protected void updateRowIndexes(List<R> rows, int startIndex) {
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
        Dispatch.run(() -> {
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
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return header == null ? 0 : header.getColumnCount();
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
        return getCellAt(rowIndex, columnIndex);
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
        return getHeader().getColumnIndex(columnName);
    }

    /********************************************************
     *                    Disposable                        *
     *******************************************************  */
    @Override
    public void disposeInner() {
        SafeDisposer.dispose(rows, false, false);
        nullify();
    }
}
