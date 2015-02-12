package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
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
        if (cell != null) {
            addSeparator();
            DatabaseCompatibilityInterface compatibilityInterface = sessionBrowser.getConnectionHandler().getInterfaceProvider().getCompatibilityInterface();
            if (compatibilityInterface.supportsFeature(DatabaseFeature.SESSION_DISCONNECT)) {
                add(new DisconnectSessionAction());
            }
            add(new KillSessionAction());

            addSeparator();

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

    private class ReloadSessionsAction extends DumbAwareAction {
        private ReloadSessionsAction() {
            super("Reload Sessions", null, Icons.ACTION_REFRESH);
        }

        public void actionPerformed(AnActionEvent e) {
            if (row != null) {
                sessionBrowser.reload(true);
            }

        }
    }

    private class KillSessionAction extends DumbAwareAction {
        private KillSessionAction() {
            super("Kill Session", null, Icons.ACTION_KILL_SESSION);
        }

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
        private DisconnectSessionAction() {
            super("Disconnect Session", null, Icons.ACTION_DISCONNECT_SESSION);
        }

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

        public void actionPerformed(AnActionEvent e) {
            if (row != null) {
                sessionBrowser.clearFilter();
            }
        }
    }

    private class FilterByAction extends DumbAwareAction {
        private SessionBrowserFilterType filterType;
        private String name;
        private FilterByAction(SessionBrowserFilterType filterType, String name) {
            super("Filter by " + filterType.name().toLowerCase() + " \"" + NamingUtil.enhanceUnderscoresForDisplay(name) + "\"", null, Icons.DATASET_FILTER);
            this.filterType = filterType;
            this.name = name;
        }

        public void actionPerformed(AnActionEvent e) {
            if (row != null) {
                SessionBrowserModel tableModel = sessionBrowser.getTableModel();
                if (tableModel != null) {
                    SessionBrowserFilterState filterState = tableModel.getState().getFilterState();
                    filterState.setFilterValue(filterType, name);

                    SessionBrowserSettings sessionBrowserSettings = sessionBrowser.getSettings();
                    if (sessionBrowserSettings.isReloadOnFilterChange()) {
                        sessionBrowser.reload(false);
                    } else {
                        sessionBrowser.refreshTable();
                    }
                }
            }

        }
    }
}
