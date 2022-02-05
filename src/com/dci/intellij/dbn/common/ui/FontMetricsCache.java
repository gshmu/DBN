package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.latent.Latent;
import org.apache.commons.lang.StringUtils;

import javax.swing.JComponent;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.HashMap;
import java.util.Map;

public class FontMetricsCache {
    private final JComponent component;
    private final Map<Font, int[]> cache = new HashMap<>();

    private final Latent<FontRenderContext> fontRenderContext = Latent.mutable(
            () -> getComponent().getFont(),
            () -> getComponent().getFontMetrics(getComponent().getFont()).getFontRenderContext());

    public FontMetricsCache(JComponent component) {
        this.component = component;
    }

    public JComponent getComponent() {
        return component;
    }

    public int getTextWidth(String displayValue) {
        int length = displayValue.length();
        if (length == 0) {
            return 0;
        }

        Font font = component.getFont();
        length = Math.min(100, length);

        int[] widths = cache.computeIfAbsent(font, k -> new int[100]);
        int index = length - 1;
        if (widths[index] == 0) {
            String mock = StringUtils.leftPad("", length, "O");
            widths[index] = (int) font.getStringBounds(mock, fontRenderContext.get()).getWidth();
        }

        return widths[index];
    }

}
