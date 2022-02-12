package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.file.util.VirtualFiles;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

@Slf4j
@EqualsAndHashCode
public class FileConnectionMappingImpl implements FileConnectionMapping {
    private String fileUrl = "";
    private ConnectionId connectionId;
    private SessionId sessionId = SessionId.MAIN;
    private SchemaId schemaId;

    FileConnectionMappingImpl(){}

    public FileConnectionMappingImpl(VirtualFile virtualFile){
        this.fileUrl = virtualFile.getUrl();
    }

    public FileConnectionMappingImpl(String fileUrl, ConnectionId connectionId, SessionId sessionId, SchemaId schemaId) {
        this.fileUrl = fileUrl;
        this.connectionId = connectionId;
        this.sessionId = sessionId;
        this.schemaId = schemaId;
    }

    @Override
    public String getFileUrl() {
        return fileUrl;
    }

    @Override
    @Nullable
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @Override
    @Nullable
    public SessionId getSessionId() {
        return sessionId;
    }

    @Override
    @Nullable
    public SchemaId getSchemaId() {
        return schemaId;
    }

    @Override
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Override
    public boolean setConnectionId(ConnectionId connectionId) {
        if (!Safe.equal(this.connectionId, connectionId)) {
            this.connectionId = connectionId;
            return true;
        }
        return false;
    }

    @Override
    public boolean setSessionId(SessionId sessionId) {
        if (!Safe.equal(this.sessionId, sessionId)) {
            this.sessionId = sessionId;
            return true;
        }
        return false;
    }

    @Override
    public boolean setSchemaId(SchemaId schemaId) {
        if (!Safe.equal(this.schemaId, schemaId)) {
            this.schemaId = schemaId;
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public VirtualFile getFile() {
        try {
            VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
            VirtualFile virtualFile = virtualFileManager.findFileByUrl(fileUrl);
            if (virtualFile != null && virtualFile.isValid()) {
                return virtualFile;
            }
        } catch (Exception e) {
            log.warn("Failed to read file " + fileUrl, e);
        }
        return null;
    }

    @Override
    @Nullable
    public ConnectionHandler getConnection() {
        return ConnectionCache.resolveConnection(connectionId);
    }

    @Override
    @Nullable
    public DatabaseSession getSession() {
        ConnectionHandler connection = getConnection();
        if (connection != null && !connection.isVirtual()) {
            return connection.getSessionBundle().getSession(sessionId);
        }
        return null;
    }


    /*********************************************
     *            PersistentStateElement         *
     *********************************************/
    @Override
    public void readState(Element element) {
        fileUrl = stringAttribute(element, "file-url");

        if (fileUrl == null) {
            // TODO backward compatibility. Do cleanup
            fileUrl = stringAttribute(element, "file-path");
        }

        fileUrl = VirtualFiles.ensureFileUrl(fileUrl);

        connectionId = connectionIdAttribute(element, "connection-id");
        sessionId = sessionIdAttribute(element, "session-id", sessionId);
        schemaId = schemaIdAttribute(element, "current-schema");
    }

    @Override
    public void writeState(Element element) {
        element.setAttribute("file-url", fileUrl);
        element.setAttribute("connection-id", connectionId == null ? "" : connectionId.id());
        element.setAttribute("session-id", sessionId == null ? "" : sessionId.id());
        element.setAttribute("current-schema", schemaId == null ? "" : schemaId.id());
    }
}
