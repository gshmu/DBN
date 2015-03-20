package com.dci.intellij.dbn.editor.data.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
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
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.UIUtil;

public class DatasetEditorForm extends DBNFormImpl implements SearchableDataComponent {
    private JPanel actionsPanel;
    private JScrollPane datasetTableScrollPane;
    private JPanel mainPanel;
    private JLabel loadingLabel;
    private JPanel loadingIconPanel;
    private JPanel searchPanel;
    private AutoCommitLabel autoCommitLabel;

    private DatasetEditorTable datasetEditorTable;
    private DatasetEditor datasetEditor;

    private LazyValue<DataSearchComponent> dataSearchComponent = new LazyValue<DataSearchComponent>(this) {
        @Override
        protected DataSearchComponent load() {
            DataSearchComponent dataSearchComponent = new DataSearchComponent(DatasetEditorForm.this);
            searchPanel.add(dataSearchComponent, BorderLayout.CENTER);
            return dataSearchComponent;
        }
    };


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
            autoCommitLabel.setConnectionHandler(connectionHandler);
        }
    }

    public DatasetEditorTable beforeRebuild() throws SQLException {
        DatasetEditorTable oldEditorTable = datasetEditorTable;
        datasetEditorTable = new DatasetEditorTable(datasetEditor);
        Disposer.register(this, datasetEditorTable);

        DataGridSettings dataGridSettings = DataGridSettings.getInstance(getProject());
        DataGridTrackingColumnSettings trackingColumnSettings = dataGridSettings.getTrackingColumnSettings();

        List<TableColumn> hiddenColumns = new ArrayList<TableColumn>();
        for (DatasetColumnState columnState : datasetEditor.getColumnSetup().getColumnStates()) {

            if (!columnState.isVisible() || !trackingColumnSettings.isColumnVisible(columnState.getName())) {
                int columnIndex = columnState.getPosition();
                TableColumn tableColumn = datasetEditorTable.getColumnModel().getColumn(columnIndex);
                hiddenColumns.add(tableColumn);
            }
        }
        for (TableColumn hiddenColumn : hiddenColumns) {
            datasetEditorTable.removeColumn(hiddenColumn);
        }
        return oldEditorTable;
    }

    public void afterRebuild(final DatasetEditorTable oldEditorTable) {
        if (oldEditorTable != null) {
            new ConditionalLaterInvocator(){
                @Override
                protected void execute() {
                    if (!isDisposed()) {
                        datasetTableScrollPane.setViewportView(datasetEditorTable);
                        datasetEditorTable.initTableGutter();
                        datasetEditorTable.updateBackground(false);

                        Disposer.dispose(oldEditorTable);
                    }
                }
            }.start();
        }
    }

    private DBDataset getDataset() {
        return datasetEditor.getDataset();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void showLoadingHint() {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                loadingLabel.setVisible(true);
                loadingIconPanel.setVisible(true);
            }
        }.start();
    }

    public void hideLoadingHint() {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                loadingLabel.setVisible(false);
                loadingIconPanel.setVisible(false);
            }
        }.start();
    }


    @NotNull
    public DatasetEditorTable getEditorTable() {
        return FailsafeUtil.get(datasetEditorTable);
    }

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
    public void showSearchHeader() {
        datasetEditorTable.cancelEditing();
        datasetEditorTable.clearSelection();

        DataSearchComponent dataSearchComponent = this.dataSearchComponent.get();
        dataSearchComponent.initializeFindModel();

        if (searchPanel.isVisible()) {
            dataSearchComponent.getSearchField().selectAll();
        } else {
            searchPanel.setVisible(true);    
        }
        dataSearchComponent.getSearchField().requestFocus();

    }

    public void hideSearchHeader() {
        dataSearchComponent.get().resetFindModel();
        searchPanel.setVisible(false);
        datasetEditorTable.revalidate();
        datasetEditorTable.repaint();
        datasetEditorTable.requestFocus();
    }

    @Override
    public void cancelEditActions() {
        datasetEditorTable.cancelEditing();
    }

    @Override
    public String getSelectedText() {
        TableCellEditor cellEditor = datasetEditorTable.getCellEditor();
        if (cellEditor instanceof DatasetTableCellEditor) {
            DatasetTableCellEditor tableCellEditor = (DatasetTableCellEditor) cellEditor;
            return tableCellEditor.getTextField().getSelectedText();
        }
        return null;
    }

    @Override
    public BasicTable getTable() {
        return datasetEditorTable;
    }

    private void createUIComponents() {
        datasetTableScrollPane = new BasicTableScrollPane();
    }
}
