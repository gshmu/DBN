package com.dci.intellij.dbn.data.export;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

import java.io.File;

public class DataExportInstructions implements JDOMExternalizable, Cloneable {
    private boolean createHeader = true;
    private boolean quoteValuesContainingSeparator = true;
    private boolean quoteAllValues = false;
    private String valueSeparator;
    private String fileName;
    private String fileLocation;
    private Scope scope = Scope.GLOBAL;
    private Destination destination = Destination.FILE;
    private DataExportFormat format = DataExportFormat.EXCEL;
    private String baseName;

    public boolean createHeader() {
        return createHeader;
    }

    public void setCreateHeader(boolean createHeader) {
        this.createHeader = createHeader;
    }

    public boolean quoteValuesContainingSeparator() {
        return quoteValuesContainingSeparator;
    }

    public void quoteValuesContainingSeparator(boolean quoteValuesContainingSeparator) {
        this.quoteValuesContainingSeparator = quoteValuesContainingSeparator;
    }

    public boolean quoteAllValues() {
        return quoteAllValues;
    }

    public void setQuoteAllValues(boolean quoteAllValues) {
        this.quoteAllValues = quoteAllValues;
    }

    public DataExportFormat getFormat() {
        return format;
    }

    public void setFormat(DataExportFormat format) {
        this.format = format;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getValueSeparator() {
        return valueSeparator;
    }

    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public File getFile() {
        return new File(fileLocation, fileName);
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public enum Scope{
        GLOBAL,
        SELECTION
    }

    public enum Destination{
        FILE,
        CLIPBOARD
    }

    @Override
    protected DataExportInstructions clone() throws CloneNotSupportedException {
        return (DataExportInstructions) super.clone();
    }

    /****************************************
    *            JDOMExternalizable         *
    *****************************************/
    public void readExternal(Element element) throws InvalidDataException {
        createHeader = SettingsUtil.getBoolean(element, "create-header", createHeader);
        quoteValuesContainingSeparator = SettingsUtil.getBoolean(element, "quote-values-containing-separator", quoteValuesContainingSeparator);
        quoteAllValues = SettingsUtil.getBoolean(element, "quote-all-values", quoteAllValues);
        valueSeparator = SettingsUtil.getString(element, "value-separator", valueSeparator);
        fileName = SettingsUtil.getString(element, "file-name", fileName);
        fileLocation = SettingsUtil.getString(element, "file-location", fileLocation);
        scope = Scope.valueOf(SettingsUtil.getString(element, "scope", scope.name()));
        destination = Destination.valueOf(SettingsUtil.getString(element, "destination", destination.name()));
        format = DataExportFormat.valueOf(SettingsUtil.getString(element, "format", format.name()));
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        SettingsUtil.setBoolean(element, "create-header", createHeader);
        SettingsUtil.setBoolean(element, "quote-values-containing-separator", quoteValuesContainingSeparator);
        SettingsUtil.setBoolean(element, "quote-all-values", quoteAllValues);
        SettingsUtil.setString(element, "value-separator", valueSeparator);
        SettingsUtil.setString(element, "file-name", fileName);
        SettingsUtil.setString(element, "file-location", fileLocation);
        SettingsUtil.setString(element, "scope", scope.name());
        SettingsUtil.setString(element, "destination", destination.name());
        SettingsUtil.setString(element, "format", format.name());
    }
}
