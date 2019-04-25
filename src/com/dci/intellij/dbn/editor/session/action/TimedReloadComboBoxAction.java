package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TimedReloadComboBoxAction extends DBNComboBoxAction implements DumbAware {

    public TimedReloadComboBoxAction() {
    }

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new SelectRefreshTimeAction(0));
        actionGroup.addSeparator();
        actionGroup.add(new SelectRefreshTimeAction(5));
        actionGroup.add(new SelectRefreshTimeAction(10));
        actionGroup.add(new SelectRefreshTimeAction(20));
        actionGroup.add(new SelectRefreshTimeAction(30));
        actionGroup.add(new SelectRefreshTimeAction(60));
        return actionGroup;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Icon icon = Icons.ACTION_TIMED_REFRESH_OFF;
        String text = "No refresh";


        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            int refreshInterval = sessionBrowser.getRefreshInterval();
            if (refreshInterval > 0) {
                text = refreshInterval + " seconds";
                if (sessionBrowser.isPreventLoading(false)) {
                    icon = Icons.ACTION_TIMED_REFRESH_INTERRUPTED;
                } else {
                    icon = Icons.ACTION_TIMED_REFRESH;
                }

            }
        }

        presentation.setText(text);
        presentation.setIcon(icon);
    }

    @Nullable
    public static SessionBrowser getSessionBrowser(JComponent component) {
        DataContext dataContext = DataManager.getInstance().getDataContext(component);
        SessionBrowser sessionBrowser = DataKeys.SESSION_BROWSER.getData(dataContext);
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
        SessionBrowser sessionBrowser = e.getData((DataKeys.SESSION_BROWSER));
        if (sessionBrowser == null) {
            FileEditor fileEditor = Lookup.getFileEditor(e);
            if (fileEditor instanceof SessionBrowser) {
                sessionBrowser = (SessionBrowser) fileEditor;
            }
        }
        return sessionBrowser;
    }

    private class SelectRefreshTimeAction extends AnAction {
        private int seconds;

        public SelectRefreshTimeAction(int seconds) {
            super(seconds == 0 ? "No refresh" : seconds + " seconds", null, seconds == 0 ? null : Icons.COMMON_TIMER);
            this.seconds = seconds;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            SessionBrowser sessionBrowser = getSessionBrowser(e);
            if (sessionBrowser != null) {
                sessionBrowser.setRefreshInterval(seconds);
            }
        }
    }
 }