package com.dci.intellij.dbn.editor.data.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.dci.intellij.dbn.data.find.SearchableDataComponent;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.data.grid.options.DataGridTrackingColumnSettings;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableScrollPane;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.state.column.DatasetColumnState;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.dci.intellij.dbn.editor.data.ui.table.cell.DatasetTableCellEditor;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatasetEditorForm extends DBNFormImpl implements SearchableDataComponent {
    private JPanel actionsPanel;
    private JScrollPane datasetTableScrollPane;
    private JPanel mainPanel;
    private JLabel loadingLabel;
    private JPanel loadingIconPanel;
    private JPanel searchPanel;
    private AutoCommitLabel autoCommitLabel;
    private JPanel loadingActionPanel;
    private JPanel loadingDataPanel;

    private DatasetEditorTable datasetEditorTable;
    private DatasetEditor datasetEditor;

    private Latent<DataSearchComponent> dataSearchComponent = Latent.disposable(this, () -> {
        DataSearchComponent dataSearchComponent = new DataSearchComponent(DatasetEditorForm.this);
        searchPanel.add(dataSearchComponent.getComponent(), BorderLayout.CENTER);
        ActionUtil.registerDataProvider(dataSearchComponent.getSearchField(), getDatasetEditor());
        return dataSearchComponent;
    });


    public DatasetEditorForm(DatasetEditor datasetEditor) {
        super(datasetEditor.getProject());
        this.datasetEditor = datasetEditor;
        DBDataset dataset = getDataset();
        try {
            datasetEditorTable = new DatasetEditorTable(datasetEditor);
            datasetTableScrollPane.setViewportView(datasetEditorTable);
            datasetEditorTable.initTableGutter();


            JPanel panel = new JPanel();
            panel.setBorder(UIUtil.getTableHeaderCellBorder());
            datasetTableScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, panel);

            ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.DataEditor");
            actionToolbar.setTargetComponent(actionsPanel);

            actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
            loadingIconPanel.add(new AsyncProcessIcon("Loading"), BorderLayout.CENTER);
            hideLoadingHint();

            ActionToolbar loadingActionToolbar = ActionUtil.createActionToolbar("", true, new CancelLoadingAction());
            actionToolbar.setTargetComponent(actionsPanel);
            loadingActionPanel.add(loadingActionToolbar.getComponent(), BorderLayout.CENTER);

            ActionUtil.registerDataProvider(mainPanel, datasetEditor);

            Disposer.register(this, autoCommitLabel);
            Disposer.register(this, datasetEditorTable);
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(
                    getProject(),
                    "Error",
                    "Error opening data editor for " + dataset.getQualifiedNameWithType(), e);
        }

        if (dataset.isEditable(DBContentType.DATA)) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            autoCommitLabel.init(getProject(), datasetEditor.getFile(), connectionHandler, SessionId.MAIN);
        }
    }

    public DatasetEditorTable beforeRebuild() throws SQLException {
        DatasetEditorTable oldEditorTable = getEditorTable();
        DatasetEditor datasetEditor = getDatasetEditor();
        datasetEditorTable = new DatasetEditorTable(datasetEditor);
        Disposer.register(this, datasetEditorTable);


        DataGridSettings dataGridSettings = DataGridSettings.getInstance(getProject());
        DataGridTrackingColumnSettings trackingColumnSettings = dataGridSettings.getTrackingColumnSettings();

        List<TableColumn> hiddenColumns = new ArrayList<>();
        for (DatasetColumnState columnState : datasetEditor.getColumnSetup().getColumnStates()) {

            if (!columnState.isVisible() || !trackingColumnSettings.isColumnVisible(columnState.getName())) {
                String columnName = columnState.getName();
                TableColumn tableColumn = datasetEditorTable.getColumnByName(columnName);
                if (tableColumn != null) {
                    hiddenColumns.add(tableColumn);
                }
            }
        }
        for (TableColumn hiddenColumn : hiddenColumns) {
            datasetEditorTable.removeColumn(hiddenColumn);
        }
        return oldEditorTable;
    }

    public void afterRebuild(final DatasetEditorTable oldEditorTable) {
        if (oldEditorTable != null) {
            Dispatch.invokeNonModal(() -> {
                DatasetEditorTable datasetEditorTable = getEditorTable();
                datasetTableScrollPane.setViewportView(datasetEditorTable);
                datasetEditorTable.initTableGutter();
                datasetEditorTable.updateBackground(false);

                DisposerUtil.disposeInBackground(oldEditorTable);
            });
        }
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @NotNull
    private DBDataset getDataset() {
        return getDatasetEditor().getDataset();
    }

    @NotNull
    public DatasetEditor getDatasetEditor() {
        return Failsafe.get(datasetEditor);
    }

    public void showLoadingHint() {
        Dispatch.invokeNonModal(() -> loadingDataPanel.setVisible(true));
    }

    public void hideLoadingHint() {
        Dispatch.invokeNonModal(() -> loadingDataPanel.setVisible(false));
    }

    @NotNull
    public DatasetEditorTable getEditorTable() {
        return Failsafe.get(datasetEditorTable);
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            datasetEditor = null;
            datasetEditorTable = null;
        }
    }

    private ConnectionHandler getConnectionHandler() {
        return getEditorTable().getDataset().getConnectionHandler();
    }

    public float getHorizontalScrollProportion() {
        datasetTableScrollPane.getHorizontalScrollBar().getModel();
        return 0;
    }

    /*********************************************************
     *              SearchableDataComponent                  *
     *********************************************************/
    @Override
    public void showSearchHeader() {
        DatasetEditorTable editorTable = getEditorTable();
        editorTable.cancelEditing();
        editorTable.clearSelection();

        DataSearchComponent dataSearchComponent = getSearchComponent();
        dataSearchComponent.initializeFindModel();

        JTextField searchField = dataSearchComponent.getSearchField();
        if (searchPanel.isVisible()) {
            searchField.selectAll();
        } else {
            searchPanel.setVisible(true);    
        }
        Dispatch.invoke(() -> searchField.requestFocus());
    }

    private DataSearchComponent getSearchComponent() {
        return dataSearchComponent.get();
    }

    @Override
    public void hideSearchHeader() {
        getSearchComponent().resetFindModel();
        searchPanel.setVisible(false);
        DatasetEditorTable editorTable = getEditorTable();

        GUIUtil.repaintAndFocus(editorTable);
    }

    @Override
    public void cancelEditActions() {
        getEditorTable().cancelEditing();
    }

    @Override
    public String getSelectedText() {
        DatasetTableCellEditor cellEditor = getEditorTable().getCellEditor();
        if (cellEditor != null) {
            return cellEditor.getTextField().getSelectedText();
        }
        return null;
    }

    @NotNull
    @Override
    public BasicTable getTable() {
        return getEditorTable();
    }

    private void createUIComponents() {
        datasetTableScrollPane = new BasicTableScrollPane();
    }

    private class CancelLoadingAction extends AnAction {
        CancelLoadingAction() {
            super("Cancel", null, Icons.DATA_EDITOR_STOP_LOADING);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            getEditorTable().getModel().cancelDataLoad();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(!getEditorTable().getModel().isLoadCancelled());
        }
    }
}
