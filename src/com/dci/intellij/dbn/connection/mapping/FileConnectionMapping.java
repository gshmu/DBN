package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.file.util.VirtualFileUtil;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

@Slf4j
@Getter
@Setter
@EqualsAndHashCode
public class FileConnectionMapping implements PersistentStateElement {
    private String fileUrl = "";
    private ConnectionId connectionId;
    private SessionId sessionId = SessionId.MAIN;
    private SchemaId schemaId;

    FileConnectionMapping(){}

    FileConnectionMapping(String fileUrl, ConnectionId connectionId, SessionId sessionId, SchemaId schemaId) {
        this.fileUrl = fileUrl;
        this.connectionId = connectionId;
        this.sessionId = sessionId;
        this.schemaId = schemaId;
    }

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

    @Nullable
    public ConnectionHandler getConnection() {
        return ConnectionCache.findConnectionHandler(connectionId);
    }

    @Nullable
    public DatabaseSession getSession() {
        ConnectionHandler connection = getConnection();
        if (connection != null) {
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

        fileUrl = VirtualFileUtil.ensureFileUrl(fileUrl);

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
