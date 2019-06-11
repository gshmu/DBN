package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public interface Colors {
    Color LIGHT_BLUE = new JBColor(new Color(235, 244, 254), new Color(0x2D3548));
    Color BUTTON_BORDER_COLOR = new JBColor(new Color(0x8C8C8C), new Color(0x606060));
    Color COMPONENT_BORDER_COLOR = new JBColor(new Color(0x8C8C8C), new Color(0x656565));
    Color HINT_COLOR = new JBColor(new Color(-12029286), new Color(-10058060));

    static Color tableHeaderBorderColor() {
        return adjust(UIUtil.getPanelBackground(), -0.05);
    }


    static Color adjust(Color color, double shift) {
        if (GUIUtil.isDarkLookAndFeel()) {
            shift = -shift;
        }
        return adjustRaw(color, shift);

    }

    @NotNull
    static Color adjustRaw(Color color, double shift) {
        int red = (int) Math.round(Math.min(255, color.getRed() + 255 * shift));
        int green = (int) Math.round(Math.min(255, color.getGreen() + 255 * shift));
        int blue = (int) Math.round(Math.min(255, color.getBlue() + 255 * shift));

        red = Math.max(Math.min(255, red), 0);
        green = Math.max(Math.min(255, green), 0);
        blue = Math.max(Math.min(255, blue), 0);

        int alpha = color.getAlpha();

        return new Color(red, green, blue, alpha);
    }
}
