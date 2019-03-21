package com.dci.intellij.dbn.editor.session.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.details.SessionDetailsTable;
import com.dci.intellij.dbn.editor.session.details.SessionDetailsTableModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SessionBrowserDetailsForm extends DBNFormImpl{
    private JPanel mainPanel;
    private JPanel sessionDetailsTabsPanel;
    private JBScrollPane sessionDetailsTablePane;
    private SessionDetailsTable sessionDetailsTable;
    private TabbedPane detailsTabbedPane;
    private JPanel explainPlanPanel;

    private SessionBrowser sessionBrowser;
    private SessionBrowserCurrentSqlPanel currentSqlPanel;

    public SessionBrowserDetailsForm(SessionBrowser sessionBrowser) {
        this.sessionBrowser = sessionBrowser;
        sessionDetailsTable = new SessionDetailsTable(sessionBrowser.getProject());
        sessionDetailsTablePane.setViewportView(sessionDetailsTable);
        sessionDetailsTablePane.getViewport().setBackground(sessionDetailsTable.getBackground());
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        JBSplitter splitter = (JBSplitter) mainPanel.getComponent(0);
        splitter.setProportion((float) 0.3);

        detailsTabbedPane = new TabbedPane(this);
        sessionDetailsTabsPanel.add(detailsTabbedPane, BorderLayout.CENTER);

        currentSqlPanel = new SessionBrowserCurrentSqlPanel(sessionBrowser);
        TabInfo currentSqlTabInfo = new TabInfo(currentSqlPanel.getComponent());
        currentSqlTabInfo.setText("Current Statement");
        currentSqlTabInfo.setIcon(Icons.FILE_SQL_CONSOLE);
        currentSqlTabInfo.setObject(currentSqlPanel);
        detailsTabbedPane.addTab(currentSqlTabInfo);

        ConnectionHandler connectionHandler = getConnectionHandler();
        if (DatabaseFeature.EXPLAIN_PLAN.isSupported(connectionHandler)) {
            explainPlanPanel = new JPanel(new BorderLayout());
            TabInfo explainPlanTabInfo = new TabInfo(new JPanel());
            explainPlanTabInfo.setText("Explain Plan");
            explainPlanTabInfo.setIcon(Icons.EXPLAIN_PLAN_RESULT);
            //explainPlanTabInfo.setObject(currentSqlPanel);
            detailsTabbedPane.addTab(explainPlanTabInfo);
        }

        detailsTabbedPane.addListener(new TabsListener.Adapter(){
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                if (newSelection.getText().equals("Explain Plan")) {

                }
            }
        });

        Disposer.register(this, sessionDetailsTable);
        Disposer.register(this, currentSqlPanel);
        Disposer.register(this, detailsTabbedPane);
    }

    @NotNull
    private ConnectionHandler getConnectionHandler() {
        return Failsafe.get(sessionBrowser.getConnectionHandler());
    }

    public void update(@Nullable final SessionBrowserModelRow selectedRow) {
        SessionDetailsTableModel model = new SessionDetailsTableModel(selectedRow);
        sessionDetailsTable.setModel(model);
        sessionDetailsTable.accommodateColumnsSize();
        currentSqlPanel.loadCurrentStatement();
    }

    public SessionBrowserCurrentSqlPanel getCurrentSqlPanel() {
        return currentSqlPanel;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
