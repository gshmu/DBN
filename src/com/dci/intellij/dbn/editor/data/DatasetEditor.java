package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.dispose.DisposableUserDataHolderBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.dci.intellij.dbn.data.grid.options.DataGridSettingsChangeListener;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterType;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModel;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.editor.data.record.ui.DatasetRecordEditorDialog;
import com.dci.intellij.dbn.editor.data.state.DatasetEditorState;
import com.dci.intellij.dbn.editor.data.state.column.DatasetColumnSetup;
import com.dci.intellij.dbn.editor.data.state.column.DatasetColumnState;
import com.dci.intellij.dbn.editor.data.structure.DatasetEditorStructureViewModel;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorForm;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.editor.data.DatasetEditorStatus.*;
import static com.dci.intellij.dbn.editor.data.DatasetLoadInstruction.*;
import static com.dci.intellij.dbn.editor.data.model.RecordStatus.INSERTING;
import static com.dci.intellij.dbn.editor.data.model.RecordStatus.MODIFIED;

@Slf4j
public class DatasetEditor extends DisposableUserDataHolderBase implements
        FileEditor,
        FileConnectionMappingProvider,
        ConnectionProvider,
        DataProvider,
        StatefulDisposable {

    private static final DatasetLoadInstructions COL_VISIBILITY_STATUS_CHANGE_LOAD_INSTRUCTIONS = new DatasetLoadInstructions(USE_CURRENT_FILTER, PRESERVE_CHANGES, DELIBERATE_ACTION, REBUILD);
    private static final DatasetLoadInstructions CON_STATUS_CHANGE_LOAD_INSTRUCTIONS = new DatasetLoadInstructions(USE_CURRENT_FILTER);

    private final ProjectRef project;
    private final DBObjectRef<DBDataset> dataset;
    private final DBEditableObjectVirtualFile databaseFile;
    private final DatasetEditorForm editorForm;
    private final DatasetEditorStatusHolder status;
    private final ConnectionHandlerRef connectionHandler;
    private final DataEditorSettings settings;
    private StructureViewModel structureViewModel;
    private String dataLoadError;

    private DatasetEditorState editorState = new DatasetEditorState();

    public DatasetEditor(DBEditableObjectVirtualFile databaseFile, DBDataset dataset) {
        Project project = dataset.getProject();
        this.project = ProjectRef.of(project);
        this.databaseFile = databaseFile;
        this.dataset = DBObjectRef.of(dataset);
        this.settings = DataEditorSettings.getInstance(project);

        connectionHandler = ConnectionHandlerRef.of(dataset.getConnectionHandler());
        status = new DatasetEditorStatusHolder();
        status.set(CONNECTED, true);
        editorForm = new DatasetEditorForm(this);

/*
        if (!EditorUtil.hasEditingHistory(databaseFile, project)) {
            load(true, true, false);
        }
*/
        ProjectEvents.subscribe(project, this, TransactionListener.TOPIC, transactionListener);
        ProjectEvents.subscribe(project, this, ConnectionStatusListener.TOPIC, connectionStatusListener);
        ProjectEvents.subscribe(project, this, DataGridSettingsChangeListener.TOPIC, dataGridSettingsChangeListener);
    }

    @NotNull
    public DBDataset getDataset() {
        return Failsafe.nn(dataset.get(getProject()));
    }

    public DataEditorSettings getSettings() {
        return settings;
    }

    @NotNull
    public DatasetEditorTable getEditorTable() {
        return getEditorForm().getEditorTable();
    }

    @NotNull
    public DatasetEditorForm getEditorForm() {
        return Failsafe.nn(editorForm);
    }

    public void showSearchHeader() {
        getEditorForm().showSearchHeader();
    }

    @NotNull
    public DatasetEditorModel getTableModel() {
        return getEditorTable().getModel();
    }



    public DBEditableObjectVirtualFile getDatabaseFile() {
        return databaseFile;
    }

    @Override
    @Nullable
    public SchemaId getSchemaId() {
        return getDataset().getSchemaIdentifier();
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    @Override
    @NotNull
    public JComponent getComponent() {
        return getEditorForm().getComponent();
    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return editorForm.getComponent();
    }

    @Override
    @NonNls
    @NotNull
    public String getName() {
        return "Data";
    }

    @Override
    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return editorState.clone();
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {
        if (fileEditorState instanceof DatasetEditorState) {
            editorState = (DatasetEditorState) fileEditorState;
        }
    }

    public DatasetEditorState getEditorState() {
        return editorState;
    }

    @Override
    public boolean isModified() {
        return getTableModel().is(MODIFIED);
    }

    @Override
    public boolean isValid() {
        return !isDisposed();
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    @Nullable
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Override
    @Nullable
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return new TreeBasedStructureViewBuilder() {
            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return createStructureViewModel();
            }

            @NotNull
            StructureViewModel createStructureViewModel() {
                // Structure does not change. so it can be cached.
                if (structureViewModel == null) {
                    structureViewModel = new DatasetEditorStructureViewModel(DatasetEditor.this);
                }
                return structureViewModel;
            }
        };
    }

    public static DatasetEditor getSelected(Project project) {
        if (project != null) {
            FileEditor[] fileEditors = FileEditorManager.getInstance(project).getSelectedEditors();
            for (FileEditor fileEditor : fileEditors) {
                if (fileEditor instanceof DatasetEditor) {
                    return (DatasetEditor) fileEditor;
                }
            }
        }
        return null;
    }

    /*******************************************************
     *                   Model operations                  *
     *******************************************************/
    public void fetchNextRecords(int records) {
        try {
            DatasetEditorModel model = getTableModel();
            model.fetchNextRecords(records, false);
            dataLoadError = null;
        } catch (SQLException e) {
            dataLoadError = e.getMessage();
/*
            String message = "Error loading data for " + getDataset().getQualifiedNameWithType() + ".\nCause: " + e.getMessage();
            MessageUtil.showErrorDialog(message, e);
*/
        } finally {
            Project project = getProject();
            ProjectEvents.notify(project,
                    DatasetLoadListener.TOPIC,
                    (listener) -> listener.datasetLoaded(databaseFile));
        }
    }

    public void loadData(final DatasetLoadInstructions instructions) {
        if (status.isNot(LOADING)) {
            ConnectionAction.invoke("loading table data", false, this,
                    (action) -> {
                        setLoading(true);
                        Project project = getProject();
                        ProjectEvents.notify(project,
                                DatasetLoadListener.TOPIC,
                                (listener) -> listener.datasetLoading(databaseFile));

                        Background.run(() -> {
                            DatasetEditorForm editorForm = getEditorForm();
                            try {
                                editorForm.showLoadingHint();
                                editorForm.getEditorTable().cancelEditing();
                                DatasetEditorTable oldEditorTable = instructions.isRebuild() ? editorForm.beforeRebuild() : null;
                                try {
                                    DatasetEditorModel tableModel = getTableModel();
                                    tableModel.load(instructions.isUseCurrentFilter(), instructions.isPreserveChanges());
                                    DatasetEditorTable editorTable = getEditorTable();
                                    editorTable.clearSelection();
                                } finally {
                                    if (!isDisposed()) {
                                        editorForm.afterRebuild(oldEditorTable);
                                    }
                                }
                                dataLoadError = null;
                            } catch (ProcessCanceledException ignore) {

                            } catch (SQLException e) {
                                dataLoadError = e.getMessage();
                                handleLoadError(e, instructions);
                            } catch (Exception e) {
                                log.error("Error loading table data", e);
                            } finally {
                                status.set(LOADED, true);
                                editorForm.hideLoadingHint();
                                setLoading(false);
                                ProjectEvents.notify(project,
                                        DatasetLoadListener.TOPIC,
                                        (listener) -> listener.datasetLoaded(databaseFile));
                            }
                        });
                    });
        }

    }

    private void handleLoadError(SQLException e, DatasetLoadInstructions instr) {
        Dispatch.run(() -> {
            checkDisposed();
            focusEditor();
            ConnectionHandler connectionHandler = getConnectionHandler();
            DatabaseMessageParserInterface messageParserInterface = connectionHandler.getInterfaceProvider().getMessageParserInterface();
            Project project = getProject();
            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);

            DBDataset dataset = getDataset();
            DatasetFilter filter = filterManager.getActiveFilter(dataset);
            String datasetName = dataset.getQualifiedNameWithType();
            if (connectionHandler.isValid()) {
                if (filter == null || filter == DatasetFilterManager.EMPTY_FILTER || filter.getError() != null) {
                    if (instr.isDeliberateAction()) {
                        String message =
                                "Error loading data for " + datasetName + ".\n" + (
                                        messageParserInterface.isTimeoutException(e) ?
                                                "The operation was timed out. Please check your timeout configuration in Data Editor settings." :
                                                "Database error message: " + e.getMessage());

                        Messages.showErrorDialog(project, message);
                    }
                } else {
                    String message =
                            "Error loading data for " + datasetName + ".\n" + (
                                    messageParserInterface.isTimeoutException(e) ?
                                            "The operation was timed out. Please check your timeout configuration in Data Editor settings." :
                                            "Filter \"" + filter.getName() + "\" may be invalid.\n" +
                                                    "Database error message: " + e.getMessage());
                    String[] options = {"Retry", "Edit filter", "Remove filter", "Ignore filter", "Cancel"};

                    Messages.showErrorDialog(project, "Error", message, options, 0,
                            (option) -> {
                                DatasetLoadInstructions instructions = DatasetLoadInstructions.clone(instr);
                                instructions.setDeliberateAction(true);

                                if (option == 0) {
                                    loadData(instructions);
                                } else if (option == 1) {
                                    filterManager.openFiltersDialog(dataset, false, false, DatasetFilterType.NONE);
                                    instructions.setUseCurrentFilter(true);
                                    loadData(instructions);
                                } else if (option == 2) {
                                    filterManager.setActiveFilter(dataset, null);
                                    instructions.setUseCurrentFilter(true);
                                    loadData(instructions);
                                } else if (option == 3) {
                                    filter.setError(e.getMessage());
                                    instructions.setUseCurrentFilter(false);
                                    loadData(instructions);
                                }
                            });
                }
            } else {
                String message =
                        "Error loading data for " + datasetName + ". Could not connect to database.\n" +
                                "Database error message: " + e.getMessage();
                Messages.showErrorDialog(project, message);
            }
        });
    }


    private void focusEditor() {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
        fileEditorManager.openFile(databaseFile, true);
    }

    protected void setLoading(boolean loading) {
        if (status.set(LOADING, loading)) {
            DatasetEditorTable editorTable = getEditorTable();
            editorTable.setLoading(loading);
            GUIUtil.repaint(editorTable);
        }

    }

    public void deleteRecords() {
        DatasetEditorTable editorTable = getEditorTable();
        DatasetEditorModel model = getTableModel();

        int[] indexes = editorTable.getSelectedRows();
        model.deleteRecords(indexes);
    }

    public void insertRecord() {
        DatasetEditorTable editorTable = getEditorTable();
        DatasetEditorModel model = getTableModel();

        int[] indexes = editorTable.getSelectedRows();
        int rowIndex = indexes.length > 0 && indexes[0] < model.getRowCount() ? indexes[0] : 0;
        model.insertRecord(rowIndex);
    }

    public void duplicateRecord() {
        DatasetEditorTable editorTable = getEditorTable();
        DatasetEditorModel model = getTableModel();
        int[] indexes = editorTable.getSelectedRows();
        if (indexes.length == 1) {
            model.duplicateRecord(indexes[0]);
        }
    }

    public void openRecordEditor() {
        DatasetEditorTable editorTable = getEditorTable();
        DatasetEditorModel model = getTableModel();

        int index = editorTable.getSelectedRow();
        if (index == -1) index = 0;
        DatasetEditorModelRow row = model.getRowAtIndex(index);
        if (row != null) {
            editorTable.stopCellEditing();
            editorTable.selectRow(row.getIndex());
            DatasetRecordEditorDialog editorDialog = new DatasetRecordEditorDialog(getProject(), row);
            editorDialog.show();
        }
    }

    public void openRecordEditor(int index) {
        if (index > -1) {
            DatasetEditorModel model = getTableModel();
            DatasetEditorModelRow row = model.getRowAtIndex(index);

            if (row != null) {
                DatasetRecordEditorDialog editorDialog = new DatasetRecordEditorDialog(getProject(), row);
                editorDialog.show();
            }
        }
    }

    public DatasetEditorStatusHolder getStatus() {
        return status;
    }

    public boolean isInserting() {
        return getTableModel().is(INSERTING);
    }

    public boolean isLoading() {
        return status.is(LOADING);
    }

    public boolean isLoaded() {
        return status.is(LOADED);
    }

    public boolean isDirty() {
        return getTableModel().isDirty();
    }

    /**
     * The dataset is readonly. This can not be changed by the flag isReadonly
     */
    public boolean isReadonlyData() {
        return getTableModel().isReadonly();
    }

    public boolean isReadonly() {
        return editorState.isReadonly() || getTableModel().isEnvironmentReadonly();
    }

    public DatasetColumnSetup getColumnSetup() {
        return editorState.getColumnSetup();
    }

    public void setEnvironmentReadonly(boolean readonly) {
        getTableModel().setEnvironmentReadonly(readonly);
    }

    public void setReadonly(boolean readonly) {
        editorState.setReadonly(readonly);
    }

    public boolean isEditable() {
        DatasetEditorModel tableModel = getTableModel();
        ConnectionHandler connectionHandler = tableModel.getConnectionHandler();
        return tableModel.isEditable() && connectionHandler.isConnected(SessionId.MAIN);
    }

    public int getRowCount() {
        return getEditorTable().getRowCount();
    }


    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @Nullable
    @Override
    public DatabaseSession getDatabaseSession() {
        return getConnectionHandler().getSessionBundle().getMainSession();
    }

    /*******************************************************
     *                      Listeners                      *
     *******************************************************/
    private final ConnectionStatusListener connectionStatusListener = (connectionId, sessionId) -> {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler.getConnectionId() == connectionId && sessionId == SessionId.MAIN) {
            boolean connected = connectionHandler.isConnected(SessionId.MAIN);
            boolean statusChanged = getStatus().set(CONNECTED, connected);

            if (statusChanged) {
                Dispatch.run(() -> {
                    DatasetEditorTable editorTable = getEditorTable();
                    if (connected) {
                        editorTable.updateBackground(false);
                        GUIUtil.repaint(editorTable);
                        if (!isReadonlyData()) {
                            loadData(CON_STATUS_CHANGE_LOAD_INSTRUCTIONS);
                        }
                    } else {
                        editorTable.cancelEditing();
                        editorTable.updateBackground(true);
                        GUIUtil.repaint(editorTable);
                    }
                });
            }
        }
    };

    private final TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void beforeAction(@NotNull ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action) {
            if (connectionHandler == getConnectionHandler()) {
                DatasetEditorModel model = getTableModel();
                DatasetEditorTable editorTable = getEditorTable();
                if (action == TransactionAction.COMMIT) {

                    if (editorTable.isEditing()) {
                        editorTable.stopCellEditing();
                    }

                    if (isInserting()) {
                        try {
                            model.postInsertRecord(true, false, true);
                        } catch (SQLException e1) {
                            Messages.showErrorDialog(getProject(), "Could not create row in " + getDataset().getQualifiedNameWithType() + '.', e1);
                            model.cancelInsert(true);
                        }
                    }
                }

                if (action == TransactionAction.ROLLBACK || action == TransactionAction.ROLLBACK_IDLE) {
                    if (editorTable.isEditing()) {
                        editorTable.stopCellEditing();
                    }
                    if (isInserting()) {
                        model.cancelInsert(true);
                    }
                }
            }
        }

        @Override
        public void afterAction(@NotNull ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded) {
            if (connectionHandler == getConnectionHandler()) {
                DatasetEditorModel model = getTableModel();
                DatasetEditorTable editorTable = getEditorTable();
                if (action == TransactionAction.COMMIT || action == TransactionAction.ROLLBACK) {
                    if (succeeded && isModified()) loadData(CON_STATUS_CHANGE_LOAD_INSTRUCTIONS);
                }

                if (action == TransactionAction.DISCONNECT) {
                    editorTable.stopCellEditing();
                    model.revertChanges();
                    GUIUtil.repaint(editorTable);
                }
            }
        }
    };

    private final DataGridSettingsChangeListener dataGridSettingsChangeListener =
            visible -> loadData(COL_VISIBILITY_STATUS_CHANGE_LOAD_INSTRUCTIONS);


    String getDataLoadError() {
        return dataLoadError;
    }


    public List<DatasetColumnState> refreshColumnStates(@Nullable List<String> columnNames) {
        DatasetColumnSetup columnSetup = editorState.getColumnSetup();
        columnSetup.init(columnNames, getDataset());
        return columnSetup.getColumnStates();
    }

    @Nullable
    @Override
    public VirtualFile getFile() {
        return getDatabaseFile();
    }

    /*******************************************************
     *                   Data Provider                     *
     *******************************************************/
    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        if (DataKeys.DATASET_EDITOR.is(dataId)) {
            return DatasetEditor.this;
        }
        return null;
    }

    @Nullable
    public static DatasetEditor get(DataContext dataContext) {
        DatasetEditor datasetEditor = DataKeys.DATASET_EDITOR.getData(dataContext);
        if (datasetEditor == null) {
            FileEditor fileEditor = PlatformDataKeys.FILE_EDITOR.getData(dataContext);
            if (fileEditor instanceof DatasetEditor) {
                return (DatasetEditor) fileEditor;
            }
        }
        return datasetEditor;
    }

    @Nullable
    public static DatasetEditor get(AnActionEvent e) {
        DatasetEditor datasetEditor = e.getData((DataKeys.DATASET_EDITOR));
        if (datasetEditor == null) {
            FileEditor fileEditor = Lookup.getFileEditor(e);
            if (fileEditor instanceof DatasetEditor) {
                return (DatasetEditor) fileEditor;
            }
        } else {
            return datasetEditor;
        }
        return null;
    }

    @Override
    public String toString() {
        return databaseFile == null ? super.toString() : databaseFile.getPath();
    }

}
