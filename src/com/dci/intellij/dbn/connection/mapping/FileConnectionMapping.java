package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SessionId;
import com.intellij.openapi.vfs.StandardFileSystems;
import org.jdom.Element;

public class FileConnectionMapping implements PersistentStateElement<Element> {
    private String fileUrl = "";
    private ConnectionId connectionId;
    private SessionId sessionId = SessionId.MAIN;
    private String schemaName = "";

    FileConnectionMapping(){}

    FileConnectionMapping(String fileUrl, ConnectionId connectionId, SessionId sessionId, String schemaName) {
        this.fileUrl = fileUrl;
        this.connectionId = connectionId;
        this.sessionId = sessionId;
        this.schemaName = schemaName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFilePath() {
        String filePath = fileUrl;
        if (filePath.startsWith(StandardFileSystems.FILE_PROTOCOL_PREFIX)) {
            filePath = filePath.substring(StandardFileSystems.FILE_PROTOCOL_PREFIX.length());
        }
        return filePath;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
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

        return getFileUrl().equals(that.getFileUrl());
    }

    @Override
    public int hashCode() {
        return getFileUrl().hashCode();
    }

    /*********************************************
     *            PersistentStateElement         *
     *********************************************/
    public void readState(Element element) {
        fileUrl = element.getAttributeValue("file-url");
        // fixme remove this backward compatibility 
        if (fileUrl == null) fileUrl = element.getAttributeValue("file-path");
        connectionId = ConnectionId.get(element.getAttributeValue("connection-id"));
        sessionId = CommonUtil.nvl(SessionId.get(element.getAttributeValue("session-id")), sessionId);
        schemaName = element.getAttributeValue("current-schema");
    }

    public void writeState(Element element) {
        element.setAttribute("file-url", fileUrl);
        element.setAttribute("connection-id", connectionId == null ? "" : connectionId.id());
        element.setAttribute("session-id", sessionId == null ? "" : sessionId.id());
        element.setAttribute("current-schema", schemaName == null ? "" : schemaName);
    }
}
