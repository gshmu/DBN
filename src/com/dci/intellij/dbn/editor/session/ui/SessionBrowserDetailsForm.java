package com.dci.intellij.dbn.editor.session.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.details.SessionDetailsTable;
import com.dci.intellij.dbn.editor.session.details.SessionDetailsTableModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class SessionBrowserDetailsForm extends DBNFormImpl{
    private JPanel mailPanel;
    private JPanel sessionDetailsTabsPanel;
    private JBScrollPane sessionDetailsTablePane;
    private SessionDetailsTable sessionDetailsTable;
    private TabbedPane detailsTabbedPane;

    private SessionBrowser sessionBrowser;
    private SessionBrowserCurrentSqlPanel currentSqlPanel;

    public SessionBrowserDetailsForm(SessionBrowser sessionBrowser) {
        this.sessionBrowser = sessionBrowser;
        sessionDetailsTable = new SessionDetailsTable(sessionBrowser.getProject());
        sessionDetailsTablePane.setViewportView(sessionDetailsTable);
        sessionDetailsTablePane.getViewport().setBackground(sessionDetailsTable.getBackground());
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mailPanel);
        JBSplitter splitter = (JBSplitter) mailPanel.getComponent(0);
        splitter.setProportion((float) 0.3);

        detailsTabbedPane = new TabbedPane(this);
        sessionDetailsTabsPanel.add(detailsTabbedPane, BorderLayout.CENTER);

        currentSqlPanel = new SessionBrowserCurrentSqlPanel(sessionBrowser);
        TabInfo currentSqlTabInfo = new TabInfo(currentSqlPanel.getComponent());
        currentSqlTabInfo.setText("Current Statement");
        currentSqlTabInfo.setIcon(Icons.FILE_SQL_CONSOLE);
        currentSqlTabInfo.setObject(currentSqlPanel);
        detailsTabbedPane.addTab(currentSqlTabInfo);

        Disposer.register(this, sessionDetailsTable);
        Disposer.register(this, currentSqlPanel);
        Disposer.register(this, detailsTabbedPane);
    }


    public void update(@Nullable final SessionBrowserModelRow selectedRow) {
        SessionDetailsTableModel model = new SessionDetailsTableModel(selectedRow);
        sessionDetailsTable.setModel(model);
        sessionDetailsTable.accommodateColumnsSize();
        currentSqlPanel.loadCurrentStatement();
    }

    @Override
    public JComponent getComponent() {
        return mailPanel;
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
        }


    }
}
