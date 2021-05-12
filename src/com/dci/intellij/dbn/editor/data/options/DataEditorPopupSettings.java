package com.dci.intellij.dbn.editor.data.options;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.editor.data.options.ui.DataEditorPopupSettingsForm;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class DataEditorPopupSettings extends BasicConfiguration<DataEditorSettings, DataEditorPopupSettingsForm> {
    private boolean active = false;
    private boolean activeIfEmpty = false;
    private int dataLengthThreshold = 100;
    private int delay = 1000;

    public DataEditorPopupSettings(DataEditorSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
   @Override
   @NotNull
   public DataEditorPopupSettingsForm createConfigurationEditor() {
       return new DataEditorPopupSettingsForm(this);
   }

    @Override
    public String getConfigElementName() {
        return "text-editor-popup";
    }

    @Override
    public void readConfiguration(Element element) {
        active = SettingsSupport.getBoolean(element, "active", active);
        activeIfEmpty = SettingsSupport.getBoolean(element, "active-if-empty", activeIfEmpty);
        dataLengthThreshold = SettingsSupport.getInteger(element, "data-length-threshold", dataLengthThreshold);
        delay = SettingsSupport.getInteger(element, "popup-delay", delay);
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setBoolean(element, "active", active);
        SettingsSupport.setBoolean(element, "active-if-empty", activeIfEmpty);
        SettingsSupport.setInteger(element, "data-length-threshold", dataLengthThreshold);
        SettingsSupport.setInteger(element, "popup-delay", delay);
    }

}
