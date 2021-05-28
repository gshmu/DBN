package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.code.common.style.options.ui.CodeStyleCaseSettingsForm;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBooleanAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBooleanAttribute;

public abstract class CodeStyleCaseSettings extends BasicConfiguration<CodeStyleCustomSettings, CodeStyleCaseSettingsForm> {
    private final List<CodeStyleCaseOption> options = new ArrayList<>();
    private boolean enabled = true;

    public CodeStyleCaseSettings(CodeStyleCustomSettings parent) {
        super(parent);
        options.add(new CodeStyleCaseOption("KEYWORD_CASE", CodeStyleCase.LOWER, false));
        options.add(new CodeStyleCaseOption("FUNCTION_CASE", CodeStyleCase.LOWER, false));
        options.add(new CodeStyleCaseOption("PARAMETER_CASE", CodeStyleCase.LOWER, false));
        options.add(new CodeStyleCaseOption("DATATYPE_CASE", CodeStyleCase.LOWER, false));
        options.add(new CodeStyleCaseOption("OBJECT_CASE", CodeStyleCase.PRESERVE, true));
    }


    @Override
    public String getDisplayName() {
        return "Case Options";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CodeStyleCaseOption getKeywordCaseOption() {
        return getCodeStyleCaseOption("KEYWORD_CASE");
    }

    public CodeStyleCaseOption getFunctionCaseOption() {
        return getCodeStyleCaseOption("FUNCTION_CASE");
    }

    public CodeStyleCaseOption getParameterCaseOption() {
        return getCodeStyleCaseOption("PARAMETER_CASE");
    }

    public CodeStyleCaseOption getDatatypeCaseOption() {
        return getCodeStyleCaseOption("DATATYPE_CASE");
    }


    public CodeStyleCaseOption getObjectCaseOption() {
        return getCodeStyleCaseOption("OBJECT_CASE");
    }

    private CodeStyleCaseOption getCodeStyleCaseOption(String name) {
        for (CodeStyleCaseOption option : options) {
            if (option.getName().equals(name)) return option;
        }
        return null;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    @NotNull
    public CodeStyleCaseSettingsForm createConfigurationEditor() {
        return new CodeStyleCaseSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "case-options";
    }

    @Override
    public void readConfiguration(Element element) {
        enabled = getBooleanAttribute(element, "enabled", enabled);
        for (Object object : element.getChildren()) {
            Element optionElement = (Element) object;
            String name = optionElement.getAttributeValue("name");
            CodeStyleCaseOption option = getCodeStyleCaseOption(name);
            if (option != null) {
                option.readConfiguration(optionElement);
            }
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        setBooleanAttribute(element, "enabled", enabled);
        for (CodeStyleCaseOption option : options) {
            Element optionElement = new Element("option");
            option.writeConfiguration(optionElement);
            element.addContent(optionElement);
        }
    }
}
