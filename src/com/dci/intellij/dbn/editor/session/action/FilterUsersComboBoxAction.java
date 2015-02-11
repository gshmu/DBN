package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FilterUsersComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterUsersComboBoxAction() {
        super("User", Icons.SB_FILTER_USER);
    }

    @Override
    String getFilterValue(@NotNull SessionBrowserFilterState modelFilter) {
        return modelFilter.getUser();
    }

    @Override
    void setFilterValue(SessionBrowserFilterState modelFilter, String filterValue) {
        modelFilter.setUser(filterValue);
    }

    @Override
    List<String> getDistinctValues(SessionBrowserModel model, SessionBrowserFilterState modelFilter) {
        return model.getDistinctUsers(modelFilter.getUser());
    }
}