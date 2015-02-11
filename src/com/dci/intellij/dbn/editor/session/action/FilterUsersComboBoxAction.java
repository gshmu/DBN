package com.dci.intellij.dbn.editor.session.action;

import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelFilter;

public class FilterUsersComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterUsersComboBoxAction() {
        super("User", Icons.SB_FILTER_USER);
    }

    @Override
    String getFilterValue(SessionBrowserModelFilter modelFilter) {
        return modelFilter.getUser();
    }

    @Override
    void setFilterValue(SessionBrowserModelFilter modelFilter, String filterValue) {
        modelFilter.setUser(filterValue);
    }

    @Override
    List<String> getDistinctValues(SessionBrowserModel model, SessionBrowserModelFilter modelFilter) {
        return model.getDistinctUsers(modelFilter.getUser());
    }
}