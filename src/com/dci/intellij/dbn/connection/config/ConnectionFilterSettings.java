package com.dci.intellij.dbn.connection.config;

import org.jdom.Element;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.config.ui.ConnectionFilterSettingsForm;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterSettings;
import com.dci.intellij.dbn.object.filter.type.ObjectTypeFilterSettings;
import com.intellij.openapi.project.Project;

public class ConnectionFilterSettings extends CompositeProjectConfiguration<ConnectionFilterSettingsForm> {
    private ObjectTypeFilterSettings objectTypeFilterSettings;
    private ObjectNameFilterSettings objectNameFilterSettings;
    private boolean hideEmptySchemas = false;
    private ConnectionSettings connectionSettings;

    private static final Filter<DBSchema> EMPTY_SCHEMAS_FILTER = new Filter<DBSchema>() {
        @Override
        public boolean accepts(DBSchema schema) {
            return !schema.isEmptySchema();
        }
    };

    private transient Filter<DBSchema> cachedSchemaFilter;

    public ConnectionFilterSettings(ConnectionSettings connectionSettings) {
        super(connectionSettings.getProject());
        this.connectionSettings = connectionSettings;
        Project project = connectionSettings.getProject();
        DatabaseBrowserSettings databaseBrowserSettings = DatabaseBrowserSettings.getInstance(project);
        ObjectTypeFilterSettings master = databaseBrowserSettings.getFilterSettings().getObjectTypeFilterSettings();
        objectTypeFilterSettings = new ObjectTypeFilterSettings(project, master);
        objectNameFilterSettings = new ObjectNameFilterSettings(project);
    }

    public boolean isHideEmptySchemas() {
        return hideEmptySchemas;
    }

    public void setHideEmptySchemas(boolean hideEmptySchemas) {
        this.hideEmptySchemas = hideEmptySchemas;
    }

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public String getDisplayName() {
        return "Connection Filter Settings";
    }

    public String getHelpTopic() {
        return "connectionFilterSettings";
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public ObjectTypeFilterSettings getObjectTypeFilterSettings() {
        return objectTypeFilterSettings;
    }

    public ObjectNameFilterSettings getObjectNameFilterSettings() {
        return objectNameFilterSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    public ConnectionFilterSettingsForm createConfigurationEditor() {
        return new ConnectionFilterSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "object-filters";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {objectTypeFilterSettings, objectNameFilterSettings};
    }

    @Override
    public void readConfiguration(Element element) {
        hideEmptySchemas = getBooleanAttribute(element, "hide-empty-schemas", hideEmptySchemas);
        super.readConfiguration(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        setBooleanAttribute(element, "hide-empty-schemas", hideEmptySchemas);
        super.writeConfiguration(element);
    }

    @Override
    protected void onApply() {
        super.onApply();
        cachedSchemaFilter = null;
    }

    public Filter<? extends DBObject> getNameFilter(DBObjectType objectType) {
        final Filter<DBObject> filter = objectNameFilterSettings.getFilter(objectType);
        if (objectType == DBObjectType.SCHEMA) {
            if (hideEmptySchemas) {
                if (filter == null) {
                    return EMPTY_SCHEMAS_FILTER;
                } else {
                    if (cachedSchemaFilter == null) {
                        cachedSchemaFilter = new Filter<DBSchema>() {
                            @Override
                            public int hashCode() {
                                return filter.hashCode() + super.hashCode();
                            }

                            @Override
                            public boolean accepts(DBSchema schema) {
                                return EMPTY_SCHEMAS_FILTER.accepts(schema) && filter.accepts(schema);
                            }
                        };

                    }
                    return cachedSchemaFilter;
                }
            }
        }
        return filter;
    }
}
