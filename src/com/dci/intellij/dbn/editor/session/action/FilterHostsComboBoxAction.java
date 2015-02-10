package com.dci.intellij.dbn.editor.session.action;

import java.util.List;

import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelFilter;

public class FilterHostsComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterHostsComboBoxAction() {
        super("Host", null);
    }

    @Override
    String getFilterValue(SessionBrowserModelFilter modelFilter) {
        return modelFilter.getHost();
    }

    @Override
    void setFilterValue(SessionBrowserModelFilter modelFilter, String filterValue) {
        modelFilter.setHost(filterValue);
    }

    @Override
    List<String> getDistinctValues(SessionBrowserModel model, SessionBrowserModelFilter modelFilter) {
        return model.getDistinctHosts(modelFilter.getHost());
    }
}