package com.dci.intellij.dbn.data.grid.options;

import java.util.Set;
import java.util.StringTokenizer;
import org.jdom.Element;

import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.grid.options.ui.DataGridTrackingColumnSettingsForm;
import com.intellij.openapi.project.Project;
import gnu.trove.THashSet;

public class DataGridTrackingColumnSettings extends ProjectConfiguration<DataGridTrackingColumnSettingsForm> {
    private Set<String> columnNames = new THashSet<String>();
    private boolean showColumns = true;
    private boolean allowEditing = false;

    public DataGridTrackingColumnSettings(Project project) {
        super(project);
    }

    /****************************************************
     *                      Custom                      *
     ****************************************************/

    public Set<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(Set<String> columnNames) {
        this.columnNames = columnNames;
    }

    public boolean isShowColumns() {
        return showColumns;
    }

    public void setShowColumns(boolean showColumns) {
        this.showColumns = showColumns;
    }

    public boolean isAllowEditing() {
        return allowEditing;
    }

    public void setAllowEditing(boolean allowEditing) {
        this.allowEditing = allowEditing;
    }

    public boolean isTrackingColumn(String columnName) {
        return columnName!= null && columnNames.size() > 0 && columnNames.contains(columnName.toUpperCase());
    }

    public boolean isColumnVisible(String columnName) {
        return showColumns || columnName == null || columnNames.size() == 0 || !columnNames.contains(columnName.toUpperCase());
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

        showColumns = SettingsUtil.getBoolean(element, "visible", showColumns);
        allowEditing = SettingsUtil.getBoolean(element, "editable", allowEditing);
    }

    public void writeConfiguration(Element element) {
        StringBuilder buffer = new StringBuilder();
        for (String columnName : columnNames) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(columnName);
        }
        SettingsUtil.setString(element, "columnNames", buffer.toString());
        SettingsUtil.setBoolean(element, "visible", showColumns);
        SettingsUtil.setBoolean(element, "editable", allowEditing);

    }

}
