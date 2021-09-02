package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@Setter
public abstract class DatasetFilterImpl extends BasicConfiguration<DatasetFilterGroup, ConfigurationEditorForm> implements DatasetFilter {
    private final DatasetFilterGroup filterGroup;
    private final DatasetFilterType filterType;

    private String id;
    private String name;
    private String error;
    private boolean temporary = false;
    private boolean customNamed = false;
    private boolean persisted = false;


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
    public String getDisplayName() {
        return name;
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

    public abstract void generateName();

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
        temporary = false;
        persisted = true;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    public void readConfiguration(Element element) {
        id = element.getAttributeValue("id");
        name = element.getAttributeValue("name");
        temporary = Boolean.parseBoolean(element.getAttributeValue("temporary"));
        customNamed = Boolean.parseBoolean(element.getAttributeValue("custom-name"));
        persisted = true;
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("id", id);
        element.setAttribute("name", name);
        element.setAttribute("temporary", Boolean.toString(temporary));
        element.setAttribute("custom-name", Boolean.toString(customNamed));
    }

}
