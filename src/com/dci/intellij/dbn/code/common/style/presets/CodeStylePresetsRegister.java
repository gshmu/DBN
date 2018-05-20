package com.dci.intellij.dbn.code.common.style.presets;

import java.util.HashMap;
import java.util.Map;

public class CodeStylePresetsRegister {
    private static Map<String, CodeStylePreset> wrapPresets = new HashMap<String, CodeStylePreset>();

    public static void registerWrapPreset(CodeStylePreset codeStylePreset) {
        wrapPresets.put(codeStylePreset.getId(), codeStylePreset);
    }

    public static CodeStylePreset getWrapPreset(String id) {
        return wrapPresets.get(id);
    }
}
