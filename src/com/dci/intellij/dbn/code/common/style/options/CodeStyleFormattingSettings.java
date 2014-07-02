package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.code.common.style.options.ui.CodeStyleFormattingSettingsForm;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class CodeStyleFormattingSettings extends Configuration<CodeStyleFormattingSettingsForm> {
    private List<CodeStyleFormattingOption> options = new ArrayList<CodeStyleFormattingOption>();
    private boolean enabled = false;

    public String getDisplayName() {
        return "Formatting Options";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected void addOption(CodeStyleFormattingOption option) {
        options.add(option);
    }

    private CodeStyleFormattingOption getCodeStyleCaseOption(String name) {
        for (CodeStyleFormattingOption option : options) {
            if (option.getName().equals(name)) return option;
        }
        return null;
    }

    public List<CodeStyleFormattingOption> getOptions() {
        return options;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public CodeStyleFormattingSettingsForm createConfigurationEditor() {
        return new CodeStyleFormattingSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "formatting-settings";
    }

    public void readConfiguration(Element element) throws InvalidDataException {
        enabled = SettingsUtil.getBooleanAttribute(element, "enabled", enabled);
        for (Object object : element.getChildren()) {
            Element optionElement = (Element) object;
            String name = optionElement.getAttributeValue("name");
            CodeStyleFormattingOption option = getCodeStyleCaseOption(name);
            if (option != null) {
                option.readExternal(optionElement);
            }
        }
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        SettingsUtil.setBooleanAttribute(element, "enabled", enabled);
        for (CodeStyleFormattingOption option : options) {
            Element optionElement = new Element("option");
            option.writeExternal(optionElement);
            element.addContent(optionElement);
        }
    }
}
