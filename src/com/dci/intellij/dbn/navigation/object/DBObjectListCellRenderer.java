package com.dci.intellij.dbn.navigation.object;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DBObjectListCellRenderer extends ColoredListCellRenderer {
    public static final DBObjectListCellRenderer INSTANCE = new DBObjectListCellRenderer();

    private DBObjectListCellRenderer() {}

    @Override
    protected void customizeCellRenderer(@NotNull JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof DBObject) {
            DBObject object = (DBObject) value;
            setIcon(object.getIcon());
            append(object.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            ConnectionHandler connectionHandler = Failsafe.nn(object.getConnection());
            append(" [" + connectionHandler.getName() + "]", SimpleTextAttributes.GRAY_ATTRIBUTES);
            if (object.getParentObject() != null) {
                append(" - " + object.getParentObject().getQualifiedName(), SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        } else {
            append(value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }
}
