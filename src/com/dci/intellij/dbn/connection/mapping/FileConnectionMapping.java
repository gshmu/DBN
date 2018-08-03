package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SessionId;
import com.intellij.openapi.vfs.StandardFileSystems;
import org.jdom.Element;

public class FileConnectionMapping implements PersistentStateElement<Element> {
    private String filePath = "";
    private ConnectionId connectionId;
    private SessionId sessionId = SessionId.MAIN;
    private String schemaName = "";

    FileConnectionMapping(){}

    FileConnectionMapping(String filePath, ConnectionId connectionId, SessionId sessionId, String schemaName) {
        this.filePath = filePath;
        this.connectionId = connectionId;
        this.sessionId = sessionId;
        this.schemaName = schemaName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public ConnectionId getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public void setSessionId(SessionId sessionId) {
        this.sessionId = sessionId;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileConnectionMapping)) return false;

        FileConnectionMapping that = (FileConnectionMapping) o;

        return filePath.equals(that.filePath);
    }

    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    /*********************************************
     *            PersistentStateElement         *
     *********************************************/
    public void readState(Element element) {
        filePath = element.getAttributeValue("file-url");
        // fixme remove this backward compatibility 
        if (filePath == null) filePath = element.getAttributeValue("file-path");

        if (filePath.startsWith(StandardFileSystems.FILE_PROTOCOL_PREFIX)) {
            filePath = filePath.substring(StandardFileSystems.FILE_PROTOCOL_PREFIX.length());
        }

        connectionId = ConnectionId.get(element.getAttributeValue("connection-id"));
        sessionId = CommonUtil.nvl(SessionId.get(element.getAttributeValue("session-id")), sessionId);
        schemaName = element.getAttributeValue("current-schema");
    }

    public void writeState(Element element) {
        element.setAttribute("file-path", filePath);
        element.setAttribute("connection-id", connectionId == null ? "" : connectionId.id());
        element.setAttribute("session-id", sessionId == null ? "" : sessionId.id());
        element.setAttribute("current-schema", schemaName == null ? "" : schemaName);
    }
}
