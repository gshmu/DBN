package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.code.common.style.options.ui.CodeStyleFormattingSettingsForm;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public abstract class CodeStyleFormattingSettings extends BasicConfiguration<CodeStyleCustomSettings, CodeStyleFormattingSettingsForm> {
    private final List<CodeStyleFormattingOption> options = new ArrayList<>();
    private boolean enabled = false;

    public CodeStyleFormattingSettings(CodeStyleCustomSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Formatting Options";
    }

    protected void addOption(CodeStyleFormattingOption option) {
        options.add(option);
    }

    private CodeStyleFormattingOption getCodeStyleCaseOption(String name) {
        return options.
                stream().
                filter(option -> Objects.equals(option.getName(), name)).
                findFirst().
                orElse(null);
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    @NotNull
    public CodeStyleFormattingSettingsForm createConfigurationEditor() {
        return new CodeStyleFormattingSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "formatting-settings";
    }

    @Override
    public void readConfiguration(Element element) {
        enabled = booleanAttribute(element, "enabled", enabled);
        for (Element child : element.getChildren()) {
            String name = stringAttribute(child, "name");
            CodeStyleFormattingOption option = getCodeStyleCaseOption(name);
            if (option != null) {
                option.readConfiguration(child);
            }
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        setBooleanAttribute(element, "enabled", enabled);
        for (CodeStyleFormattingOption option : options) {
            Element optionElement = new Element("option");
            option.writeConfiguration(optionElement);
            element.addContent(optionElement);
        }
    }
}
