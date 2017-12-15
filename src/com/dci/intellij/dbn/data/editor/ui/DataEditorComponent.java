package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public interface DataEditorComponent extends Disposable{
    Border BUTTON_OUTSIDE_BORDER = JBUI.Borders.empty(1);
    Border BUTTON_INSIDE_BORDER = JBUI.Borders.empty(0, 8);
    RoundedLineBorder BUTTON_LINE_BORDER = new RoundedLineBorder(Colors.BUTTON_BORDER_COLOR, 4);
    CompoundBorder BUTTON_BORDER = new CompoundBorder(BUTTON_OUTSIDE_BORDER, new CompoundBorder(BUTTON_LINE_BORDER, BUTTON_INSIDE_BORDER));

    JTextField getTextField();

    void setEditable(boolean editable);

    void setEnabled(boolean enabled);

    UserValueHolder getUserValueHolder();

    void setUserValueHolder(UserValueHolder userValueHolder);

    String getText();

    void setText(String text);
}
