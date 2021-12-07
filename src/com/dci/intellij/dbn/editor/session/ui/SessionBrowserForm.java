package com.dci.intellij.dbn.editor.session.ui;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.table.DBNTableHeaderRenderer;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.dci.intellij.dbn.data.find.SearchableDataComponent;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableScrollPane;
import com.dci.intellij.dbn.editor.data.ui.table.cell.DatasetTableCellEditor;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.GuiUtils;
import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.ui.AsyncProcessIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.sql.SQLException;

public class SessionBrowserForm extends DBNFormImpl implements SearchableDataComponent {
    private JPanel actionsPanel;
    private JScrollPane editorTableScrollPane;
    private JPanel mainPanel;
    private JLabel loadingLabel;
    private JPanel loadingIconPanel;
    private JPanel searchPanel;
    private JLabel loadTimestampLabel;
    private JPanel detailsPanel;
    private JPanel editorPanel;
    private SessionBrowserTable editorTable;

    private final Latent<DataSearchComponent> dataSearchComponent = Latent.basic(() -> {
        DataSearchComponent dataSearchComponent = new DataSearchComponent(SessionBrowserForm.this);
        searchPanel.add(dataSearchComponent.getComponent(), BorderLayout.CENTER);
        DataManager.registerDataProvider(dataSearchComponent.getSearchField(), this);
        return dataSearchComponent;
    });

    private final WeakRef<SessionBrowser> sessionBrowser;
    private SessionBrowserDetailsForm detailsForm;

    public SessionBrowserForm(SessionBrowser sessionBrowser) {
        super(sessionBrowser, sessionBrowser.getProject());
        this.sessionBrowser = WeakRef.of(sessionBrowser);
        try {
            editorTable = new SessionBrowserTable(this, sessionBrowser);
            editorTableScrollPane.setViewportView(editorTable);
            editorTableScrollPane.getViewport().setBackground(editorTable.getBackground());
            editorTable.initTableGutter();
            detailsForm = new SessionBrowserDetailsForm(this, sessionBrowser);
            detailsPanel.add(detailsForm.getComponent(), BorderLayout.CENTER);

            loadTimestampLabel.setForeground(Colors.HINT_COLOR);
            GuiUtils.replaceJSplitPaneWithIDEASplitter(editorPanel);
            GUIUtil.updateSplitterProportion(editorPanel, (float) 0.6);

            refreshLoadTimestamp();

            JPanel panel = new JPanel();
            panel.setBorder(DBNTableHeaderRenderer.BORDER_LBR.get());
            editorTableScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, panel);

            ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel,"", true, "DBNavigator.ActionGroup.SessionBrowser");

            actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
            loadingIconPanel.add(new AsyncProcessIcon("Loading"), BorderLayout.CENTER);
            hideLoadingHint();

            DataManager.registerDataProvider(actionsPanel, this);
        } catch (SQLException e) {
            Messages.showErrorDialog(
                    sessionBrowser.getProject(),
                    "Error",
                    "Error opening session browser for connection " + getConnectionHandler().getName(), e);
        }
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public SessionBrowserDetailsForm getDetailsForm() {
        return detailsForm;
    }

    public void showLoadingHint() {
        Dispatch.run(() -> {
            Failsafe.nd(this);
            loadingLabel.setVisible(true);
            loadingIconPanel.setVisible(true);
            loadTimestampLabel.setVisible(false);
            refreshLoadTimestamp();
        });
    }

    public void hideLoadingHint() {
        Dispatch.run(() -> {
            Failsafe.nd(this);
            loadingLabel.setVisible(false);
            loadingIconPanel.setVisible(false);
            refreshLoadTimestamp();
        });
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


    @NotNull
    public SessionBrowserTable getEditorTable() {
        return Failsafe.nn(editorTable);
    }

    @NotNull
    public SessionBrowser getSessionBrowser() {
        return sessionBrowser.ensure();
    }

    @NotNull
    private ConnectionHandler getConnectionHandler() {
        return getSessionBrowser().getConnectionHandler();
    }

    public float getHorizontalScrollProportion() {
        editorTableScrollPane.getHorizontalScrollBar().getModel();
        return 0;
    }

    /*********************************************************
     *              SearchableDataComponent                  *
     *********************************************************/
    @Override
    public void showSearchHeader() {
        getEditorTable().clearSelection();

        DataSearchComponent dataSearchComponent = getSearchComponent();
        dataSearchComponent.initializeFindModel();
        JTextField searchField = dataSearchComponent.getSearchField();
        if (searchPanel.isVisible()) {
            searchField.selectAll();
        } else {
            searchPanel.setVisible(true);    
        }
        searchField.requestFocus();

    }

    private DataSearchComponent getSearchComponent() {
        return dataSearchComponent.get();
    }

    @Override
    public void hideSearchHeader() {
        getSearchComponent().resetFindModel();
        searchPanel.setVisible(false);
        SessionBrowserTable editorTable = getEditorTable();
        GUIUtil.repaintAndFocus(editorTable);
    }

    @Override
    public void cancelEditActions() {}

    @Override
    public String getSelectedText() {
        TableCellEditor cellEditor = getEditorTable().getCellEditor();
        if (cellEditor instanceof DatasetTableCellEditor) {
            DatasetTableCellEditor tableCellEditor = (DatasetTableCellEditor) cellEditor;
            return tableCellEditor.getTextField().getSelectedText();
        }
        return null;
    }

    @NotNull
    @Override
    public BasicTable getTable() {
        return getEditorTable();
    }

    private void createUIComponents() {
        editorTableScrollPane = new BasicTableScrollPane();
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        Object data = super.getData(dataId);
        if (data == null) {
            data = getSessionBrowser().getData(dataId);
        }
        return data;
    }

    @Override
    protected void disposeInner() {
        DataManager.removeDataProvider(actionsPanel);
        super.disposeInner();
        editorTable = null;
    }
}
