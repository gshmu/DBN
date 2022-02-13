package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.event.ApplicationEvents;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.locale.options.RegionalSettingsListener;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.Fonts;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.Mouse;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.table.DBNTableGutter;
import com.dci.intellij.dbn.common.ui.table.DBNTableHeaderRenderer;
import com.dci.intellij.dbn.common.ui.table.DBNTableWithGutter;
import com.dci.intellij.dbn.common.ui.table.TableSelectionRestorer;
import com.dci.intellij.dbn.common.util.MathResult;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.dci.intellij.dbn.data.model.basic.BasicDataModelCell;
import com.dci.intellij.dbn.data.preview.LargeValuePreviewPopup;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.intellij.ide.IdeTooltip;
import com.intellij.ide.IdeTooltipManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.ui.components.JBViewport;
import com.intellij.util.Alarm;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BasicTable<T extends BasicDataModel<?, ?>> extends DBNTableWithGutter<T> implements EditorColorsListener, Disposable {
    private final BasicTableCellRenderer cellRenderer;
    private final RegionalSettings regionalSettings;
    private final DataGridSettings dataGridSettings;
    private final TableSelectionRestorer selectionRestorer = createSelectionRestorer();
    private JBPopup valuePopup;
    private MathResult selectionMath;
    private boolean loading;

    public BasicTable(DBNComponent parent, T dataModel) {
        super(parent, dataModel, true);

        Project project = getProject();
        regionalSettings = RegionalSettings.getInstance(project);
        dataGridSettings = DataGridSettings.getInstance(project);
        cellRenderer = createCellRenderer();
        DataGridTextAttributes displayAttributes = cellRenderer.getAttributes();

        ApplicationEvents.subscribe(this, EditorColorsManager.TOPIC, this);
        Color bgColor = displayAttributes.getPlainData(false, false).getBgColor();
        setBackground(bgColor == null ? Colors.getTableBackground() : bgColor);
        addMouseListener(Mouse.listener().onClick(e -> {
            if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && valuePopup == null) {
                showCellValuePopup();
            }}));

        addPropertyChangeListener(e -> {
            Object newProperty = e.getNewValue();
            if (newProperty instanceof Font) {
                Font font = (Font) newProperty;
                adjustRowHeight();
                JTableHeader tableHeader = getTableHeader();
                if (tableHeader != null) {
                    TableCellRenderer defaultRenderer = tableHeader.getDefaultRenderer();
                    if (defaultRenderer instanceof DBNTableHeaderRenderer) {
                        DBNTableHeaderRenderer renderer = (DBNTableHeaderRenderer) defaultRenderer;
                        renderer.setFont(font);
                    }
                }
                accommodateColumnsSize();
            }

        });

        getSelectionModel().addListSelectionListener(e -> {
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal count = BigDecimal.ZERO;
            if (!e.getValueIsAdjusting()) {
                selectionMath = null;
                int rows = getSelectedRowCount();
                int columns = getSelectedColumnCount();
                if (columns == 1 && rows > 1 && rows < 100) {
                    int selectedColumn = getSelectedColumn();
                    int[] selectedRows = getSelectedRows();
                    for (int selectedRow : selectedRows) {
                        Object value = getValueAt(selectedRow, selectedColumn);
                        if (value instanceof BasicDataModelCell) {
                            BasicDataModelCell<?, ?> cell = (BasicDataModelCell<?, ?>) value;
                            Object userValue = cell.getUserValue();
                            if (userValue == null || userValue instanceof Number) {
                                if (userValue != null) {
                                    count = count.add(BigDecimal.ONE);
                                    Number number = (Number) userValue;
                                    total = total.add(new BigDecimal(number.toString()));
                                }

                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                    if (count.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal average = total.divide(count, total.scale(), RoundingMode.HALF_UP);
                        selectionMath = new MathResult(total, count, average);
                        showSelectionTooltip();
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            private final Alarm runner = Dispatch.alarm(BasicTable.this);
            @Override
            public void mouseMoved(MouseEvent e) {
                if (selectionMath != null && isCellSelected(e.getPoint())) {
                    Dispatch.alarmRequest(runner, 100, true, () -> showSelectionTooltip());
                }
            }
        });

        ProjectEvents.subscribe(project, this, RegionalSettingsListener.TOPIC, regionalSettingsListener);
        ApplicationEvents.subscribe(this, EditorColorsManager.TOPIC, this);

        //EventUtil.subscribe(this, UISettingsListener.TOPIC, this);
    }

    boolean isCellSelected(Point point) {
        DataModelCell<?, ?> cell = getCellAtLocation(point);
        if (cell != null) {
            int rowIndex = cell.getRow().getIndex();
            int columnIndex = cell.getColumnInfo().getColumnIndex();
            return isCellSelected(rowIndex, columnIndex);
        }
        return false;
    }

    private void showSelectionTooltip() {
        MathResult mathResult = this.selectionMath;
        if (mathResult != null) {
            MathPanel mathPanel = new MathPanel(getProject(), mathResult);
            Point mousePosition = getMousePosition();
            if (mousePosition != null && isCellSelected(mousePosition)) {
                IdeTooltip tooltip = new IdeTooltip(this, mousePosition, mathPanel.getComponent());
                tooltip.setFont(Fonts.deriveFont(Fonts.REGULAR,  (float) 16));
                IdeTooltipManager.getInstance().show(tooltip, true);
            }
        }
    }

    private final RegionalSettingsListener regionalSettingsListener = () -> regionalSettingsChanged();

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
    protected BasicTableGutter<?> createTableGutter() {
        return new BasicTableGutter<>(this);
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
        this.loading = loading;
        updateBackground(loading);
    }

    public void updateBackground(boolean readonly) {
        Dispatch.run(() -> {
            JBViewport viewport = UIUtil.getParentOfType(JBViewport.class, this);
            DataGridTextAttributes attributes = cellRenderer.getAttributes();
            Color background = readonly ?
                    attributes.getLoadingData(false).getBgColor() :
                    attributes.getPlainData(false, false).getBgColor();

            if (viewport != null) {
                viewport.setBackground(background);
                viewport.getParent().setBackground(background);
                GUIUtil.repaint(viewport);
            }
        });

    }

    public boolean isLoading() {
        return loading;
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

        DBNTableGutter<?> tableGutter = getTableGutter();
        if (tableGutter != null) {
            tableGutter.setFixedCellHeight(rowHeight);
            tableGutter.setFixedCellWidth(getModel().getRowCount() == 0 ? 10 : -1);
        }
    }

    @Nullable
    public DataModelCell<?, ?> getCellAtLocation(Point point) {
        int columnIndex = columnAtPoint(point);
        int rowIndex = rowAtPoint(point);
        return columnIndex > -1 && rowIndex > -1 ? getCellAtPosition(rowIndex, columnIndex) : null;
    }

    @Nullable
    protected DataModelCell<?, ?> getCellAtMouseLocation() {
        Point location = MouseInfo.getPointerInfo().getLocation();
        location.setLocation(location.getX() - getLocationOnScreen().getX(), location.getY() - getLocationOnScreen().getY());
        return getCellAtLocation(location);
    }

    public int getModelColumnIndex(int columnIndex) {
        return getColumnModel().getColumn(columnIndex).getModelIndex();
    }

    @Nullable
    protected DataModelCell<?, ?> getCellAtPosition(int rowIndex, int columnIndex) {
        DataModelRow<?, ?> row = getModel().getRowAtIndex(rowIndex);
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
        updateBackground(loading);
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
                    DataModelCell<?, ?> cell = (DataModelCell<?, ?>) getValueAt(rowIndex, columnIndex);
                    TableColumn column = getColumnModel().getColumn(columnIndex);

                    int preferredWidth = column.getWidth();
                    LargeValuePreviewPopup viewer = new LargeValuePreviewPopup(getProject(), this, cell, preferredWidth);
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

    @Nullable
    public MathResult getSelectionMath() {
        return selectionMath;
    }

    private boolean canDisplayCompleteValue(int rowIndex, int columnIndex) {
        DataModelCell<?, ?> cell = (DataModelCell<?, ?>) getValueAt(rowIndex, columnIndex);
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

    public Rectangle getCellRect(DataModelCell<?, ?> cell) {
        int rowIndex = convertRowIndexToView(cell.getRow().getIndex());
        int columnIndex = convertColumnIndexToView(cell.getIndex());
        return getCellRect(rowIndex, columnIndex, true);
    }

    public void scrollCellToVisible(DataModelCell<?, ?> cell) {
        Rectangle cellRectangle = getCellRect(cell);
        scrollRectToVisible(cellRectangle);
    }

    @NotNull
    @Override
    public T getModel() {
        return super.getModel();
    }

}
