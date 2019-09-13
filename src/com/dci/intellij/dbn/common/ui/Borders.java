package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.latent.MapLatent;
import com.intellij.ui.JBColor;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.JBUI;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public interface Borders {
    Insets EMPTY_INSETS = JBUI.emptyInsets();
    Border EMPTY_BORDER = new EmptyBorder(EMPTY_INSETS);
    Border TEXT_FIELD_BORDER = JBUI.Borders.empty(0, 3);
    Border COMPONENT_LINE_BORDER = new LineBorder(Colors.COMPONENT_BORDER_COLOR);
    Border BOTTOM_LINE_BORDER = new CustomLineBorder(JBColor.border(),0,0, 1,0);

    MapLatent<Color, Border, RuntimeException> LINE_BORDERS = MapLatent.create(color -> new LineBorder(color, 1));

    static Border getLineBorder(Color color) {
        return LINE_BORDERS.get(color);
    }
}
