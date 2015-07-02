package com.dci.intellij.dbn.editor.data.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.options.setting.IntegerSetting;
import com.dci.intellij.dbn.editor.data.options.ui.DataEditorGeneralSettingsForm;

public class DataEditorGeneralSettings extends Configuration<DataEditorGeneralSettingsForm> {
    private IntegerSetting fetchBlockSize = new IntegerSetting("fetch-block-size", 100);
    private IntegerSetting fetchTimeout = new IntegerSetting("fetch-timeout", 30);
    private BooleanSetting trimWhitespaces = new BooleanSetting("trim-whitespaces", true);
    private BooleanSetting convertEmptyStringsToNull = new BooleanSetting("convert-empty-strings-to-null", true);
    private BooleanSetting selectContentOnCellEdit = new BooleanSetting("select-content-on-cell-edit", true);
    private BooleanSetting largeValuePreviewActive = new BooleanSetting("large-value-preview-active", true);
    private BooleanSetting loadViewDataActive = new BooleanSetting("load-view-data-active", false);
    private boolean viewDataLoadEnabled = false;

    public String getDisplayName() {
        return "Data editor general settings";
    }

    public String getHelpTopic() {
        return "dataEditor";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/

    public IntegerSetting getFetchBlockSize() {
        return fetchBlockSize;
    }

    public IntegerSetting getFetchTimeout() {
        return fetchTimeout;
    }

    public BooleanSetting getTrimWhitespaces() {
        return trimWhitespaces;
    }

    public BooleanSetting getConvertEmptyStringsToNull() {
        return convertEmptyStringsToNull;
    }

    public BooleanSetting getSelectContentOnCellEdit() {
        return selectContentOnCellEdit;
    }

    public BooleanSetting getLargeValuePreviewActive() {
        return largeValuePreviewActive;
    }

    public BooleanSetting getLoadViewDataActive() {
        return loadViewDataActive;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @NotNull
    public DataEditorGeneralSettingsForm createConfigurationEditor() {
        return new DataEditorGeneralSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "general";
    }

    public void readConfiguration(Element element) {
        fetchBlockSize.readConfiguration(element);
        fetchTimeout.readConfiguration(element);
        trimWhitespaces.readConfiguration(element);
        convertEmptyStringsToNull.readConfiguration(element);
        selectContentOnCellEdit.readConfiguration(element);
        largeValuePreviewActive.readConfiguration(element);
        loadViewDataActive.readConfiguration(element);
    }

    public void writeConfiguration(Element element) {
        fetchBlockSize.writeConfiguration(element);
        fetchTimeout.writeConfiguration(element);
        trimWhitespaces.writeConfiguration(element);
        convertEmptyStringsToNull.writeConfiguration(element);
        selectContentOnCellEdit.writeConfiguration(element);
        largeValuePreviewActive.writeConfiguration(element);
        loadViewDataActive.writeConfiguration(element);
    }

}
