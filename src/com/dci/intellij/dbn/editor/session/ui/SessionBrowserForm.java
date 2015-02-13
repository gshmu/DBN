package com.dci.intellij.dbn.editor.session.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.dci.intellij.dbn.data.find.SearchableDataComponent;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.editor.data.ui.table.cell.DatasetTableCellEditor;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.UIUtil;

public class SessionBrowserForm extends DBNFormImpl implements DBNForm, SearchableDataComponent {
    private JPanel actionsPanel;
    private JScrollPane editorTableScrollPane;
    private JPanel mainPanel;
    private JLabel loadingLabel;
    private JPanel loadingIconPanel;
    private JPanel searchPanel;
    private AutoCommitLabel autoCommitLabel;
    private JLabel loadTimestampLabel;
    private SessionBrowserTable editorTable;
    private DataSearchComponent dataSearchComponent;

    private SessionBrowser sessionBrowser;

    public SessionBrowserForm(SessionBrowser sessionBrowser) {
        this.sessionBrowser = sessionBrowser;
        try {
            editorTable = new SessionBrowserTable(sessionBrowser);
            editorTableScrollPane.setViewportView(editorTable);
            editorTableScrollPane.getViewport().setBackground(editorTable.getBackground());
            editorTable.initTableGutter();
            loadTimestampLabel.setForeground(Colors.HINT_COLOR);
            refreshLoadTimestamp();

            JPanel panel = new JPanel();
            panel.setBorder(UIUtil.getTableHeaderCellBorder());
            editorTableScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, panel);

            ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.SessionBrowser");
            actionToolbar.setTargetComponent(actionsPanel);

            actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
            loadingIconPanel.add(new AsyncProcessIcon("Loading"), BorderLayout.CENTER);
            hideLoadingHint();

            ActionUtil.registerDataProvider(actionsPanel, sessionBrowser.getDataProvider(), true);

            Disposer.register(this, autoCommitLabel);
            Disposer.register(this, editorTable);
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(
                    sessionBrowser.getProject(),
                    "Error",
                    "Error opening session browser for connection " + getConnectionHandler().getName(), e);
        }
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void showLoadingHint() {
        new ConditionalLaterInvocator() {
            public void execute() {
                loadingLabel.setVisible(true);
                loadingIconPanel.setVisible(true);
                loadTimestampLabel.setVisible(false);
                refreshLoadTimestamp();
            }
        }.start();
    }

    public void hideLoadingHint() {
        new ConditionalLaterInvocator() {
            public void execute() {
                loadingLabel.setVisible(false);
                loadingIconPanel.setVisible(false);
                refreshLoadTimestamp();

            }
        }.start();
    }

    public void refreshLoadTimestamp() {
        boolean visible = !loadingLabel.isVisible();
        if (visible) {
            SessionBrowserModel model = getEditorTable().getModel();
            long timestamp = model.getTimestamp();
/*
            RegionalSettings regionalSettings = RegionalSettings.getInstance(sessionBrowser.getProject());
            String dateTime = regionalSettings.getFormatter().formatTime(new Date(timestamp));
            loadTimestampLabel.setText("Updated: " + dateTime + " (" + DateFormatUtil.formatPrettyDateTime(timestamp)+ ")");
*/

            loadTimestampLabel.setText("Updated: " + DateFormatUtil.formatPrettyDateTime(timestamp));
        }
        loadTimestampLabel.setVisible(visible);
    }


    public SessionBrowserTable getEditorTable() {
        return editorTable;
    }

    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            sessionBrowser = null;
        }
    }

    private ConnectionHandler getConnectionHandler() {
        return sessionBrowser.getConnectionHandler();
    }

    public float getHorizontalScrollProportion() {
        editorTableScrollPane.getHorizontalScrollBar().getModel();
        return 0;
    }

    /*********************************************************
     *              SearchableDataComponent                  *
     *********************************************************/
    public void showSearchHeader() {
        editorTable.clearSelection();

        if (dataSearchComponent == null) {
            dataSearchComponent = new DataSearchComponent(this);
            searchPanel.add(dataSearchComponent, BorderLayout.CENTER);

            Disposer.register(this, dataSearchComponent);
        } else {
            dataSearchComponent.initializeFindModel();
        }
        if (searchPanel.isVisible()) {
            dataSearchComponent.getSearchField().selectAll();
        } else {
            searchPanel.setVisible(true);    
        }
        dataSearchComponent.getSearchField().requestFocus();

    }

    public void hideSearchHeader() {
        dataSearchComponent.resetFindModel();
        searchPanel.setVisible(false);
        editorTable.revalidate();
        editorTable.repaint();
        editorTable.requestFocus();
    }

    @Override
    public void cancelEditActions() {}

    @Override
    public String getSelectedText() {
        TableCellEditor cellEditor = editorTable.getCellEditor();
        if (cellEditor instanceof DatasetTableCellEditor) {
            DatasetTableCellEditor tableCellEditor = (DatasetTableCellEditor) cellEditor;
            return tableCellEditor.getTextField().getSelectedText();
        }
        return null;
    }

    @Override
    public BasicTable getTable() {
        return editorTable;
    }
}
