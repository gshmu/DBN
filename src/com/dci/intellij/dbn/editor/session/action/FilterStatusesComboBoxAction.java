package com.dci.intellij.dbn.editor.session.action;

import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelFilter;

public class FilterStatusesComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterStatusesComboBoxAction() {
        super("Status", Icons.SB_FILTER_STATUS);
    }

    @Override
    String getFilterValue(SessionBrowserModelFilter modelFilter) {
        return modelFilter.getStatus();
    }

    @Override
    void setFilterValue(SessionBrowserModelFilter modelFilter, String filterValue) {
        modelFilter.setStatus(filterValue);
    }

    @Override
    List<String> getDistinctValues(SessionBrowserModel model, SessionBrowserModelFilter modelFilter) {
        return model.getDistinctStatuses(modelFilter.getStatus());
    }
}