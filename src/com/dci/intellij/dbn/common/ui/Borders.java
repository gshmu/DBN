package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.Colors;
import com.intellij.ui.JBColor;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.JBUI;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Insets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Borders {
    private Borders() {}

    public static final Insets EMPTY_INSETS = JBUI.emptyInsets();
    public static final Border EMPTY_BORDER = new EmptyBorder(EMPTY_INSETS);
    public static final Border TEXT_FIELD_BORDER = JBUI.Borders.empty(0, 3);
    public static final Border COMPONENT_LINE_BORDER = new LineBorder(Colors.COMPONENT_BORDER_COLOR);
    public static final Border BOTTOM_LINE_BORDER = new CustomLineBorder(JBColor.border(),0,0, 1,0);

    private static final Map<Color, Border> LINE_BORDERS = new ConcurrentHashMap<>();

    public static Border getLineBorder(Color color) {
        return LINE_BORDERS.computeIfAbsent(color, color1 -> new LineBorder(color, 1));
    }
}
