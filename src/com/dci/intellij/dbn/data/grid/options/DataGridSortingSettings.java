package com.dci.intellij.dbn.data.grid.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.grid.options.ui.DataGridSortingSettingsForm;
import com.intellij.openapi.project.Project;

public class DataGridSortingSettings extends ProjectConfiguration<DataGridSortingSettingsForm> {
    private boolean nullsFirst = true;
    private int columnSortingLimit = 4;

    public DataGridSortingSettings(Project project) {
        super(project);
    }

    /****************************************************
     *                      Custom                      *
     ****************************************************/
    public void setNullsFirst(boolean nullsFirst) {
        this.nullsFirst = nullsFirst;
    }

    public boolean isNullsFirst() {
        return nullsFirst;
    }

    public int getColumnSortingLimit() {
        return columnSortingLimit;
    }

    public void setColumnSortingLimit(int columnSortingLimit) {
        this.columnSortingLimit = columnSortingLimit;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public DataGridSortingSettingsForm createConfigurationEditor() {
        return new DataGridSortingSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "sorting";
    }

    public void readConfiguration(Element element) {
        nullsFirst = SettingsUtil.getBoolean(element, "nulls-first", nullsFirst);
        columnSortingLimit = SettingsUtil.getInteger(element, "column-sorting-limit", columnSortingLimit);
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setBoolean(element, "nulls-first", nullsFirst);
        SettingsUtil.setInteger(element, "column-sorting-limit", columnSortingLimit);
    }

}
