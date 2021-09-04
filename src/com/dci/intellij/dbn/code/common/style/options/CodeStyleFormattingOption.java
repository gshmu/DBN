package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class CodeStyleFormattingOption implements PersistentConfiguration {
    private final List<CodeStylePreset> presets = new ArrayList<>();
    private String name;
    private String displayName;
    private CodeStylePreset preset;

    public CodeStyleFormattingOption(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public void addPreset(CodeStylePreset preset) {
        presets.add(preset);
    }

    public void addPreset(CodeStylePreset preset, boolean makeDefault) {
        presets.add(preset);
        if (makeDefault) this.preset = preset;
    }

    private CodeStylePreset getCodeStylePreset(String id) {
        for (CodeStylePreset preset : presets) {
            if (preset.getId().equals(id)) return preset;
        }
        return null;
    }

    /*********************************************************
     *                PersistentConfiguration                *
     *********************************************************/
    @Override
    public void readConfiguration(Element element) {
        name = element.getAttributeValue("name");
        String presetId = element.getAttributeValue("value");
        CodeStylePreset newPreset = getCodeStylePreset(presetId);
        if (newPreset != null) preset = newPreset;
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("name", name);
        element.setAttribute("value", preset.getId());
    }
}