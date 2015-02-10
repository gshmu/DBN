package com.dci.intellij.dbn.editor.session.ui.table;

import javax.swing.table.TableCellRenderer;
import java.sql.SQLException;
import java.util.EventObject;

import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableCellRenderer;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTableHeaderRenderer;
import com.dci.intellij.dbn.data.preview.LargeValuePreviewPopup;
import com.dci.intellij.dbn.data.record.RecordViewInfo;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;

public class SessionBrowserTable extends ResultSetTable<SessionBrowserModel> {
    private SessionBrowser sessionBrowser;

    public SessionBrowserTable(SessionBrowser sessionBrowser, SessionBrowserModel model) throws SQLException {
        super(model, false, new RecordViewInfo(sessionBrowser.getConnectionHandler().getName(), null));
        getTableHeader().setDefaultRenderer(new SortableTableHeaderRenderer());
        this.sessionBrowser = sessionBrowser;


        DataProvider dataProvider = sessionBrowser.getDataProvider();
        ActionUtil.registerDataProvider(this, dataProvider, false);
        ActionUtil.registerDataProvider(getTableHeader(), dataProvider, false);
    }

    @Override
    protected BasicTableCellRenderer createCellRenderer() {
        return new BasicTableCellRenderer();
    }

    public Project getProject() {
        return sessionBrowser.getProject();
    }


    public String getName() {
        return sessionBrowser == null ? "Disposed" : sessionBrowser.getConnectionHandler().getName();
    }

    @Override
    protected BasicTableGutter createTableGutter() {
        return new SessionBrowserTableGutter(this);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return getCellRenderer();
    }

    public void clearSelection() {
        new ConditionalLaterInvocator() {
            public void execute() {
                SessionBrowserTable.super.clearSelection();
            }
        }.start();
    }

    @Override
    public void removeEditor() {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                SessionBrowserTable.super.removeEditor();
            }
        }.start();
    }

    public void updateTableGutter() {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                getTableGutter().revalidate();
                getTableGutter().repaint();
            }
        }.start();
    }

    @Override
    public boolean editCellAt(final int row, final int column, final EventObject e) {
        return super.editCellAt(row, column, e);
    }

    @Override
    protected void initLargeValuePopup(LargeValuePreviewPopup viewer) {
        super.initLargeValuePopup(viewer);
    }

    @Override
    public int getColumnWidthSpan() {
        return 0;
    }

    public SessionBrowser getSessionBrowser() {
        return sessionBrowser;
    }

    /********************************************************
     *                     Disposable                       *
     ********************************************************/

    @Override
    public void dispose() {
        super.dispose();
        sessionBrowser = null;
    }
}
