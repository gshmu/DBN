package com.dci.intellij.dbn.editor.data.options;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.editor.data.options.ui.DatatEditorValueListPopupSettingsForm;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class DataEditorValueListPopupSettings extends BasicConfiguration<DataEditorSettings, DatatEditorValueListPopupSettingsForm> {
    private boolean showPopupButton = true;
    private int elementCountThreshold = 1000;
    private int dataLengthThreshold = 250;

    DataEditorValueListPopupSettings(DataEditorSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Data editor filters settings";
    }

    @Override
    public String getHelpTopic() {
        return "dataEditor";
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
        return "values-actions-popup";
    }

    @Override
    public void readConfiguration(Element element) {
        showPopupButton = SettingsSupport.getBoolean(element, "show-popup-button", showPopupButton);
        elementCountThreshold = SettingsSupport.getInteger(element, "element-count-threshold", elementCountThreshold);
        dataLengthThreshold = SettingsSupport.getInteger(element, "data-length-threshold", dataLengthThreshold);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setBoolean(element, "show-popup-button", showPopupButton);
        SettingsSupport.setInteger(element, "element-count-threshold", elementCountThreshold);
        SettingsSupport.setInteger(element, "data-length-threshold", dataLengthThreshold);
    }
}
