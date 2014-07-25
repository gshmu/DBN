package com.dci.intellij.dbn.data.grid.options;

import java.util.Set;
import java.util.StringTokenizer;
import org.jdom.Element;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.grid.options.ui.DataGridTrackingColumnSettingsForm;
import gnu.trove.THashSet;

public class DataGridTrackingColumnSettings extends Configuration<DataGridTrackingColumnSettingsForm>{
    private Set<String> columnNames = new THashSet<String>();
    private boolean visible = true;
    private boolean editable = true;

    /****************************************************
     *                      Custom                      *
     ****************************************************/

    public Set<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(Set<String> columnNames) {
        this.columnNames = columnNames;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public DataGridTrackingColumnSettingsForm createConfigurationEditor() {
        return new DataGridTrackingColumnSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "tracking-columns";
    }

    public void readConfiguration(Element element) {
        StringTokenizer columnNames = new StringTokenizer(SettingsUtil.getString(element, "columnNames", ""), ",");
        while (columnNames.hasMoreTokens()) {
            String columnName = columnNames.nextToken().trim().toUpperCase();
            this.columnNames.add(columnName);
        }

        visible = SettingsUtil.getBoolean(element, "visible", visible);
        editable = SettingsUtil.getBoolean(element, "editable", editable);
    }

    public void writeConfiguration(Element element) {
        StringBuilder buffer = new StringBuilder();
        for (String columnName : columnNames) {
            if (buffer.length() == 0) {
                buffer.append(", ");
            }
            buffer.append(columnName);
        }
        SettingsUtil.setString(element, "columnNames", buffer.toString());
        SettingsUtil.setBoolean(element, "visible", visible);
        SettingsUtil.setBoolean(element, "editable", editable);

    }

}
