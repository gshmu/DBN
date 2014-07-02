package com.dci.intellij.dbn.common.ui.table;

import javax.swing.JTable;
import java.awt.Rectangle;

public class TableUtil {
    public static Rectangle getCellRectangle(JTable table, int row, int column) {
        Rectangle rectangle = table.getCellRect(row, column, true);

        rectangle.setLocation(
                (int) (rectangle.getX() + table.getLocationOnScreen().getX()),
                (int) (rectangle.getY() + table.getLocationOnScreen().getY()));
        return rectangle;
    }
}
