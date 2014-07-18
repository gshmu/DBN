package com.dci.intellij.dbn.code.common.style.options;

import java.util.List;
import java.util.ArrayList;
import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public class CodeStyleFormattingOption {
    private List<CodeStylePreset> presets = new ArrayList<CodeStylePreset>();
    private CodeStylePreset preset;
    private String name;
    private String displayName;

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


    public void setPreset(CodeStylePreset preset) {
        this.preset = preset;
    }

    public CodeStylePreset getPreset() {
        return preset;
    }

    public List<CodeStylePreset> getPresets() {
        return presets;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    private CodeStylePreset getCodeStylePreset(String id) {
        for (CodeStylePreset preset : presets) {
            if (preset.getId().equals(id)) return preset;
        }
        return null;
    }

    /*********************************************************
     *                   JDOMExternalizable                  *
     *********************************************************/
    public void readExternal(Element element) {
        name = element.getAttributeValue("name");
        String presetId = element.getAttributeValue("value");
        CodeStylePreset newPreset = getCodeStylePreset(presetId);
        if (newPreset != null) preset = newPreset;
    }

    public void writeExternal(Element element) {
        element.setAttribute("name", name);
        element.setAttribute("value", preset.getId());
    }
}