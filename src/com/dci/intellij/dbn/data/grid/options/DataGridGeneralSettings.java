package com.dci.intellij.dbn.data.grid.options;

import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.grid.options.ui.DataGridGeneralSettingsForm;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class DataGridGeneralSettings extends ProjectConfiguration<DataGridGeneralSettingsForm> {
    private boolean columnTooltipEnabled = true;
    private boolean zoomingEnabled = true;

    public DataGridGeneralSettings(Project project) {
        super(project);
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
        zoomingEnabled = SettingsUtil.getBoolean(element, "enable-zooming", zoomingEnabled);
        columnTooltipEnabled = SettingsUtil.getBoolean(element, "enable-column-tooltip", columnTooltipEnabled);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setBoolean(element, "enable-zooming", zoomingEnabled);
        SettingsUtil.setBoolean(element, "enable-column-tooltip", columnTooltipEnabled);
    }

}
