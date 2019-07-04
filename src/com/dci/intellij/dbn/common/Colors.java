package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.data.grid.color.BasicTableTextAttributes;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.JBColor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static com.dci.intellij.dbn.common.util.CommonUtil.nvlf;

public interface Colors {
    Color LIGHT_BLUE = new JBColor(new Color(235, 244, 254), new Color(0x2D3548));
    Color BUTTON_BORDER_COLOR = new JBColor(new Color(0x8C8C8C), new Color(0x606060));
    Color COMPONENT_BORDER_COLOR = new JBColor(new Color(0x8C8C8C), new Color(0x656565));
    Color HINT_COLOR = new JBColor(new Color(-12029286), new Color(-10058060));

    Latent<ColorsImpl> COLORS = Latent.basic(() -> new ColorsImpl());

    static Color tableHeaderBorderColor() {
        return get().tableHeaderBorderColor;
    }

    static Color tableGridColor() {
        return get().tableGridColor;
    }

    static Color tableCaretRowColor() {
        return get().tableCaretRowColor;
    }

    static Color tableSelectionBackgroundColor() {
        return get().tableSelectionBackgroundColor;
    }

    static Color tableSelectionForegroundColor() {
        return get().tableSelectionForegroundColor;
    }

    static Color tableLineNumberColor() {
        return get().tableLineNumberColor;
    }


    class ColorsImpl implements Colors {
        private Color tableHeaderBorderColor = adjust(UIUtil.getPanelBackground(), -0.07);
        private Color tableGridColor = adjust(UIUtil.getTableBackground(), -0.09);

        private Color tableCaretRowColor = nvlf(
                getGlobalScheme().getAttributes(DataGridTextAttributesKeys.CARET_ROW).getBackgroundColor(),
                getGlobalScheme().getColor(EditorColors.CARET_ROW_COLOR));

        private Color tableSelectionBackgroundColor = nvlf(
                BasicTableTextAttributes.get().getSelection().getBgColor(),
                getGlobalScheme().getColor(EditorColors.SELECTION_BACKGROUND_COLOR),
                UIUtil.getTableSelectionBackground());

        private Color tableSelectionForegroundColor = nvlf(
                BasicTableTextAttributes.get().getSelection().getFgColor(),
                getGlobalScheme().getColor(EditorColors.SELECTION_FOREGROUND_COLOR),
                UIUtil.getTableSelectionForeground());


        private Color tableLineNumberColor = getGlobalScheme().getColor(EditorColors.LINE_NUMBERS_COLOR);

        ColorsImpl() {
            MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect();
            connection.subscribe(EditorColorsManager.TOPIC, scheme -> COLORS.reset());

            UIManager.addPropertyChangeListener(evt -> {
                if ("lookAndFeel".equals(evt.getPropertyName())) {
                    COLORS.reset();
                }
            });
        }
    }

    static ColorsImpl get() {
        return COLORS.get();
    }

    @NotNull
    static EditorColorsScheme getGlobalScheme() {
        return EditorColorsManager.getInstance().getGlobalScheme();
    }

    static void subscribe(Runnable runnable) {
        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect();
        connection.subscribe(EditorColorsManager.TOPIC, scheme -> runnable.run());

        UIManager.addPropertyChangeListener(evt -> {
            if ("lookAndFeel".equals(evt.getPropertyName())) {
                runnable.run();
            }
        });
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
