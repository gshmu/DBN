package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SessionId;
import org.jdom.Element;

public class FileConnectionMapping implements PersistentStateElement<Element> {
    private String fileUrl = "";
    private ConnectionId connectionId;
    private SessionId sessionId = SessionId.MAIN;
    private String currentSchema = "";

    FileConnectionMapping(){}

    FileConnectionMapping(String fileUrl, ConnectionId connectionId, SessionId sessionId, String currentSchema) {
        this.fileUrl = fileUrl;
        this.connectionId = connectionId;
        this.sessionId = sessionId;
        this.currentSchema = currentSchema;
    }

    public String getFileUrl() {
        return fileUrl;
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

    public String getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(String currentSchema) {
        this.currentSchema = currentSchema;
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
        currentSchema = element.getAttributeValue("current-schema");
    }

    public void writeState(Element element) {
        element.setAttribute("file-url", fileUrl);
        element.setAttribute("connection-id", connectionId == null ? "" : connectionId.id());
        element.setAttribute("session-id", sessionId == null ? "" : sessionId.id());
        element.setAttribute("current-schema", currentSchema == null ? "" : currentSchema);
    }
}
