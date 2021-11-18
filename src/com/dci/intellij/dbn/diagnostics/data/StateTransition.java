package com.dci.intellij.dbn.diagnostics.data;

import com.dci.intellij.dbn.common.Colors;
import com.intellij.ui.SimpleTextAttributes;
import lombok.Getter;

import java.awt.Color;

@Getter
public enum StateTransition {
    UNCHANGED(SimpleTextAttributes.GRAY_ATTRIBUTES.getFgColor(), false),

    IMPROVED(Colors.SUCCESS_COLOR, true),
    FIXED(Colors.SUCCESS_COLOR, true),

    DEGRADED(Colors.FAILURE_COLOR, true),
    BROKEN(Colors.FAILURE_COLOR, true);

    private final Color color;
    private final boolean bold;
    private final SimpleTextAttributes textAttributes;

    StateTransition(Color color, boolean bold) {
        this.color = color;
        this.bold = bold;
        textAttributes = new SimpleTextAttributes(bold ?
                SimpleTextAttributes.STYLE_BOLD :
                SimpleTextAttributes.STYLE_PLAIN, color);
    }

    public SimpleTextAttributes getTextAttributes() {
        return textAttributes;
    }
}
