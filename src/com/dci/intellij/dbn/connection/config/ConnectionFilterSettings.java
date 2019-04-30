package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionIdProvider;
import com.dci.intellij.dbn.connection.config.ui.ConnectionFilterSettingsForm;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterSettings;
import com.dci.intellij.dbn.object.filter.type.ObjectTypeFilterSettings;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBooleanAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBooleanAttribute;

public class ConnectionFilterSettings extends CompositeProjectConfiguration<ConnectionSettings, ConnectionFilterSettingsForm> implements ConnectionIdProvider {
    private ObjectTypeFilterSettings objectTypeFilterSettings;
    private ObjectNameFilterSettings objectNameFilterSettings;
    private boolean hideEmptySchemas = false;
    private boolean hideHiddenColumns = true;
    private ConnectionSettings connectionSettings;

    private static final Filter<DBSchema> EMPTY_SCHEMAS_FILTER = schema -> !schema.isEmptySchema();
    private static final Filter<DBColumn> EMPTY_HIDDEN_COLUMN_FILTER = column -> !column.isHidden();

    private Latent<Filter<DBSchema>> schemaFilter = Latent.mutable(
            () -> hideEmptySchemas,
            () -> {
                Filter<DBObject> filter = objectNameFilterSettings.getFilter(DBObjectType.SCHEMA);
                if (filter == null) {
                    return EMPTY_SCHEMAS_FILTER;
                } else {
                    return new Filter<DBSchema>() {
                        @Override
                        public int hashCode() {
                            return filter.hashCode() + EMPTY_SCHEMAS_FILTER.hashCode();
                        }

                        @Override
                        public boolean accepts(DBSchema schema) {
                            return EMPTY_SCHEMAS_FILTER.accepts(schema) && filter.accepts(schema);
                        }
                    };
                }
            });

    private Latent<Filter<DBColumn>> columnFilter = Latent.mutable(
        () -> hideHiddenColumns,
        () -> {
            Filter<DBObject> filter = objectNameFilterSettings.getFilter(DBObjectType.COLUMN);
            if (filter == null) {
                return EMPTY_HIDDEN_COLUMN_FILTER;
            } else {
                return new Filter<DBColumn>() {
                    @Override
                    public int hashCode() {
                        return filter.hashCode() + EMPTY_HIDDEN_COLUMN_FILTER.hashCode();
                    }

                    @Override
                    public boolean accepts(DBColumn column) {
                        return EMPTY_HIDDEN_COLUMN_FILTER.accepts(column) && filter.accepts(column);
                    }
                };
            }
        });

    ConnectionFilterSettings(ConnectionSettings connectionSettings) {
        super(connectionSettings.getProject());
        this.connectionSettings = connectionSettings;
        objectTypeFilterSettings = new ObjectTypeFilterSettings(this, connectionSettings);
        objectNameFilterSettings = new ObjectNameFilterSettings(this, connectionSettings);
    }

    public boolean isHideEmptySchemas() {
        return hideEmptySchemas;
    }

    public void setHideEmptySchemas(boolean hideEmptySchemas) {
        this.hideEmptySchemas = hideEmptySchemas;
    }

    public boolean isHideHiddenColumns() {
        return hideHiddenColumns;
    }

    public void setHideHiddenColumns(boolean hideHiddenColumns) {
        this.hideHiddenColumns = hideHiddenColumns;
    }

    public ConnectionId getConnectionId() {
        return connectionSettings.getConnectionId();
    }

    @Override
    public String getDisplayName() {
        return "Connection Filter Settings";
    }

    @Override
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
    @NotNull
    @Override
    public ConnectionFilterSettingsForm createConfigurationEditor() {
        return new ConnectionFilterSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "object-filters";
    }

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {objectTypeFilterSettings, objectNameFilterSettings};
    }

    @Override
    public void readConfiguration(Element element) {
        hideEmptySchemas = getBooleanAttribute(element, "hide-empty-schemas", hideEmptySchemas);
        hideHiddenColumns = getBooleanAttribute(element, "hide-hidden-columns", hideHiddenColumns);
        super.readConfiguration(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        setBooleanAttribute(element, "hide-empty-schemas", hideEmptySchemas);
        setBooleanAttribute(element, "hide-hidden-columns", hideHiddenColumns);
        super.writeConfiguration(element);
    }

    @Nullable
    public Filter<? extends DBObject> getNameFilter(DBObjectType objectType) {
        return
            objectType == DBObjectType.SCHEMA ? schemaFilter.get() :
            objectType == DBObjectType.COLUMN ? columnFilter.get() :
                objectNameFilterSettings.getFilter(objectType);
    }
}
