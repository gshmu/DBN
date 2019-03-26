package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterType;
import com.dci.intellij.dbn.editor.session.SessionInterruptionType;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserColumnInfo;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelCell;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SessionBrowserTableActionGroup extends DefaultActionGroup {
    boolean isHeaderAction;
    private SessionBrowser sessionBrowser;
    private SessionBrowserModelRow row;
    public SessionBrowserTableActionGroup(SessionBrowser sessionBrowser, @Nullable SessionBrowserModelCell cell, SessionBrowserColumnInfo columnInfo) {
        this.sessionBrowser = sessionBrowser;
        SessionBrowserTable table = sessionBrowser.getEditorTable();

        isHeaderAction = cell == null;
        row = cell == null ? null : cell.getRow();
        SessionBrowserModel tableModel = sessionBrowser.getTableModel();

        add(new ReloadSessionsAction());
        if (table.getSelectedRowCount() > 0) {
            int rowCount = table.getSelectedRowCount();
            addSeparator();
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (DatabaseFeature.SESSION_DISCONNECT.isSupported(connectionHandler)) {
                add(new DisconnectSessionAction(rowCount > 1));
            }
            add(new KillSessionAction(rowCount > 1));
        }

        addSeparator();

        if (cell != null) {
            Object userValue = cell.getUserValue();
            if (userValue instanceof String) {
                SessionBrowserFilterType filterType = columnInfo.getFilterType();
                if (filterType != null && tableModel != null) {
                    SessionBrowserFilterState filter = tableModel.getFilter();
                    if (filter == null || StringUtil.isEmpty(filter.getFilterValue(filterType))) {
                        add(new FilterByAction(filterType, userValue.toString()));
                    }
                }
            }
        }

        if (tableModel != null && !tableModel.getState().getFilterState().isEmpty()) {
            add(new ClearFilterAction());
        }
    }

    @NotNull
    private ConnectionHandler getConnectionHandler() {
        return Failsafe.nn(sessionBrowser.getConnectionHandler());
    }

    private class ReloadSessionsAction extends DumbAwareAction {
        private ReloadSessionsAction() {
            super("Reload", null, Icons.ACTION_REFRESH);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            sessionBrowser.loadSessions(true);
        }
    }

    private class KillSessionAction extends DumbAwareAction {
        private KillSessionAction(boolean multiple) {
            super(multiple ? "Kill Sessions" : "Kill Session", null, Icons.ACTION_KILL_SESSION);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            if (row != null) {
                sessionBrowser.interruptSession(
                        row.getSessionId(),
                        row.getSerialNumber(),
                        SessionInterruptionType.KILL);
            }

        }
    }

    private class DisconnectSessionAction extends DumbAwareAction {
        private DisconnectSessionAction(boolean multiple) {
            super(multiple ? "Disconnect Sessions" : "Disconnect Session", null, Icons.ACTION_DISCONNECT_SESSION);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            if (row != null) {
                sessionBrowser.interruptSession(
                        row.getSessionId(),
                        row.getSerialNumber(),
                        SessionInterruptionType.DISCONNECT);
            }
        }
    }

    private class ClearFilterAction extends DumbAwareAction {
        private ClearFilterAction() {
            super("Clear Filter", null, Icons.DATASET_FILTER_CLEAR);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            sessionBrowser.clearFilter();
        }
    }

    private class FilterByAction extends DumbAwareAction {
        private SessionBrowserFilterType filterType;
        private String name;
        private FilterByAction(SessionBrowserFilterType filterType, String name) {
            super("Filter by " + filterType.name().toLowerCase() + " \"" + name + "\"", null, Icons.DATASET_FILTER);
            this.filterType = filterType;
            this.name = name;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            if (row != null) {
                SessionBrowserModel tableModel = sessionBrowser.getTableModel();
                if (tableModel != null) {
                    SessionBrowserFilterState filterState = tableModel.getState().getFilterState();
                    filterState.setFilterValue(filterType, name);

                    SessionBrowserSettings sessionBrowserSettings = sessionBrowser.getSettings();
                    if (sessionBrowserSettings.isReloadOnFilterChange()) {
                        sessionBrowser.loadSessions(false);
                    } else {
                        sessionBrowser.refreshTable();
                    }
                }
            }
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setText("Filter by " + filterType.name().toLowerCase() + " \"" + name + "\"", false);
        }
    }
}
