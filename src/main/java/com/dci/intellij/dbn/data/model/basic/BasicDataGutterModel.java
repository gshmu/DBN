package com.dci.intellij.dbn.data.model.basic;

import com.dci.intellij.dbn.common.ui.table.DBNTableGutterModel;

public class BasicDataGutterModel extends DBNTableGutterModel<BasicDataModel> {
    public BasicDataGutterModel(BasicDataModel dataModel) {
        super(dataModel);
    }

    @Override
    public Object getElementAt(int index) {
        return getTableModel().getRowAtIndex(index);
    }
}
