package com.dci.intellij.dbn.common.ui;

import com.intellij.util.ui.UIUtil;

import javax.swing.UIManager;
import java.util.Objects;

public final class LookAndFeel {
    private LookAndFeel() {}

    static boolean darkMode = UIUtil.isUnderDarcula();

    public static boolean isDarkMode() {
        return darkMode;
    }

    static {
        UIManager.addPropertyChangeListener(evt -> {
            if (Objects.equals(evt.getPropertyName(), "lookAndFeel")) {
                LookAndFeel.darkMode = UIUtil.isUnderDarcula();
            }


        });
    }


}
