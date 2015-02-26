package com.dci.intellij.dbn.data.grid.color;

import java.awt.Color;

import com.intellij.ui.SimpleTextAttributes;

/**
 * Created by CiocaDa on 26.02.2015.
 */
public interface DataGridTextAttributes {
    SimpleTextAttributes getSelection();

    SimpleTextAttributes getSearchResult();

    Color getCaretRowBgColor();
}
