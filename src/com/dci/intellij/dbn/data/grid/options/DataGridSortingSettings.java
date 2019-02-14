package com.dci.intellij.dbn.data.grid.options;

import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.data.grid.options.ui.DataGridSortingSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class DataGridSortingSettings extends BasicProjectConfiguration<DataGridSettings, DataGridSortingSettingsForm> {
    private boolean nullsFirst = true;
    private int maxSortingColumns = 4;

    DataGridSortingSettings(DataGridSettings parent) {
        super(parent);
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

    public int getMaxSortingColumns() {
        return maxSortingColumns;
    }

    public void setMaxSortingColumns(int maxSortingColumns) {
        this.maxSortingColumns = maxSortingColumns;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public DataGridSortingSettingsForm createConfigurationEditor() {
        return new DataGridSortingSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "sorting";
    }

    @Override
    public void readConfiguration(Element element) {
        nullsFirst = SettingsSupport.getBoolean(element, "nulls-first", nullsFirst);
        maxSortingColumns = SettingsSupport.getInteger(element, "max-sorting-columns", maxSortingColumns);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setBoolean(element, "nulls-first", nullsFirst);
        SettingsSupport.setInteger(element, "max-sorting-columns", maxSortingColumns);
    }

}
