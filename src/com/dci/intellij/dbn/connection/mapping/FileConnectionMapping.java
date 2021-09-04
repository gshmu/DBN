package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

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

    /*********************************************
     *            PersistentStateElement         *
     *********************************************/
    @Override
    public void readState(Element element) {
        fileUrl = element.getAttributeValue("file-url");

        if (fileUrl == null) {
            // TODO backward compatibility. Do cleanup
            fileUrl = element.getAttributeValue("file-path");
        }

        fileUrl = VirtualFileUtil.ensureFileUrl(fileUrl);

        connectionId = ConnectionId.get(element.getAttributeValue("connection-id"));
        sessionId = CommonUtil.nvl(SessionId.get(element.getAttributeValue("session-id")), sessionId);
        schemaId = SchemaId.get(element.getAttributeValue("current-schema"));
    }

    @Override
    public void writeState(Element element) {
        element.setAttribute("file-url", fileUrl);
        element.setAttribute("connection-id", connectionId == null ? "" : connectionId.id());
        element.setAttribute("session-id", sessionId == null ? "" : sessionId.id());
        element.setAttribute("current-schema", schemaId == null ? "" : schemaId.id());
    }
}
