package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.event.ApplicationEvents;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.data.grid.color.BasicTableTextAttributes;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.UIManager;
import java.awt.Color;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.util.Commons.coalesce;
import static java.lang.Math.*;

public final class Colors {
    private Colors() {}
    private static final Map<Color, Map<Double, Color>> cache = new ConcurrentHashMap<>();

    public static Color LIGHT_BLUE = new JBColor(new Color(235, 244, 254), new Color(0x2D3548));
    public static Color BUTTON_BORDER_COLOR = new JBColor(new Color(0x8C8C8C), new Color(0x606060));
    public static Color COMPONENT_BORDER_COLOR = new JBColor(new Color(0x8C8C8C), new Color(0x656565));
    public static Color HINT_COLOR = new JBColor(new Color(-12029286), new Color(-10058060));

    public static Color FAILURE_COLOR = new JBColor(new Color(0xFF0000), new Color(0xBC3F3C));
    public static Color SUCCESS_COLOR = new JBColor(new Color(0x009600), new Color(0x629755));


    public static Latent<ColorsImpl> COLORS = Latent.basic(() -> new ColorsImpl());

    public static Color tableHeaderBorderColor() {
        return get().tableHeaderBorderColor;
    }

    public static Color tableGridColor() {
        return get().tableGridColor;
    }

    public static Color tableCaretRowColor() {
        return get().tableCaretRowColor;
    }

    public static Color tableSelectionBackgroundColor(boolean focused) {
        return focused ?
                get().tableSelectionBackgroundColorFocused :
                get().tableSelectionBackgroundColor;
    }

    public static Color tableSelectionForegroundColor(boolean focused) {
        return focused ?
                get().tableSelectionForegroundColorFocused :
                get().tableSelectionForegroundColor;
    }

    public static Color tableLineNumberColor() {
        return get().tableLineNumberColor;
    }


    private static class ColorsImpl {
        private final Color tableHeaderBorderColor = stronger(UIUtil.getPanelBackground(), 1);
        private final Color tableGridColor = stronger(UIUtil.getTableBackground(), 1);

        private final Color tableCaretRowColor = coalesce(
                getGlobalScheme().getAttributes(DataGridTextAttributesKeys.CARET_ROW).getBackgroundColor(),
                getGlobalScheme().getColor(EditorColors.CARET_ROW_COLOR));

        private final Color tableSelectionBackgroundColor = coalesce(
                () -> BasicTableTextAttributes.get().getSelection().getBgColor(),
                () -> getGlobalScheme().getColor(EditorColors.SELECTION_BACKGROUND_COLOR),
                () -> UIUtil.getTableSelectionBackground(false));

        private final Color tableSelectionBackgroundColorFocused = coalesce(
                () -> BasicTableTextAttributes.get().getSelection().getBgColor(),
                () -> getGlobalScheme().getColor(EditorColors.SELECTION_BACKGROUND_COLOR),
                () -> UIUtil.getTableSelectionBackground(true));

        private final Color tableSelectionForegroundColor = coalesce(
                () -> BasicTableTextAttributes.get().getSelection().getFgColor(),
                () -> getGlobalScheme().getColor(EditorColors.SELECTION_FOREGROUND_COLOR),
                () -> UIUtil.getTableSelectionForeground(false));

        private final Color tableSelectionForegroundColorFocused = coalesce(
                () -> BasicTableTextAttributes.get().getSelection().getFgColor(),
                () -> getGlobalScheme().getColor(EditorColors.SELECTION_FOREGROUND_COLOR),
                () -> UIUtil.getTableSelectionForeground(true));


        private final Color tableLineNumberColor = getGlobalScheme().getColor(EditorColors.LINE_NUMBERS_COLOR);

        ColorsImpl() {
            ApplicationEvents.subscribe(null, EditorColorsManager.TOPIC, scheme -> COLORS.reset());

            UIManager.addPropertyChangeListener(evt -> {
                if (Objects.equals(evt.getPropertyName(), "lookAndFeel")) {
                    COLORS.reset();
                }
            });
        }
    }

    private static ColorsImpl get() {
        return COLORS.get();
    }

    @NotNull
    public static EditorColorsScheme getGlobalScheme() {
        return EditorColorsManager.getInstance().getGlobalScheme();
    }

    public static void subscribe(@Nullable Disposable parentDisposable,  Runnable runnable) {
        ApplicationEvents.subscribe(parentDisposable, EditorColorsManager.TOPIC, scheme -> runnable.run());

        UIManager.addPropertyChangeListener(evt -> {
            if (Objects.equals(evt.getPropertyName(), "lookAndFeel")) {
                Safe.run(runnable);
            }
        });
    }

    public static Color adjust(Color color, double factor) {
        Map<Double, Color> cache = Colors.cache.computeIfAbsent(color, k -> new ConcurrentHashMap<>());
        return cache.computeIfAbsent(factor, k ->
                GUIUtil.isDarkLookAndFeel() ?
                        adjustRaw(color, -k) :
                        adjustRaw(color, k));
    }


    public static Color softer(Color color, int tones) {
        return GUIUtil.isDarkLookAndFeel() ?
                ColorUtil.darker(color, tones) :
                ColorUtil.brighter(color, tones);
    }

    public static Color stronger(Color color, int tones) {
        return GUIUtil.isDarkLookAndFeel() ?
                ColorUtil.brighter(color, tones) :
                ColorUtil.darker(color, tones);
    }



    @NotNull
    private static Color adjustRaw(Color color, double factor) {
        int red   = color.getRed();
        int green = color.getGreen();
        int blue  = color.getBlue();

        red   = (int) round(min(255, red   + red   * factor));
        green = (int) round(min(255, green + green * factor));
        blue  = (int) round(min(255, blue  + blue  * factor));

        red   = max(min(255, red), 0);
        green = max(min(255, green), 0);
        blue  = max(min(255, blue), 0);

        int alpha = color.getAlpha();

        return new Color(red, green, blue, alpha);
    }
}
