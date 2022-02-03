package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.event.ApplicationEvents;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.ui.LookAndFeel;
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
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.UIManager;
import java.awt.Color;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.dci.intellij.dbn.common.util.Commons.coalesce;

public final class Colors {
    private Colors() {}

    public static Color LIGHT_BLUE = new JBColor(new Color(235, 244, 254), new Color(0x2D3548));
    public static Color BUTTON_BORDER_COLOR = new JBColor(new Color(0x8C8C8C), new Color(0x606060));
    public static Color COMPONENT_BORDER_COLOR = new JBColor(new Color(0x8C8C8C), new Color(0x656565));
    public static Color HINT_COLOR = new JBColor(new Color(-12029286), new Color(-10058060));

    public static Color FAILURE_COLOR = new JBColor(new Color(0xFF0000), new Color(0xBC3F3C));
    public static Color SUCCESS_COLOR = new JBColor(new Color(0x009600), new Color(0x629755));

    public static Color TABLE_HEADER_GRID_COLOR = new JBColor(() -> stronger(UIUtil.getPanelBackground(), 3));
    public static Color TABLE_GRID_COLOR = new JBColor(() -> stronger(UIUtil.getTableBackground(), 3));

    public static Latent<Cache> COLORS = Latent.basic(() -> new Cache());


    @Deprecated
    private static final Map<Color, Map<Double, Color>> oldCache = new ConcurrentHashMap<>();


    private static final TIntObjectHashMap<TIntObjectHashMap<Color>> brighterCache = new TIntObjectHashMap<>();
    private static final TIntObjectHashMap<TIntObjectHashMap<Color>> darkerCache = new TIntObjectHashMap<>();

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


    private static class Cache {
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

        Cache() {
            ApplicationEvents.subscribe(null, EditorColorsManager.TOPIC, scheme -> COLORS.reset());
            UIManager.addPropertyChangeListener(evt -> {
                if (Objects.equals(evt.getPropertyName(), "lookAndFeel")) {
                    COLORS.reset();
                }
            });
        }
    }

    private static Cache get() {
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

    public static Color lighter(Color color, int tones) {
        return LookAndFeel.isDarkMode() ?
                darker(color, tones * 2) :
                brighter(color, tones);
    }

    public static Color stronger(Color color, int tones) {
        return LookAndFeel.isDarkMode() ?
                brighter(color, tones * 2) :
                darker(color, tones);
    }


    private static Color brighter(Color color, int tones) {
        return cached(brighterCache, color, tones,
                c -> hackBrightness(c, tones, 1.03F),
                c -> tuneSaturation(c, tones, 1.03F));
    }

    private static Color darker(Color color, int tones) {
        return cached(darkerCache, color, tones,
                c -> hackBrightness(c, tones, 1 / 1.03F),
                c -> c);
    }

    private static Color cached(
            TIntObjectHashMap<TIntObjectHashMap<Color>> cacheStore,
            Color color,
            int tones,
            Function<Color, Color> adjustment,
            Function<Color, Color> alternativeAdjustment) {

        int rgb = color.getRGB();
        TIntObjectHashMap<Color> cache = cacheStore.get(rgb);
        if (cache == null) {
            cache = new TIntObjectHashMap<>();
            cacheStore.put(rgb, cache);
        }

        Color adjustedColor = cache.get(tones);
        if (adjustedColor == null) {
            adjustedColor = adjustment.apply(color);
            if (adjustedColor.getRGB() == color.getRGB()) {
                adjustedColor = alternativeAdjustment.apply(color);
            }

            cache.put(tones, adjustedColor);
        }
        return adjustedColor;
    }


    /*****************************************************************
     *           Copied over from {@link ColorUtil}
     *****************************************************************/

    private static Color hackBrightness(@NotNull Color color, int howMuch, float hackValue) {
        return tuneHSBComponent(color, 2, howMuch, hackValue);
    }

    private static Color tuneSaturation(@NotNull Color color, int howMuch, float hackValue) {
        return tuneHSBComponent(color, 1, howMuch, hackValue);
    }

    @NotNull
    private static Color tuneHSBComponent(@NotNull Color color, int componentIndex, int howMuch, float factor) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float component = hsb[componentIndex];
        for (int i = 0; i < howMuch; i++) {
            component = Math.min(1, Math.max(factor * component, 0));
            if (component == 0 || component == 1) break;
        }
        hsb[componentIndex] = component;
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }
}
