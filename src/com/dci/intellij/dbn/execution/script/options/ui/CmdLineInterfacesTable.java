package com.dci.intellij.dbn.execution.script.options.ui;

import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.execution.script.CmdLineInterfaceBundle;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TableUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;

public class CmdLineInterfacesTable extends DBNTable<CmdLineInterfacesTableModel> {

    public CmdLineInterfacesTable(Project project, CmdLineInterfaceBundle environmentTypes) {
        super(project, new CmdLineInterfacesTableModel(environmentTypes), true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        getSelectionModel().addListSelectionListener(selectionListener);
        setSelectionBackground(UIUtil.getTableBackground());
        setSelectionForeground(UIUtil.getTableForeground());
        setCellSelectionEnabled(true);
        setDefaultRenderer(Object.class, new CmdLineInterfacesTableCellRenderer());

        columnModel.getColumn(0).setMaxWidth(120);
        columnModel.getColumn(1).setMaxWidth(120);
        columnModel.getColumn(0).setPreferredWidth(120);
        columnModel.getColumn(1).setPreferredWidth(120);

        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        addMouseListener(mouseListener);
    }

    public void setExecutorBundle(CmdLineInterfaceBundle executorBundle) {
        super.setModel(new CmdLineInterfacesTableModel(executorBundle));
        columnModel.getColumn(0).setMaxWidth(120);
        columnModel.getColumn(1).setMaxWidth(120);
        columnModel.getColumn(0).setPreferredWidth(120);
        columnModel.getColumn(1).setPreferredWidth(120);
    }

    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                Point point = e.getPoint();
                int rowIndex = rowAtPoint(point);
                int columnIndex = columnAtPoint(point);
                DatabaseType databaseType = (DatabaseType) getValueAt(rowIndex, 0);
                if (columnIndex == 0) {
                    showPopup(databaseType, rowIndex, columnIndex);
                } else if (columnIndex == 2) {
                    Project project = getProject();
                    if (databaseType == null) {
                        MessageUtil.showInfoDialog(project, "Select Database Type", "Please select database type first");
                    } else {
                        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
                        CmdLineInterface defaultCli = CmdLineInterface.getDefault(databaseType);
                        fileChooserDescriptor.
                                withTitle("Select Command-Line Executable").
                                withDescription("Select Command-Line Executable (" + defaultCli + ")").
                                withShowHiddenFiles(true);
                        String executablePath = (String) getValueAt(rowIndex, 2);
                        VirtualFile selectedFile = StringUtil.isEmpty(executablePath) ? null : LocalFileSystem.getInstance().findFileByPath(executablePath);
                        VirtualFile[] virtualFiles = FileChooser.chooseFiles(fileChooserDescriptor, project, selectedFile);
                        if (virtualFiles.length == 1) {
                            setValueAt(virtualFiles[0].getPath(), rowIndex, 2);
                        }
                    }
                }
            }
        }
    };

    private void showPopup(final DatabaseType selectedDatabaseType, int rowIndex, int columnIndex) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (DatabaseType databaseType : DatabaseType.values()) {
            if (databaseType != DatabaseType.UNKNOWN){
                actionGroup.add(new SelectValueAction(databaseType, rowIndex));
            }
        }

        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                null,
                actionGroup,
                DataManager.getInstance().getDataContext(this),
                false,
                false,
                false,
                null, 10, new Condition<AnAction>() {
                    @Override
                    public boolean value(AnAction anAction) {
                        if (anAction instanceof SelectValueAction) {
                            SelectValueAction action = (SelectValueAction) anAction;
                            return action.databaseType.equals(selectedDatabaseType);
                        }
                        return false;
                    }
                });

        Rectangle cellRect = getCellRect(rowIndex, columnIndex, true);
        Point point = new Point((int) cellRect.getX() + 4, (int) cellRect.getHeight() + 1);
        popup.show(new RelativePoint(this, point));
    }

    public class SelectValueAction extends DumbAwareAction {
        private DatabaseType databaseType;
        private int rowIndex;
        public SelectValueAction(DatabaseType databaseType, int rowIndex) {
            super(NamingUtil.enhanceUnderscoresForDisplay(databaseType.getName()), null, databaseType.getIcon());
            this.databaseType = databaseType;
            this.rowIndex = rowIndex;
        }

        public void actionPerformed(AnActionEvent e) {
            setValueAt(databaseType, rowIndex, 0);
            CmdLineInterface defaultCli = CmdLineInterface.getDefault(databaseType);
            if (defaultCli != null) {
                setValueAt(defaultCli.getExecutablePath(), rowIndex, 1);
            }
            setValueAt("", rowIndex, 2);
        }

        @Override
        public void update(AnActionEvent e) {

        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        Point point = e.getPoint();
        int columnIndex = columnAtPoint(point);
        setCursor(columnIndex == 0 || columnIndex == 2 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }


    ListSelectionListener selectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                startCellEditing();
            }
        }
    };

    public void columnSelectionChanged(ListSelectionEvent e) {
        super.columnSelectionChanged(e);
        JTableHeader tableHeader = getTableHeader();
        if (tableHeader != null && tableHeader.getDraggedColumn() == null) {
            if (!e.getValueIsAdjusting()) {
                startCellEditing();
            }
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 1;
    }

    private void startCellEditing() {
        if (getModel().getRowCount() > 0) {
            int[] selectedRows = getSelectedRows();
            int selectedRow = selectedRows.length > 0 ? selectedRows[0] : 0;

            int[] selectedColumns = getSelectedColumns();
            int selectedColumn = selectedColumns.length > 0 ? selectedColumns[0] : 0;

            editCellAt(selectedRow, selectedColumn);
        }
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        super.editingStopped(e);
        CmdLineInterfacesTableModel model = getModel();
        model.notifyListeners(0, model.getRowCount(), 0);
    }

    public Component prepareEditor(TableCellEditor editor, int rowIndex, int columnIndex) {
        final JTextField textField = (JTextField) super.prepareEditor(editor, rowIndex, columnIndex);
        textField.setBorder(new EmptyBorder(0,3,0,0));

        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                textField.selectAll();
                textField.grabFocus();
            }
        }.start();
        selectCell(rowIndex, columnIndex);
        return textField;
    }

    public void insertRow() {
        stopCellEditing();
        int rowIndex = getSelectedRow();
        CmdLineInterfacesTableModel model = getModel();
        rowIndex = model.getRowCount() == 0 ? 0 : rowIndex + 1;
        model.insertRow(rowIndex);
        getSelectionModel().setSelectionInterval(rowIndex, rowIndex);

        revalidate();
        repaint();
    }


    public void removeRow() {
        stopCellEditing();
        int selectedRow = getSelectedRow();
        CmdLineInterfacesTableModel model = getModel();
        model.removeRow(selectedRow);

        revalidate();
        repaint();

        if (model.getRowCount() == selectedRow && selectedRow > 0) {
            getSelectionModel().setSelectionInterval(selectedRow -1, selectedRow -1);
        }
    }

    public void moveRowUp() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();
        stopCellEditing();
        TableUtil.moveSelectedItemsUp(this);
        selectCell(selectedRow -1, selectedColumn);
    }

    public void moveRowDown() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();
        stopCellEditing();
        TableUtil.moveSelectedItemsDown(this);
        selectCell(selectedRow + 1, selectedColumn);
    }
}
