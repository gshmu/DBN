package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.RuntimeLatent;

import javax.swing.*;
import java.awt.*;

public abstract class DBNTableHeaderRendererBase implements DBNTableHeaderRenderer {

    private RuntimeLatent<FontMetrics> fontMetrics = Latent.runtime(() -> {
        JLabel nameLabel = getNameLabel();
        return nameLabel.getFontMetrics(nameLabel.getFont());
    });

    protected abstract JLabel getNameLabel();

    @Override
    public final void setFont(Font font) {
        getNameLabel().setFont(font);
        fontMetrics.reset();
    }

    public FontMetrics getFontMetrics() {
        return fontMetrics.get();
    }
}

