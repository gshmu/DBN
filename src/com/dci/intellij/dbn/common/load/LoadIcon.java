package com.dci.intellij.dbn.common.load;

import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class LoadIcon implements Icon{
    public static final Icon INSTANCE = new LoadIcon();
    private static Icon[] icons = new Icon[12];
    static {
        for (int i = 0; i <= 12 - 1; i++) {
            icons[i] = IconLoader.getIcon("/process/step_" + (i + 1) + ".png");
        }
    }


    private static int iconIndex;
    private static long lastAccessTimestamp = System.currentTimeMillis();

    private static Timer ICON_ROLLER;
    private static class IconRollerTimerTask extends TimerTask {
        @Override
        public void run() {
            if (iconIndex == icons.length - 1) {
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
                    ICON_ROLLER.schedule(new IconRollerTimerTask(), 50, 50);
                }
            }
        }
    }

    private static Icon getIcon() {
        startRoller();
        lastAccessTimestamp = System.currentTimeMillis();
        return icons[iconIndex];
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        getIcon().paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return getIcon().getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return getIcon().getIconHeight();
    }
}
