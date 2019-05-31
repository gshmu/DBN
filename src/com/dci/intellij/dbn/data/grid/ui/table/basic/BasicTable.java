package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.locale.options.RegionalSettingsListener;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.table.DBNTableWithGutter;
import com.dci.intellij.dbn.common.ui.table.TableSelectionRestorer;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.dci.intellij.dbn.data.preview.LargeValuePreviewPopup;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.ui.components.JBViewport;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BasicTable<T extends BasicDataModel> extends DBNTableWithGutter<T> implements EditorColorsListener, Disposable {
    private BasicTableCellRenderer cellRenderer;
    private JBPopup valuePopup;
    private boolean isLoading;
    private RegionalSettings regionalSettings;
    private DataGridSettings dataGridSettings;
    private TableSelectionRestorer selectionRestorer = createSelectionRestorer();

    public BasicTable(Project project, T dataModel) {
        super(project, dataModel, true);
        regionalSettings = RegionalSettings.getInstance(project);
        dataGridSettings = DataGridSettings.getInstance(project);
        cellRenderer = createCellRenderer();
        DataGridTextAttributes displayAttributes = cellRenderer.getAttributes();
        Color selectionFgColor = displayAttributes.getSelection().getFgColor();
        Color selectionBgColor = displayAttributes.getSelection().getBgColor();
        if (selectionFgColor != null && selectionBgColor != null) {
            setSelectionForeground(selectionFgColor);
            setSelectionBackground(selectionBgColor);
        }
        EditorColorsManager.getInstance().addEditorColorsListener(this, this);
        Color bgColor = displayAttributes.getPlainData(false, false).getBgColor();
        setBackground(bgColor == null ? UIUtil.getTableBackground() : bgColor);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && valuePopup == null) {
                    showCellValuePopup();
                }
            }
        });

        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                Object newProperty = e.getNewValue();
                if (newProperty instanceof Font) {
                    Font font = (Font) newProperty;
                    adjustRowHeight();
                    JTableHeader tableHeader = getTableHeader();
                    if (tableHeader != null) {
                        TableCellRenderer defaultRenderer = tableHeader.getDefaultRenderer();
                        if (defaultRenderer instanceof BasicTableHeaderRenderer) {
                            BasicTableHeaderRenderer renderer = (BasicTableHeaderRenderer) defaultRenderer;
                            renderer.setFont(font);
                        }
                    }
                    accommodateColumnsSize();
                }

            }
        });

        EventUtil.subscribe(project, this, RegionalSettingsListener.TOPIC, regionalSettingsListener);
        //EventUtil.subscribe(this, EditorColorsManager.TOPIC, this);
        EditorColorsManager.getInstance().addEditorColorsListener(this, this);

        //EventUtil.subscribe(this, UISettingsListener.TOPIC, this);
    }

    private RegionalSettingsListener regionalSettingsListener = new RegionalSettingsListener() {
        @Override
        public void settingsChanged() {
            regionalSettingsChanged();
        }
    };

    protected void regionalSettingsChanged() {
        resizeAndRepaint();
    }

    @NotNull
    public BasicTableSelectionRestorer createSelectionRestorer() {
        return new BasicTableSelectionRestorer();
    }

    public boolean isRestoringSelection() {
        return selectionRestorer.isRestoring();
    }

    public void snapshotSelection() {
        selectionRestorer.snapshot();
    }

    public void restoreSelection() {
        selectionRestorer.restore();
    }

    @Override
    protected BasicTableGutter createTableGutter() {
        return new BasicTableGutter(this);
    }

    public RegionalSettings getRegionalSettings() {
        return regionalSettings;
    }

    public DataGridSettings getDataGridSettings() {
        return dataGridSettings;
    }

    protected BasicTableCellRenderer createCellRenderer() {
        return new BasicTableCellRenderer();
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
        updateBackground(loading);
    }

    public void updateBackground(boolean readonly) {
        Dispatch.run(() -> {
            JBViewport viewport = UIUtil.getParentOfType(JBViewport.class, this);
            if (viewport != null) {
                DataGridTextAttributes attributes = cellRenderer.getAttributes();
                Color background = readonly ?
                        attributes.getLoadingData(false).getBgColor() :
                        attributes.getPlainData(false, false).getBgColor();
                viewport.setBackground(background);

                GUIUtil.repaint(viewport);
            }
        });

    }

    public boolean isLoading() {
        return isLoading;
    }

    public void selectRow(int index) {
        int columnCount = getModel().getColumnCount();
        if (columnCount > 0) {
            clearSelection();
            int lastColumnIndex = Math.max(0, columnCount - 1);
            setColumnSelectionInterval(0, lastColumnIndex);
            getSelectionModel().setSelectionInterval(index, index);
            scrollRectToVisible(getCellRect(index, 0, true));
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(int i, int i1) {
        return cellRenderer;
    }

    public BasicTableCellRenderer getCellRenderer() {
        return cellRenderer;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        if (e.getFirstRow() != e.getLastRow()) {
            accommodateColumnsSize();
        }

        if (tableGutter != null) {
            tableGutter.setFixedCellHeight(rowHeight);
            tableGutter.setFixedCellWidth(getModel().getRowCount() == 0 ? 10 : -1);
        }
    }

    @Nullable
    public DataModelCell getCellAtLocation(Point point) {
        int columnIndex = columnAtPoint(point);
        int rowIndex = rowAtPoint(point);
        return columnIndex > -1 && rowIndex > -1 ? getCellAtPosition(rowIndex, columnIndex) : null;
    }

    @Nullable
    protected DataModelCell getCellAtMouseLocation() {
        Point location = MouseInfo.getPointerInfo().getLocation();
        location.setLocation(location.getX() - getLocationOnScreen().getX(), location.getY() - getLocationOnScreen().getY());
        return getCellAtLocation(location);
    }

    public int getModelColumnIndex(int columnIndex) {
        return getColumnModel().getColumn(columnIndex).getModelIndex();
    }

    @Nullable
    protected DataModelCell getCellAtPosition(int rowIndex, int columnIndex) {
        DataModelRow row = getModel().getRowAtIndex(rowIndex);
        if (row != null) {
            int modelColumnIndex = getModelColumnIndex(columnIndex);
            return row.getCellAtIndex(modelColumnIndex);
        }
        return null;
    }
    /*********************************************************
     *                EditorColorsListener                  *
     *********************************************************/
    @Override
    public void globalSchemeChange(EditorColorsScheme scheme) {
        cellRenderer.getAttributes().load();
        updateBackground(isLoading);
        resizeAndRepaint();
/*        JBScrollPane scrollPane = UIUtil.getParentOfType(JBScrollPane.class, this);
        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.repaint();
        }*/
    }

    /*********************************************************
     *                ListSelectionListener                  *
     *********************************************************/
    @Override
    public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        if (!e.getValueIsAdjusting()) {
            if (hasFocus()) getTableGutter().clearSelection();
            showCellValuePopup();
        }
    }

    @Override
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
        if (isLargeValuePopupActive() && !isRestoringSelection()) {
            T model = getModel();
            DataModelState modelState = model.getState();
            boolean isReadonly = model.isReadonly() || model.isEnvironmentReadonly() || modelState.isReadonly() ;
            if (isReadonly && getSelectedColumnCount() == 1 && getSelectedRowCount() == 1 && this.isShowing()) {
                int rowIndex = getSelectedRow();
                int columnIndex = getSelectedColumn();
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
                    valuePopup.addListener(
                        new JBPopupAdapter() {
                            @Override
                            public void onClosed(@NotNull LightweightWindowEvent event) {
                                valuePopup.cancel();
                                valuePopup = null;
                            }
                        }
                    );
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
            if (value instanceof LargeObjectValue) {
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

    public Rectangle getCellRect(DataModelCell cell) {
        int rowIndex = convertRowIndexToView(cell.getRow().getIndex());
        int columnIndex = convertColumnIndexToView(cell.getIndex());
        return getCellRect(rowIndex, columnIndex, true);
    }

    public void scrollCellToVisible(DataModelCell cell) {
        Rectangle cellRectangle = getCellRect(cell);
        scrollRectToVisible(cellRectangle);
    }

    @NotNull
    @Override
    public T getModel() {
        return super.getModel();
    }

}
