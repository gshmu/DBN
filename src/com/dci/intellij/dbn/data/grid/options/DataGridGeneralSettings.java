package com.dci.intellij.dbn.data.grid.options;

import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.data.grid.options.ui.DataGridGeneralSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class DataGridGeneralSettings extends BasicProjectConfiguration<DataGridSettings, DataGridGeneralSettingsForm> {
    private boolean columnTooltipEnabled = true;
    private boolean zoomingEnabled = true;

    public DataGridGeneralSettings(DataGridSettings parent) {
        super(parent);
    }

    /****************************************************
     *                      Custom                      *
     ****************************************************/

    public boolean isColumnTooltipEnabled() {
        return columnTooltipEnabled;
    }

    public void setColumnTooltipEnabled(boolean columnTooltipEnabled) {
        this.columnTooltipEnabled = columnTooltipEnabled;
    }

    public boolean isZoomingEnabled() {
        return zoomingEnabled;
    }

    public void setZoomingEnabled(boolean zoomingEnabled) {
        this.zoomingEnabled = zoomingEnabled;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public DataGridGeneralSettingsForm createConfigurationEditor() {
        return new DataGridGeneralSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "general";
    }

    @Override
    public void readConfiguration(Element element) {
        zoomingEnabled = SettingsSupport.getBoolean(element, "enable-zooming", zoomingEnabled);
        columnTooltipEnabled = SettingsSupport.getBoolean(element, "enable-column-tooltip", columnTooltipEnabled);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setBoolean(element, "enable-zooming", zoomingEnabled);
        SettingsSupport.setBoolean(element, "enable-column-tooltip", columnTooltipEnabled);
    }

}
