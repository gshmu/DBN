package com.dci.intellij.dbn.editor.data.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterType;
import com.dci.intellij.dbn.editor.data.options.ui.DataEditorFilterSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class DataEditorFilterSettings extends Configuration<DataEditorFilterSettingsForm> {
    private boolean promptFilterDialog = true;
    private DatasetFilterType defaultFilterType = DatasetFilterType.BASIC;

    @Override
    public String getDisplayName() {
        return "Data editor filters settings";
    }

    @Override
    public String getHelpTopic() {
        return "dataEditor";
    }

    /*********************************************************
     *                       Custom                          *
     *********************************************************/

    public boolean isPromptFilterDialog() {
        return promptFilterDialog;
    }

    public void setPromptFilterDialog(boolean promptFilterDialog) {
        this.promptFilterDialog = promptFilterDialog;
    }

    public DatasetFilterType getDefaultFilterType() {
        return defaultFilterType;
    }

    public void setDefaultFilterType(DatasetFilterType defaultFilterType) {
        this.defaultFilterType = defaultFilterType;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public DataEditorFilterSettingsForm createConfigurationEditor() {
        return new DataEditorFilterSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "filters";
    }

    @Override
    public void readConfiguration(Element element) {
        promptFilterDialog = SettingsUtil.getBoolean(element, "prompt-filter-dialog", promptFilterDialog);
        defaultFilterType = DatasetFilterType.get(SettingsUtil.getString(element, "default-filter-type", defaultFilterType.name()));
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setBoolean(element, "prompt-filter-dialog", promptFilterDialog);
        SettingsUtil.setString(element, "default-filter-type", defaultFilterType.name());
    }
}
