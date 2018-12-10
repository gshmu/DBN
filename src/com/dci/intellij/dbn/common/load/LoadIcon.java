package com.dci.intellij.dbn.common.load;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;

import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.AnimatedIcon;

public class LoadIcon implements Icon{

    public static final Icon INSTANCE = new AnimatedIcon.Default();
    public static final int ROLL_INTERVAL = AnimatedIcon.Default.DELAY;
    public static final int ROLL_ELEMENTS = 12;

    private static Icon[] ICONS = new Icon[ROLL_ELEMENTS];
    static {
        for (int i = 0; i < ICONS.length; i++) {
            ICONS[i] = IconLoader.getIcon("/process/step_" + (i + 1) + ".png");
        }
    }

    private static int iconIndex;
    private static long lastAccessTimestamp = System.currentTimeMillis();

    private static Timer ICON_ROLLER;
    private static class IconRollerTimerTask extends TimerTask {
        @Override
        public void run() {
            if (iconIndex == ICONS.length - 1) {
                iconIndex = 0;
            } else {
                iconIndex++;
            }

            if (ICON_ROLLER != null && TimeUtil.isOlderThan(lastAccessTimestamp, TimeUtil.TEN_SECONDS)) {
                synchronized (IconRollerTimerTask.class) {
                    Timer cachedIconRoller = ICON_ROLLER;
                    ICON_ROLLER = null;
                    cachedIconRoller.purge();
                    cachedIconRoller.cancel();
                }
            }
        }
    };

    private static void startRoller() {
        if (ICON_ROLLER == null) {
            synchronized (IconRollerTimerTask.class) {
                if (ICON_ROLLER == null) {
                    ICON_ROLLER = new Timer("DBN - Load in Progress (icon roller)");
                    ICON_ROLLER.schedule(new IconRollerTimerTask(), ROLL_INTERVAL, ROLL_INTERVAL);
                }
            }
        }
    }

    private static Icon getIcon() {
        startRoller();
        lastAccessTimestamp = System.currentTimeMillis();
        return ICONS[iconIndex];
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        getIcon().paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return ICONS[0].getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return ICONS[0].getIconHeight();
    }
}
