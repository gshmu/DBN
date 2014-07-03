package com.dci.intellij.dbn.data.ui.table.basic;

import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.data.editor.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.dci.intellij.dbn.data.preview.LargeValuePreviewPopup;
import com.dci.intellij.dbn.data.value.LazyLoadedValue;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBViewport;
import com.intellij.util.ui.UIUtil;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;

public class BasicTable<T extends BasicDataModel> extends DBNTable<T> implements EditorColorsListener, Disposable {
    private BasicTableCellRenderer cellRenderer;
    private BasicTableGutter tableGutter;
    private JBPopup valuePopup;
    private boolean isLoading;
    protected RegionalSettings regionalSettings;

    public BasicTable(Project project, T dataModel) {
        super(project, dataModel, true);
        cellRenderer = createCellRenderer(project);
        DataGridTextAttributes displayAttributes = cellRenderer.getAttributes();
        setSelectionForeground(displayAttributes.getSelection().getFgColor());
        setSelectionBackground(displayAttributes.getSelection().getBgColor());
        EditorColorsManager.getInstance().addEditorColorsListener(this);
        Color bgColor = displayAttributes.getPlainData(false, false).getBgColor();
        setBackground(bgColor == null ? UIUtil.getTableBackground() : bgColor);
    }

    protected BasicTableCellRenderer createCellRenderer(Project project) {
        return new BasicTableCellRenderer(project);
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
        updateBackground(loading);
    }

    public void updateBackground(final boolean readonly) {
        final JBViewport viewport = UIUtil.getParentOfType(JBViewport.class, this);
        if (viewport != null) {
            new ConditionalLaterInvocator() {
                @Override
                public void execute() {
                    DataGridTextAttributes attributes = cellRenderer.getAttributes();
                    Color background = readonly ?
                            attributes.getLoadingData(false).getBgColor() :
                            attributes.getPlainData(false, false).getBgColor();
                    viewport.setBackground(background);
                    viewport.repaint();
                }
            }.start();
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public BasicTableGutter createTableGutter() {
        return new BasicTableGutter(this);
    }

    public BasicTableGutter getTableGutter() {
        if (tableGutter == null) {
            tableGutter = createTableGutter();
            Disposer.register(this, tableGutter);
        }
        return tableGutter;
    }

    public void selectRow(int index) {
        clearSelection();
        int lastColumnIndex = getModel().getColumnCount() - 1;
        setColumnSelectionInterval(0, lastColumnIndex);
        getSelectionModel().setSelectionInterval(index, index);
        scrollRectToVisible(getCellRect(index, 0, true));
    }

    public TableCellRenderer getCellRenderer(int i, int i1) {
        return cellRenderer;
    }

    public BasicTableCellRenderer getCellRenderer() {
        return cellRenderer;
    }

    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        if (e.getFirstRow() != e.getLastRow()) {
            accommodateColumnsSize();
        }

        if (tableGutter != null) {
            tableGutter.setFixedCellHeight(rowHeight);
            tableGutter.setFixedCellWidth(getModel().getSize() == 0 ? 10 : -1);
        }
    }

    public DataModelCell getCellAtLocation(Point point) {
        int columnIndex = columnAtPoint(point);
        int rowIndex = rowAtPoint(point);
        return columnIndex > -1 && rowIndex > -1 ? getCellAtPosition(rowIndex, columnIndex) : null;
    }

    public DataModelCell getCellAtMouseLocation() {
        Point location = MouseInfo.getPointerInfo().getLocation();
        location.setLocation(location.getX() - getLocationOnScreen().getX(), location.getY() - getLocationOnScreen().getY());
        return getCellAtLocation(location);
    }

    public int getModelColumnIndex(int columnIndex) {
        return getColumnModel().getColumn(columnIndex).getModelIndex();
    }

    public DataModelCell getCellAtPosition(int rowIndex, int columnIndex) {
        DataModelRow row = getModel().getRowAtIndex(rowIndex);
        int modelColumnIndex = getModelColumnIndex(columnIndex);
        return row.getCellAtIndex(modelColumnIndex);
    }
    /*********************************************************
     *                EditorColorsListener                  *
     *********************************************************/
    @Override
    public void globalSchemeChange(EditorColorsScheme scheme) {
        cellRenderer.getAttributes().load();
        repaint();
    }

    /*********************************************************
     *                ListSelectionListener                  *
     *********************************************************/
    public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        if (!e.getValueIsAdjusting()) {
            if (hasFocus()) getTableGutter().clearSelection();
            showCellValuePopup();
        }
    }

    public void columnSelectionChanged(ListSelectionEvent e) {
        JTableHeader tableHeader = getTableHeader();
        if (tableHeader != null && tableHeader.getDraggedColumn() == null) {
            super.columnSelectionChanged(e);
            if (!e.getValueIsAdjusting()) {
                showCellValuePopup();
            }
        }
    }

    private void showCellValuePopup() {
        if (valuePopup != null) {
            valuePopup.cancel();
            valuePopup = null;
        }
        if (isLargeValuePopupActive()) {
            boolean isReadonly = getModel().isReadonly() || getModel().getState().isReadonly();
            if (isReadonly && getSelectedColumnCount() == 1 && getSelectedRowCount() == 1) {
                int rowIndex = getSelectedRows()[0];
                int columnIndex = getSelectedColumns()[0];
                if (!canDisplayCompleteValue(rowIndex, columnIndex)) {
                    Rectangle cellRect = getCellRect(rowIndex, columnIndex, true);
                    DataModelCell cell = (DataModelCell) getValueAt(rowIndex, columnIndex);
                    TableColumn column = getColumnModel().getColumn(columnIndex);

                    int preferredWidth = column.getWidth();
                    LargeValuePreviewPopup viewer = new LargeValuePreviewPopup(this, cell, preferredWidth);
                    initLargeValuePopup(viewer);
                    Point location = cellRect.getLocation();
                    location.setLocation(location.getX() + 4, location.getY() + 20);
                    valuePopup = viewer.show(this, location);
                }
            }
        }
    }

    protected void initLargeValuePopup(LargeValuePreviewPopup viewer) {
    }

    protected boolean isLargeValuePopupActive() {
        return true;
    }

    private boolean canDisplayCompleteValue(int rowIndex, int columnIndex) {
        DataModelCell cell = (DataModelCell) getValueAt(rowIndex, columnIndex);
        if (cell != null) {
            Object value = cell.getUserValue();
            if (value instanceof LazyLoadedValue) {
                return false;
            }
            if (value != null) {
                TableCellRenderer renderer = getCellRenderer(rowIndex, columnIndex);
                Component component = renderer.getTableCellRendererComponent(this, cell, false, false, rowIndex, columnIndex);
                TableColumn column = getColumnModel().getColumn(columnIndex);
                return component.getPreferredSize().width <= column.getWidth();
            }
        }
        return true;
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    public void dispose() {
        super.dispose();
        EditorColorsManager.getInstance().removeEditorColorsListener(this);
    }

    public Rectangle getCellRect(DataModelCell cell) {
        int rowIndex = convertRowIndexToView(cell.getRow().getIndex());
        int columnIndex = convertColumnIndexToView(cell.getIndex());
        return getCellRect(rowIndex, columnIndex, true);
    }

    public void scrollCellToVisible(DataModelCell cell) {
        Rectangle cellRectangle = getCellRect(cell);
        scrollRectToVisible(cellRectangle);
    }
}
