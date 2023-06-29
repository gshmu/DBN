package com.dci.intellij.dbn.common.ui.misc;

import com.dci.intellij.dbn.common.ui.util.Borderless;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.border.Border;
import java.awt.*;

public class DBNScrollPane extends JBScrollPane {

    public DBNScrollPane() {
    }

    public DBNScrollPane(Component view) {
        super(view);
    }

    @Override
    public void setBorder(Border border) {
        if (getViewport().getView() instanceof Borderless) {
            border = null;
        }
        super.setBorder(border);
    }
}
