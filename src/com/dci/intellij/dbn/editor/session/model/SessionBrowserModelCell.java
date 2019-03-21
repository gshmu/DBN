package com.dci.intellij.dbn.editor.session.model;


import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelCell;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionBrowserModelCell extends ResultSetDataModelCell implements ChangeListener {

    public SessionBrowserModelCell(SessionBrowserModelRow row, ResultSet resultSet, ResultSetColumnInfo columnInfo) throws SQLException {
        super(row, resultSet, columnInfo);
    }

    @Override
    public ResultSetColumnInfo getColumnInfo() {
        return (ResultSetColumnInfo) super.getColumnInfo();
    }

    public ConnectionHandler getConnectionHandler() {
        return getRow().getModel().getConnectionHandler();
    }

    @Override
    @NotNull
    public SessionBrowserModelRow getRow() {
        return (SessionBrowserModelRow) super.getRow();
    }

    /*********************************************************
     *                    ChangeListener                     *
     *********************************************************/
    @Override
    public void stateChanged(ChangeEvent e) {
    }
}
