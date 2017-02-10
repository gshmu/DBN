package com.dci.intellij.dbn.common.ui;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Insets;

import com.dci.intellij.dbn.common.Colors;
import com.intellij.ui.JBColor;
import com.intellij.ui.border.CustomLineBorder;

public interface Borders {
    Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
    Border EMPTY_BORDER = new EmptyBorder(EMPTY_INSETS);
    Border TEXT_FIELD_BORDER = new EmptyBorder(0, 3, 0, 3);
    Border COMPONENT_LINE_BORDER = new LineBorder(Colors.COMPONENT_BORDER_COLOR);
    Border BOTTOM_LINE_BORDER = new CustomLineBorder(JBColor.border(),0,0, 1,0);
}
