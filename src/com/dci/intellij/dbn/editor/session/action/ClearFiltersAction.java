package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.data.DatasetLoadInstructions;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class ClearFiltersAction extends AbstractSessionBrowserAction {

    public static final DatasetLoadInstructions LOAD_INSTRUCTIONS = new DatasetLoadInstructions(true, true, true, false);

    public ClearFiltersAction() {
        super("Clear Filter", Icons.DATASET_FILTER_CLEAR);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            sessionBrowser.clearFilter();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Clear Filter");

        boolean enabled = false;
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            SessionBrowserModel tableModel = sessionBrowser.getTableModel();
            SessionBrowserFilterState filter = tableModel.getFilter();
            if (filter != null) {
                enabled =
                    StringUtil.isNotEmpty(filter.getUser()) ||
                    StringUtil.isNotEmptyOrSpaces(filter.getHost()) ||
                    StringUtil.isNotEmptyOrSpaces(filter.getStatus());
            }
        }

        presentation.setEnabled(enabled);

    }
}