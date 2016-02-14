package com.dci.intellij.dbn.common.properties.ui;

import com.dci.intellij.dbn.common.ui.table.DBNEditableTable;

import java.util.Map;

public class PropertiesEditorTable extends DBNEditableTable<PropertiesTableModel> {

    public PropertiesEditorTable(Map<String, String> properties) {
        super(null, new PropertiesTableModel(properties), true);
    }

    public void setProperties(Map<String, String> properties) {
        setModel(new PropertiesTableModel(properties));
    }
}