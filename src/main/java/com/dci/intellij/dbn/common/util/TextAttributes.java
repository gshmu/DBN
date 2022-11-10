package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.ui.SimpleTextAttributes;

public class TextAttributes {
    private TextAttributes() {}


    public static SimpleTextAttributes getSimpleTextAttributes(TextAttributesKey textAttributesKey) {
        EditorColorsManager colorManager = EditorColorsManager.getInstance();
        com.intellij.openapi.editor.markup.TextAttributes textAttributes = colorManager.getGlobalScheme().getAttributes(textAttributesKey);
        if (textAttributes == null) {
            textAttributes = HighlighterColors.TEXT.getDefaultAttributes();
        }
        return new SimpleTextAttributes(
                textAttributes.getBackgroundColor(),
                textAttributes.getForegroundColor(),
                textAttributes.getEffectColor(),
                textAttributes.getFontType());
    }
}
