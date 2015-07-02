package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;

public class ShowReferencedRecordAction extends ShowRecordsAction {

    public ShowReferencedRecordAction(DatasetFilterInput filterInput) {
        super("Show referenced " + filterInput.getDataset().getName() + " record", filterInput);
    }
}
