package com.dci.intellij.dbn.connection.mapping.ui;

import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.table.DBNColoredTableCellRenderer;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.ui.table.DBNTableTransferHandler;
import com.dci.intellij.dbn.common.util.Context;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMapping;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FileConnectionMappingTable extends DBNTable<FileConnectionMappingTableModel> {
    private final FileConnectionMappingManager manager;

    public FileConnectionMappingTable(@NotNull DBNComponent parent, FileConnectionMappingTableModel model) {
        super(parent, model, true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDefaultRenderer(FileConnectionMapping.class, new CellRenderer());
        setTransferHandler(DBNTableTransferHandler.INSTANCE);
        initTableSorter();
        setCellSelectionEnabled(true);
        adjustRowHeight(2);
        accommodateColumnsSize();
        addMouseListener(new MouseListener());
        manager = FileConnectionMappingManager.getInstance(getProject());
    }

    @Override
    protected int getMaxColumnWidth() {
        return 800;
    }

    @Override
    public void setModel(@NotNull TableModel dataModel) {
        super.setModel(dataModel);
        initTableSorter();
    }

    private static class CellRenderer extends DBNColoredTableCellRenderer {
        @Override
        protected void customizeCellRenderer(DBNTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
            FileConnectionMapping entry = (FileConnectionMapping) value;
            FileConnectionMappingTableModel model = (FileConnectionMappingTableModel) table.getModel();
            Object columnValue = model.getValue(entry, column);
            if (columnValue instanceof Presentable) {
                Presentable presentable = (Presentable) columnValue;
                setIcon(presentable.getIcon());
            }

            if (columnValue instanceof ConnectionHandler ||
                    columnValue instanceof SchemaId ||
                    columnValue instanceof DatabaseSession) {
                //setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            } else if (columnValue instanceof VirtualFile) {
                VirtualFile virtualFile = (VirtualFile) columnValue;
                setIcon(virtualFile.getFileType().getIcon());
            }

            SimpleTextAttributes textAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
            String presentableValue = model.getPresentableValue(entry, column);
            append(presentableValue, textAttributes);
            setBorder(Borders.TEXT_FIELD_BORDER);
        }
    }

    public class MouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int clickCount = e.getClickCount();
            if (e.getButton() == MouseEvent.BUTTON1) {
                int selectedRow = getSelectedRow();
                int selectedColumn = getSelectedColumn();
                if (selectedRow > -1) {
                    FileConnectionMapping mapping = (FileConnectionMapping) getValueAt(selectedRow, 0);
                    if (mapping != null) {
                        VirtualFile file = mapping.getFile();
                        if (file != null) {
                            if (selectedColumn == 0 && clickCount == 2) {
                                FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
                                fileEditorManager.openFile(file, true);
                            } else if (selectedColumn == 1) {
                                promptConnectionSelector(mapping);
                            } else if (selectedColumn == 2) {
                                promptSchemaSelector(mapping);
                            }
                        }
                    }
                }
            }
        }
    }


    private void promptConnectionSelector(@NotNull FileConnectionMapping mapping) {
        Project project = getProject();
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        List<ConnectionHandler> connectionHandlers = connectionBundle.getConnections();

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        VirtualFile file = mapping.getFile();
        if (connectionHandlers.size() > 0) {
            for (ConnectionHandler connection : connectionHandlers) {
                actionGroup.add(new ConnectionAction(file, connection));
            }
        }

        actionGroup.addSeparator();
        for (ConnectionHandler connection : connectionBundle.getVirtualConnections()) {
            actionGroup.add(new ConnectionAction(file, connection));
        }
        actionGroup.addSeparator();
        actionGroup.add(new ConnectionAction(file, null));

        ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                null,
                actionGroup,
                Context.getDataContext(this),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true,
                null,
                1000,
                action -> ((ConnectionAction) action).getConnectionId() == mapping.getConnectionId(),
                null);

        popupBuilder.showInScreenCoordinates(this, getPopupLocation());
    }

    private void promptSchemaSelector(@NotNull FileConnectionMapping mapping) {
        ConnectionHandler connection = mapping.getConnection();
        if (connection != null) {
            DefaultActionGroup actionGroup = new DefaultActionGroup();
            VirtualFile file = mapping.getFile();
            List<SchemaId> schemaIds = connection.getSchemaIds();
            for (SchemaId schemaId : schemaIds) {
                actionGroup.add(new SchemaAction(file, schemaId));
            }
            ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                    null,
                    actionGroup,
                    Context.getDataContext(this),
                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    true,
                    null,
                    30,
                    action -> ((SchemaAction) action).getSchemaId() == mapping.getSchemaId(),
                    null);

            popupBuilder.showInScreenCoordinates(this, getPopupLocation());
        }
    }

    @NotNull
    private Point getPopupLocation() {
        Point location = getCellLocation(getSelectedRow(), getSelectedColumn());
        Rectangle rectangle = getCellRect(getSelectedRow(), getSelectedColumn(), true);
        location = new Point(
                (int) (location.getX() + rectangle.getWidth() / 2),
                (int) (location.getY() /*+ rectangle.getHeight()*/));
        return location;
    }



    private class ConnectionAction extends AnAction implements DumbAware {
        private final VirtualFile virtualFile;
        private final ConnectionHandlerRef connectionHandler;
        private ConnectionAction(VirtualFile virtualFile, ConnectionHandler connectionHandler) {
            super(
                Safe.call(connectionHandler, c -> c.getName(), "No Connection"), null,
                Safe.call(connectionHandler, c -> c.getIcon()));
            this.virtualFile = virtualFile;
            this.connectionHandler = ConnectionHandlerRef.of(connectionHandler);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            manager.setConnectionHandler(virtualFile, getConnectionHandler());
        }

        @Nullable
        private ConnectionHandler getConnectionHandler() {
            return ConnectionHandlerRef.get(connectionHandler);
        }

        public ConnectionId getConnectionId() {
            ConnectionHandler connectionHandler = getConnectionHandler();
            return connectionHandler == null ? null : connectionHandler.getConnectionId();
        }
    }

    @Getter
    private class SchemaAction extends AnAction implements DumbAware {
        private final VirtualFile virtualFile;
        private final SchemaId schemaId;
        private SchemaAction(VirtualFile virtualFile, SchemaId schemaId) {
            super(schemaId.getName(), "", schemaId.getIcon());
            this.virtualFile = virtualFile;
            this.schemaId = schemaId;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            manager.setDatabaseSchema(virtualFile, schemaId);
        }
    }
}
