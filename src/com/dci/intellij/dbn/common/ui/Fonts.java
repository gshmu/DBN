package com.dci.intellij.dbn.common.ui;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.util.ui.UIUtil;

import java.awt.Font;

public final class Fonts {
    public static final Font REGULAR = UIUtil.getLabelFont();
    public static final Font BOLD = new Font(REGULAR.getName(), Font.BOLD, REGULAR.getSize());

    private Fonts() {}

    public static Font getEditorFont() {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        return new Font(scheme.getEditorFontName(), Font.PLAIN, UIUtil.getLabelFont().getSize());
    }
}
