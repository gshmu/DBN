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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.booleanAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBooleanAttribute;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ConnectionFilterSettings extends CompositeProjectConfiguration<ConnectionSettings, ConnectionFilterSettingsForm> implements ConnectionIdProvider {
    private final ObjectTypeFilterSettings objectTypeFilterSettings;
    private final ObjectNameFilterSettings objectNameFilterSettings;
    private boolean hideEmptySchemas = false;
    private boolean hidePseudoColumns = false;

    @EqualsAndHashCode.Exclude
    private final ConnectionSettings connectionSettings;

    private static final Filter<DBSchema> EMPTY_SCHEMAS_FILTER = schema -> !schema.isEmptySchema();
    private static final Filter<DBColumn> PSEUDO_COLUMNS_FILTER = column -> !column.isHidden();

    private final Latent<Filter<DBSchema>> schemaFilter = Latent.mutable(
            () -> hideEmptySchemas,
            () -> {
                Filter<DBObject> filter = getObjectNameFilterSettings().getFilter(DBObjectType.SCHEMA);
                if (filter == null) {
                    return hideEmptySchemas ? EMPTY_SCHEMAS_FILTER : null; // return null filter for optimization
                } else {
                    return schema -> {
                        if (hideEmptySchemas) {
                            return EMPTY_SCHEMAS_FILTER.accepts(schema) && filter.accepts(schema);
                        } else {
                            return filter.accepts(schema);
                        }
                    };
                }
            });

    private final Latent<Filter<DBColumn>> columnFilter = Latent.mutable(
        () -> hidePseudoColumns,
        () -> {
            Filter<DBObject> filter = getObjectNameFilterSettings().getFilter(DBObjectType.COLUMN);
            if (filter == null) {
                return PSEUDO_COLUMNS_FILTER;
            } else {
                return column -> {
                    if (hidePseudoColumns) {
                        return PSEUDO_COLUMNS_FILTER.accepts(column) && filter.accepts(column);
                    } else {
                        return filter.accepts(column);
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
        return new Configuration[] {
                objectTypeFilterSettings,
                objectNameFilterSettings};
    }

    @Override
    public void readConfiguration(Element element) {
        hideEmptySchemas = booleanAttribute(element, "hide-empty-schemas", hideEmptySchemas);
        hidePseudoColumns = booleanAttribute(element, "hide-pseudo-columns", hidePseudoColumns);
        super.readConfiguration(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        setBooleanAttribute(element, "hide-empty-schemas", hideEmptySchemas);
        setBooleanAttribute(element, "hide-pseudo-columns", hidePseudoColumns);
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
