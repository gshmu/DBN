package com.dci.intellij.dbn.data.export;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class DataExportInstructions extends SettingsUtil implements PersistentStateComponent<Element>, Cloneable {
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

    /***********************************************
     *            PersistentStateComponent         *
     ***********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("export-instructions");
        setBoolean(element, "create-header", createHeader);
        setBoolean(element, "quote-values-containing-separator", quoteValuesContainingSeparator);
        setBoolean(element, "quote-all-values", quoteAllValues);
        setString(element, "value-separator", valueSeparator);
        setString(element, "file-name", fileName);
        setString(element, "file-location", fileLocation);
        setString(element, "scope", scope.name());
        setString(element, "destination", destination.name());
        setString(element, "format", format.name());
        return element;
    }

    @Override
    public void loadState(Element element) {
        if (element != null) {
            createHeader = getBoolean(element, "create-header", createHeader);
            quoteValuesContainingSeparator = getBoolean(element, "quote-values-containing-separator", quoteValuesContainingSeparator);
            quoteAllValues = getBoolean(element, "quote-all-values", quoteAllValues);
            valueSeparator = getString(element, "value-separator", valueSeparator);
            fileName = getString(element, "file-name", fileName);
            fileLocation = getString(element, "file-location", fileLocation);
            scope = Scope.valueOf(getString(element, "scope", scope.name()));
            destination = Destination.valueOf(getString(element, "destination", destination.name()));
            format = DataExportFormat.valueOf(getString(element, "format", format.name()));
        }
    }
}
