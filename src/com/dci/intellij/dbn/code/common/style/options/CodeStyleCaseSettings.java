package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.code.common.style.options.ui.CodeStyleCaseSettingsForm;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
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
        enabled = booleanAttribute(element, "enabled", enabled);
        for (Element child : element.getChildren()) {
            String name = stringAttribute(child, "name");
            CodeStyleCaseOption option = getCodeStyleCaseOption(name);
            if (option != null) {
                option.readConfiguration(child);
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
