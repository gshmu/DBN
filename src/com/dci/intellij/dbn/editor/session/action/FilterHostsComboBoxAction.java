package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FilterHostsComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterHostsComboBoxAction() {
        super("Host", Icons.SB_FILTER_SERVER);
    }

    @Override
    String getFilterValue(@NotNull SessionBrowserFilterState modelFilter) {
        return modelFilter.getHost();
    }

    @Override
    void setFilterValue(SessionBrowserFilterState modelFilter, String filterValue) {
        modelFilter.setHost(filterValue);
    }

    @Override
    List<String> getDistinctValues(SessionBrowserModel model, SessionBrowserFilterState modelFilter) {
        return model.getDistinctHosts(modelFilter.getHost());
    }
}