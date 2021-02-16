package com.dci.intellij.dbn.object.filter.type;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ConnectionRef;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.type.ui.ObjectTypeFilterSettingsForm;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ObjectTypeFilterSettings extends BasicProjectConfiguration<ProjectConfiguration, ObjectTypeFilterSettingsForm> {
    private final Latent<List<ObjectTypeFilterSetting>> objectTypeFilterSettings = Latent.basic(() -> {
        List<ObjectTypeFilterSetting> objectTypeFilterSettings = new ArrayList<>();
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.SCHEMA));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.USER));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.ROLE));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.PRIVILEGE));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.CHARSET));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.TABLE));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.VIEW));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.MATERIALIZED_VIEW));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.NESTED_TABLE));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.COLUMN));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.INDEX));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.CONSTRAINT));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.DATASET_TRIGGER));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.DATABASE_TRIGGER));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.SYNONYM));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.SEQUENCE));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.PROCEDURE));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.FUNCTION));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.PACKAGE));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.TYPE));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.TYPE_ATTRIBUTE));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.ARGUMENT));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.DIMENSION));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.CLUSTER));
        objectTypeFilterSettings.add(new ObjectTypeFilterSetting(ObjectTypeFilterSettings.this, DBObjectType.DBLINK));
        return objectTypeFilterSettings;
    });

    private final BooleanSetting useMasterSettings = new BooleanSetting("use-master-settings", true);
    private final ConnectionRef connectionRef;

    public ObjectTypeFilterSettings(ProjectConfiguration parent, @Nullable ConnectionRef connectionRef) {
        super(parent);
        this.connectionRef = connectionRef;
    }

    public ObjectTypeFilterSettings getMasterSettings() {
        if (isProjectLevel()) { // is project level
            return null;
        } else {
            DatabaseBrowserSettings databaseBrowserSettings = DatabaseBrowserSettings.getInstance(getProject());
            return databaseBrowserSettings.getFilterSettings().getObjectTypeFilterSettings();
        }
    }

    private boolean isProjectLevel() {
        return connectionRef == null;
    }

    public ConnectionId getConnectionId() {
        return connectionRef == null ? null : connectionRef.getConnectionId();
    }

    public BooleanSetting getUseMasterSettings() {
        return useMasterSettings;
    }

    @NotNull
    @Override
    public ObjectTypeFilterSettingsForm createConfigurationEditor() {
        return new ObjectTypeFilterSettingsForm(this);
    }

    public Filter<BrowserTreeNode> getElementFilter() {
        return elementFilter;
    }

    public Filter<DBObjectType> getTypeFilter() {
        return typeFilter;
    }

    private final Filter<BrowserTreeNode> elementFilter = treeNode -> {
        if (treeNode == null) {
            return false;
        }

        if (treeNode instanceof DBObject) {
            DBObject object = (DBObject) treeNode;
            DBObjectType objectType = object.getObjectType();
            return isVisible(objectType);
        }

        if (treeNode instanceof DBObjectList) {
            DBObjectList objectList = (DBObjectList) treeNode;
            return isVisible(objectList.getObjectType());
        }

        return true;
    };

    private final Filter<DBObjectType> typeFilter = objectType -> objectType != null && isVisible(objectType);

    private boolean isVisible(DBObjectType objectType) {
        return isProjectLevel() ?
            isSelected(objectType) :
            useMasterSettings.value() ?
                    getMasterSettings().isSelected(objectType) :
                    getMasterSettings().isSelected(objectType) && isSelected(objectType);
    }

    private boolean isSelected(DBObjectType objectType) {
        ObjectTypeFilterSetting objectTypeEntry = getObjectTypeEntry(objectType);
        return objectTypeEntry == null || objectTypeEntry.isSelected();
    }

    private void setVisible(DBObjectType objectType, boolean visible) {
        ObjectTypeFilterSetting objectTypeEntry = getObjectTypeEntry(objectType);
        if (objectTypeEntry != null) {
            objectTypeEntry.setSelected(visible);
        }
    }


    private ObjectTypeFilterSetting getObjectTypeEntry(DBObjectType objectType) {
        for (ObjectTypeFilterSetting objectTypeEntry : getSettings()) {
            DBObjectType visibleObjectType = objectTypeEntry.getObjectType();
            if (visibleObjectType == objectType || objectType.isInheriting(visibleObjectType)) {
                return objectTypeEntry;
            }
        }
        return null;
    }

    public List<ObjectTypeFilterSetting> getSettings() {
        return objectTypeFilterSettings.get();
    }

    public boolean isSelected(ObjectTypeFilterSetting objectFilterEntry) {
        for (ObjectTypeFilterSetting entry : getSettings()) {
            if (entry.equals(objectFilterEntry)) {
                return entry.isSelected();
            }
        }
        return false;
    }


    @Override
    public String getConfigElementName() {
        return "object-type-filter";
    }

    @Override
    public void readConfiguration(Element element) {
        useMasterSettings.readConfigurationAttribute(element);
        for (Object o : element.getChildren()) {
            Element child = (Element) o;
            String typeName = child.getAttributeValue("name");
            DBObjectType objectType = DBObjectType.get(typeName);
            if (objectType != null) {
                boolean enabled = Boolean.parseBoolean(child.getAttributeValue("enabled"));
                setVisible(objectType, enabled);
            }
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        if (!isProjectLevel()) {
            useMasterSettings.writeConfigurationAttribute(element);
        }

        for (ObjectTypeFilterSetting objectTypeEntry : getSettings()) {
            Element child = new Element("object-type");
            child.setAttribute("name", objectTypeEntry.getObjectType().name());
            child.setAttribute("enabled", Boolean.toString(objectTypeEntry.isSelected()));
            element.addContent(child);
        }
    }
}
