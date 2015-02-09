package com.dci.intellij.dbn.editor.session.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelHeader;
import com.dci.intellij.dbn.editor.session.SessionBrowser;

public class SessionBrowserModelHeader extends ResultSetDataModelHeader implements DataModelHeader {
    public SessionBrowserModelHeader(SessionBrowser sessionBrowser, ResultSet resultSet) throws SQLException {
        super(sessionBrowser.getConnectionHandler(), resultSet);
    }
}
