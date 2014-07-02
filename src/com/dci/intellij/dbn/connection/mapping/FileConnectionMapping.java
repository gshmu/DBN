package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public class FileConnectionMapping implements PersistentConfiguration {
    String fileUrl = "";
    String connectionId = "";
    String currentSchema = "";

    public FileConnectionMapping(Element element) throws InvalidDataException {                
        readConfiguration(element);
    }

    public FileConnectionMapping(String fileUrl, String connectionId, String currentSchema) {
        this.fileUrl = fileUrl;
        this.connectionId = connectionId;
        this.currentSchema = currentSchema;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(String currentSchema) {
        this.currentSchema = currentSchema;
    }

    /***************************************
     *          JDOMExternalizable         *
     ***************************************/
    public void readConfiguration(Element element) throws InvalidDataException {
        fileUrl = element.getAttributeValue("file-url");
        // fixme remove this backward compatibility 
        if (fileUrl == null) fileUrl = element.getAttributeValue("file-path");
        connectionId = element.getAttributeValue("connection-id");
        currentSchema = element.getAttributeValue("current-schema");
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        element.setAttribute("file-url", fileUrl);
        element.setAttribute("connection-id", connectionId == null ? "" : connectionId);
        element.setAttribute("current-schema", currentSchema == null ? "" : currentSchema);
    }
}
