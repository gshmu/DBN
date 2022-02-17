package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.color.Colors;
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

    public static final Border TEXT_FIELD_INSETS = JBUI.Borders.empty(0, 3);

    public static final Border COMPONENT_LINE_BORDER = new LineBorder(Colors.COMPONENT_BORDER_COLOR);
    public static final Border BOTTOM_LINE_BORDER = new CustomLineBorder(JBColor.border(),0,0, 1,0);
    public static final Border TOP_LINE_BORDER = new CustomLineBorder(JBColor.border(),1,0, 0,0);

    private static final Map<Color, Border> LINE_BORDERS = new ConcurrentHashMap<>();
    private static final Map<Integer, Border> INSET_BORDERS = new ConcurrentHashMap<>();
    private static final Map<Integer, Border> TOP_INSET_BORDERS = new ConcurrentHashMap<>();

    public static Border lineBorder(Color color) {
        return LINE_BORDERS.computeIfAbsent(color, c -> new LineBorder(c, 1));
    }

    public static Border lineBorder(Color color, int top, int left, int bottom, int right) {
        return new CustomLineBorder(color, top, left, bottom, right);
    }

    public static Border lineBorder(Color color, int thickness) {
        return new LineBorder(color, thickness);
    }

    public static Border insetBorder(int insets) {
        return INSET_BORDERS.computeIfAbsent(insets, i -> new EmptyBorder(JBUI.insets(i)));
    }

    public static Border topInsetBorder(int inset) {
        return TOP_INSET_BORDERS.computeIfAbsent(inset, i -> new EmptyBorder(JBUI.insets(i, 0, 0, 0)));
    }

}
