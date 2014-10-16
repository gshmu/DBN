package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.Icons;

import javax.swing.Icon;

public enum CompileTypeOption {
    NORMAL("Normal", Icons.OBEJCT_COMPILE),
    DEBUG("Debug", Icons.OBEJCT_COMPILE_DEBUG),
    KEEP("Keep existing", null/*Icons.OBEJCT_COMPILE_KEEP*/),
    ASK("Ask", null/*Icons.OBEJCT_COMPILE_ASK*/);

    private String displayName;
    private Icon icon;

    CompileTypeOption(String displayName, Icon icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    public static CompileTypeOption get(String name) {
        for (CompileTypeOption compileType : CompileTypeOption.values()) {
            if (compileType.getDisplayName().equals(name) || compileType.name().equals(name)) {
                return compileType;
            }
        }
        return null;
    }}
