package com.dci.intellij.dbn.common;

import com.intellij.icons.AllIcons;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBRectangle;
import lombok.experimental.UtilityClass;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class CompoundIcons {

    private static final Map<Icon, Icon> modifiedOverlayIcons = new ConcurrentHashMap<>();
    private static final JBRectangle MODIFIED_OVERLAY_AREA = new JBRectangle(3, 3, 7, 7);

    public static Icon addModifiedOverlay(Icon icon) {
        return modifiedOverlayIcons.computeIfAbsent(icon, i -> {
            Icon modifiedIcon = IconUtil.cropIcon(AllIcons.General.Modified, MODIFIED_OVERLAY_AREA);
            LayeredIcon layeredIcon = new LayeredIcon(2);

            layeredIcon.setIcon(i, 0);
            layeredIcon.setIcon(modifiedIcon, 1, -(modifiedIcon.getIconWidth() / 2) - 2, -2);

            return JBUIScale.scaleIcon(layeredIcon);
        });
    }
}
