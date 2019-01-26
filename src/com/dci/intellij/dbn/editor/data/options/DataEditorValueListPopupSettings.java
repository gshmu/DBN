package com.dci.intellij.dbn.editor.data.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.editor.data.options.ui.DatatEditorValueListPopupSettingsForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class DataEditorValueListPopupSettings extends Configuration<DatatEditorValueListPopupSettingsForm> {
    private boolean showPopupButton = true;
    private int elementCountThreshold = 1000;
    private int dataLengthThreshold = 250;

    @Override
    public String getDisplayName() {
        return "Data editor filters settings";
    }

    @Override
    public String getHelpTopic() {
        return "dataEditor";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/
    public boolean isShowPopupButton() {
        return showPopupButton;
    }

    public void setShowPopupButton(boolean showPopupButton) {
        this.showPopupButton = showPopupButton;
    }

    public int getElementCountThreshold() {
        return elementCountThreshold;
    }

    public void setElementCountThreshold(int elementCountThreshold) {
        this.elementCountThreshold = elementCountThreshold;
    }

    public int getDataLengthThreshold() {
        return dataLengthThreshold;
    }

    public void setDataLengthThreshold(int dataLengthThreshold) {
        this.dataLengthThreshold = dataLengthThreshold;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public DatatEditorValueListPopupSettingsForm createConfigurationEditor() {
        return new DatatEditorValueListPopupSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "values-list-popup";
    }

    @Override
    public void readConfiguration(Element element) {
        showPopupButton = SettingsUtil.getBoolean(element, "show-popup-button", showPopupButton);
        elementCountThreshold = SettingsUtil.getInteger(element, "element-count-threshold", elementCountThreshold);
        dataLengthThreshold = SettingsUtil.getInteger(element, "data-length-threshold", dataLengthThreshold);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setBoolean(element, "show-popup-button", showPopupButton);
        SettingsUtil.setInteger(element, "element-count-threshold", elementCountThreshold);
        SettingsUtil.setInteger(element, "data-length-threshold", dataLengthThreshold);
    }
}
