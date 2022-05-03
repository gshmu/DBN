package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.code.common.style.options.ui.CodeStyleFormattingSettingsForm;
import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public abstract class CodeStyleFormattingSettings extends BasicConfiguration<CodeStyleCustomSettings, CodeStyleFormattingSettingsForm> {
    private final Map<String, CodeStyleFormattingOption> options = new LinkedHashMap<>();
    private boolean enabled = false;

    public CodeStyleFormattingSettings(CodeStyleCustomSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return "Formatting Options";
    }

    protected void addOption(CodeStyleFormattingOption option) {
        options.put(option.getName(), option);
    }

    private CodeStyleFormattingOption getCodeStyleCaseOption(String name) {
        return options.get(name);
    }

    public CodeStyleFormattingOption[] getOptions() {
        return options.values().toArray(new CodeStyleFormattingOption[0]);
    }

    @Nullable
    public CodeStylePreset getPreset(BasePsiElement element) {
        for (CodeStyleFormattingOption option : options.values()) {
            CodeStylePreset preset = option.getPreset();
            if (preset.accepts(element)) {
                return preset;
            }
        }
        return null;
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
        for (CodeStyleFormattingOption option : options.values()) {
            Element optionElement = new Element("option");
            option.writeConfiguration(optionElement);
            element.addContent(optionElement);
        }
    }
}
