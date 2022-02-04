package com.dci.intellij.dbn.common.color;

import gnu.trove.TIntObjectHashMap;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

final class ColorAdjustmentCache {
    private ColorAdjustmentCache() {}

    private static final Map<ColorAdjustment, TIntObjectHashMap<TIntObjectHashMap<Color>>> store = new EnumMap<>(ColorAdjustment.class);

    public static Color adjusted(Color color, ColorAdjustment adjustment, int tones) {
        int rgb = color.getRGB();
        TIntObjectHashMap<Color> cache = adjustmentStore(adjustment).get(rgb);
        if (cache == null) {
            cache = new TIntObjectHashMap<>();
            adjustmentStore(adjustment).put(rgb, cache);
        }

        Color adjustedColor = cache.get(tones);
        if (adjustedColor == null) {
            adjustedColor = adjustment.adjust(color, tones);
            cache.put(tones, adjustedColor);
        }
        return adjustedColor;
    }

    private static TIntObjectHashMap<TIntObjectHashMap<Color>> adjustmentStore(ColorAdjustment adj) {
        return store.computeIfAbsent(adj, k -> new TIntObjectHashMap<>());
    }
}
