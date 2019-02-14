package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class DatasetFilterImpl extends BasicConfiguration<DatasetFilterGroup, ConfigurationEditorForm> implements DatasetFilter {
    private DatasetFilterGroup filterGroup;
    private String id;
    private String name;
    private String error;
    private boolean isNew = true;
    private boolean isCustomNamed = false;
    private boolean isTemporary;
    private DatasetFilterType filterType;

    private DatasetFilterImpl(DatasetFilterGroup filterGroup, String name, String id, DatasetFilterType filterType) {
        super(filterGroup);
        this.filterGroup = filterGroup;
        this.name = name;
        this.id = id;
        this.filterType = filterType;
    }

    DatasetFilterImpl(DatasetFilterGroup filterGroup, String name, DatasetFilterType filterType) {
        this(filterGroup, name, UUID.randomUUID().toString(), filterType);
    }

    @Override
    @NotNull
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public void setName(String name) {
        this.name = StringUtil.first(name, 40, true);
    }

    @Override
    public ConnectionId getConnectionId() {
        return filterGroup.getConnectionId();
    }

    @Override
    public String getDatasetName() {
        return filterGroup.getDatasetName();
    }

    @Override
    public DatasetFilterGroup getFilterGroup() {
        return filterGroup;
    }

    public boolean isCustomNamed() {
        return isCustomNamed;
    }

    public void setCustomNamed(boolean customNamed) {
        this.isCustomNamed = customNamed;
    }

    public abstract void generateName();

    @Override
    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean temporary) {
        isTemporary = temporary;
    }

    @Override
    public DatasetFilterType getFilterType() {
        return filterType;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof DatasetFilter) {
            DatasetFilter remote = (DatasetFilter) obj;
            return remote.getFilterGroup().equals(filterGroup) &&
                   remote.getId().equals(id);
        }
        return false;
    }

    @Nullable
    public DBDataset lookupDataset() {
        return filterGroup.lookupDataset();
    }

    @Override
    public void apply() throws ConfigurationException {
        super.apply();
        isTemporary = false;
        isNew = false;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    public void readConfiguration(Element element) {
        id = element.getAttributeValue("id");
        name = element.getAttributeValue("name");
        isTemporary = Boolean.parseBoolean(element.getAttributeValue("temporary"));
        isCustomNamed = Boolean.parseBoolean(element.getAttributeValue("custom-name"));
        isNew = false;
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("id", id);
        element.setAttribute("name", name);
        element.setAttribute("temporary", Boolean.toString(isTemporary));
        element.setAttribute("custom-name", Boolean.toString(isCustomNamed));
    }

}
