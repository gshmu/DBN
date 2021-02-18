package com.dci.intellij.dbn.navigation.options;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.ui.list.Selectable;
import com.dci.intellij.dbn.navigation.options.ui.ObjectsLookupSettingsForm;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.options.ConfigurationException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ObjectsLookupSettings extends BasicProjectConfiguration<NavigationSettings, ObjectsLookupSettingsForm> {
    private final Latent<List<ObjectTypeEntry>> lookupObjectTypes = Latent.basic(() -> createLookupObjectTypes());

    private final BooleanSetting forceDatabaseLoad = new BooleanSetting("force-database-load", false);
    private final BooleanSetting promptConnectionSelection = new BooleanSetting("prompt-connection-selection", true);
    private final BooleanSetting promptSchemaSelection = new BooleanSetting("prompt-schema-selection", true);
    private Set<DBObjectType> fastLookupObjectTypes;

    public ObjectsLookupSettings(NavigationSettings parent) {
        super(parent);
    }

    @NotNull
    @Override
    public ObjectsLookupSettingsForm createConfigurationEditor() {
        return new ObjectsLookupSettingsForm(this);
    }

    @Override
    public void apply() throws ConfigurationException {
        super.apply();
        fastLookupObjectTypes = null;
    }

    public boolean isEnabled(DBObjectType objectType) {
        if (fastLookupObjectTypes == null) {
            fastLookupObjectTypes = EnumSet.noneOf(DBObjectType.class);
            for (ObjectTypeEntry objectTypeEntry : getLookupObjectTypes()) {
                if (objectTypeEntry.isSelected()) {
                    fastLookupObjectTypes.add(objectTypeEntry.getObjectType());
                }
            }
        }
        return fastLookupObjectTypes.contains(objectType);
    }

    public BooleanSetting getForceDatabaseLoad() {
        return forceDatabaseLoad;
    }

    public BooleanSetting getPromptConnectionSelection() {
        return promptConnectionSelection;
    }

    public BooleanSetting getPromptSchemaSelection() {
        return promptSchemaSelection;
    }

    private ObjectTypeEntry getObjectTypeEntry(DBObjectType objectType) {
        for (ObjectTypeEntry objectTypeEntry : getLookupObjectTypes()) {
            DBObjectType visibleObjectType = objectTypeEntry.getObjectType();
            if (visibleObjectType == objectType || objectType.isInheriting(visibleObjectType)) {
                return objectTypeEntry;
            }
        }
        return null;
    }

    public List<ObjectTypeEntry> getLookupObjectTypes() {
        return lookupObjectTypes.get();
    }

    private List<ObjectTypeEntry> createLookupObjectTypes() {
        List<ObjectTypeEntry> lookupObjectTypes = new ArrayList<ObjectTypeEntry>();
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.SCHEMA, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.USER, false));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.ROLE, false));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.PRIVILEGE, false));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.CHARSET, false));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.TABLE, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.VIEW, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.MATERIALIZED_VIEW, true));
        //lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.NESTED_TABLE, false));
        //lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.COLUMN, false));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.INDEX, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.CONSTRAINT, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.DATASET_TRIGGER, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.DATABASE_TRIGGER, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.SYNONYM, false));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.SEQUENCE, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.PROCEDURE, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.FUNCTION, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.PACKAGE, true));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.TYPE, true));
        //lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.TYPE_ATTRIBUTE, false));
        //lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.ARGUMENT, false));

        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.DIMENSION, false));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.CLUSTER, false));
        lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.DBLINK, true));
        return lookupObjectTypes;
    }


    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    public String getConfigElementName() {
        return "lookup-filters";
    }

    @Override
    public void readConfiguration(Element element) {
        Element visibleObjectsElement = element.getChild("lookup-objects");
        for (Object o : visibleObjectsElement.getChildren()) {
            Element child = (Element) o;
            String typeName = child.getAttributeValue("name");
            DBObjectType objectType = DBObjectType.get(typeName);
            if (objectType != null) {
                boolean enabled = Boolean.parseBoolean(child.getAttributeValue("enabled"));
                ObjectTypeEntry objectTypeEntry = getObjectTypeEntry(objectType);
                if (objectTypeEntry != null) {
                    objectTypeEntry.setSelected(enabled);
                }
            }
        }
        forceDatabaseLoad.readConfiguration(element);
        promptConnectionSelection.readConfiguration(element);
        promptSchemaSelection.readConfiguration(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        Element visibleObjectsElement = new Element("lookup-objects");
        element.addContent(visibleObjectsElement);

        for (ObjectTypeEntry objectTypeEntry : getLookupObjectTypes()) {
            Element child = new Element("object-type");
            child.setAttribute("name", objectTypeEntry.getName());
            child.setAttribute("enabled", Boolean.toString(objectTypeEntry.isSelected()));
            visibleObjectsElement.addContent(child);
        }
        forceDatabaseLoad.writeConfiguration(element);
        promptConnectionSelection.writeConfiguration(element);
        promptSchemaSelection.writeConfiguration(element);
    }
    
    private static class ObjectTypeEntry implements Selectable<ObjectTypeEntry> {
        private final DBObjectType objectType;
        private boolean enabled = true;

        private ObjectTypeEntry(DBObjectType objectType) {
            this.objectType = objectType;
        }

        private ObjectTypeEntry(DBObjectType objectType, boolean enabled) {
            this.objectType = objectType;
            this.enabled = enabled;
        }

        public DBObjectType getObjectType() {
            return objectType;
        }

        @Override
        public Icon getIcon() {
            return objectType.getIcon();
        }

        @Override
        public String getName() {
            return objectType.getName().toUpperCase();
        }

        @Override
        public String getError() {
            return null;
        }

        @Override
        public boolean isSelected() {
            return enabled;
        }

        @Override
        public boolean isMasterSelected() {
            return true;
        }

        @Override
        public void setSelected(boolean selected) {
            this.enabled = selected;
        }

        @Override
        public int compareTo(ObjectTypeEntry remote) {
            return objectType.compareTo(remote.objectType);
        }
    }
}
