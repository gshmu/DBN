package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.util.List;

public abstract class AbstractFilterComboBoxAction extends DBNComboBoxAction {
    private String type;
    private Icon icon;

    public AbstractFilterComboBoxAction(String type, Icon icon) {
        this.type = type;
        this.icon = icon;
    }

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        SessionBrowser sessionBrowser = getSessionBrowser(component);
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new SelectFilterValueAction(null));
        actionGroup.addSeparator();
        if (sessionBrowser != null) {
            DBSessionBrowserVirtualFile databaseFile = sessionBrowser.getDatabaseFile();
            SessionBrowserModel model = databaseFile.getModel();
            if (model != null) {
                List<String> filterValues = getDistinctValues(model, model.getFilter());
                for (String filterValue : filterValues) {
                    SelectFilterValueAction action = new SelectFilterValueAction(filterValue);
                    actionGroup.add(action);
                }
            }
        }
        return actionGroup;
    }

    public synchronized void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        String text = type;
        Icon icon = null;

        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            SessionBrowserModel model = sessionBrowser.getDatabaseFile().getModel();
            if (model != null) {
                SessionBrowserFilterState modelFilter = model.getFilter();
                if (modelFilter != null) {
                    String filterValue = getFilterValue(modelFilter);
                    if (StringUtil.isNotEmpty(filterValue)) {
                        text = filterValue;
                        icon = this.icon;
                    }
                }
            }
        }

        presentation.setText(text);
        presentation.setIcon(icon);
    }

    abstract String getFilterValue(@NotNull SessionBrowserFilterState modelFilter);

    abstract void setFilterValue(SessionBrowserFilterState modelFilter, String filterValue);

    abstract List<String> getDistinctValues(SessionBrowserModel model, SessionBrowserFilterState modelFilter);

    @Nullable
    public static SessionBrowser getSessionBrowser(JComponent component) {
        DataContext dataContext = DataManager.getInstance().getDataContext(component);
        SessionBrowser sessionBrowser = DBNDataKeys.SESSION_BROWSER.getData(dataContext);
        if (sessionBrowser == null) {
            FileEditor fileEditor = PlatformDataKeys.FILE_EDITOR.getData(dataContext);
            if (fileEditor instanceof SessionBrowser) {
                sessionBrowser = (SessionBrowser) fileEditor;
            }
        }
        return sessionBrowser;
    }

    @Nullable
    public static SessionBrowser getSessionBrowser(AnActionEvent e) {
        SessionBrowser sessionBrowser = e.getData((DBNDataKeys.SESSION_BROWSER));
        if (sessionBrowser == null) {
            FileEditor fileEditor = e.getData(PlatformDataKeys.FILE_EDITOR);
            if (fileEditor instanceof SessionBrowser) {
                sessionBrowser = (SessionBrowser) fileEditor;
            }
        }
        return sessionBrowser;
    }

    private class SelectFilterValueAction extends AnAction {
        private String filterValue;

        public SelectFilterValueAction(String filterValue) {
            super(filterValue == null ? "No Filter" : filterValue, null, filterValue == null ? null : icon);
            this.filterValue = filterValue;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            SessionBrowser sessionBrowser = getSessionBrowser(e);
            if (sessionBrowser != null) {
                DBSessionBrowserVirtualFile sessionBrowserFile = sessionBrowser.getDatabaseFile();
                SessionBrowserModel model = sessionBrowserFile.getModel();
                if (model !=  null) {
                    SessionBrowserFilterState modelFilter = model.getFilter();
                    setFilterValue(modelFilter, filterValue);
                    sessionBrowser.refreshTable();
                }
            }
        }
    }
 }