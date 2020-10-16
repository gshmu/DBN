package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.ui.ListUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetFilterForm;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatasetFilterGroup extends BasicProjectConfiguration<ProjectConfiguration, DatasetFilterForm> implements ListModel {
    private ConnectionId connectionId;
    private String datasetName;
    private DatasetFilter activeFilter;
    private final List<DatasetFilter> filters = new ArrayList<>();
    private final List<DatasetFilter> filtersTemp = new ArrayList<>();

    private boolean changed;
    private final Set<ListDataListener> listeners = new HashSet<>();


    public DatasetFilterGroup(@NotNull Project project) {
        super(project);
    }

    public DatasetFilterGroup(@NotNull Project project, ConnectionId connectionId, String datasetName) {
        super(project);
        this.connectionId = connectionId;
        this.datasetName = datasetName;
    }

    public DatasetBasicFilter createBasicFilter(boolean interactive) {
        String name = createFilterName("Filter");
        DatasetBasicFilter filter = new DatasetBasicFilter(this, name);
        filter.addCondition(new DatasetBasicFilterCondition(filter, null, null, ConditionOperator.EQUAL));
        initChange();
        addFilter(filter, interactive);
        return filter;
    }

    @NotNull
    DatasetBasicFilter createBasicFilter(String columnName, Object columnValue, ConditionOperator operator, boolean interactive) {
        String name = createFilterName("Filter");
        DatasetBasicFilter filter = new DatasetBasicFilter(this, name);
        if (columnValue != null) filter.setName(columnValue.toString());
        DatasetBasicFilterCondition condition = interactive ?
                new DatasetBasicFilterCondition(filter, columnName, columnValue, operator, true) :
                new DatasetBasicFilterCondition(filter, columnName, columnValue, operator);
        filter.addCondition(condition);

        if (interactive) initChange();
        addFilter(filter, interactive);
        return filter;
    }

    @NotNull
    DatasetBasicFilter createBasicFilter(String columnName, Object columnValue, ConditionOperator operator) {
        String name = createFilterName("Filter");
        DatasetBasicFilter filter = new DatasetBasicFilter(this, name);
        if (columnValue != null) filter.setName(columnValue.toString());
        DatasetBasicFilterCondition condition = new DatasetBasicFilterCondition(filter, columnName, columnValue, operator, true);
        filter.addCondition(condition);
        addFilter(filter, false);
        return filter;
    }


    public String createFilterName(String baseName) {
        while (lookupFilter(baseName) != null) {
            baseName = NamingUtil.getNextNumberedName(baseName, true);
        }
        return baseName;
    }

    private Object lookupFilter(String name) {
        for (DatasetFilter filter : getFilters()) {
            if (filter.getName().equals(name)) {
                return filter;
            }
        }
        return null;
    }

    public DatasetCustomFilter createCustomFilter(boolean interactive) {
        String name = createFilterName("Filter");
        DatasetCustomFilter filter = new DatasetCustomFilter(this, name);
        initChange();
        addFilter(filter, interactive);
        return filter;
    }

    public DatasetFilter getFilter(String filterId) {
        for (DatasetFilter filter : filters) {
            if (filter.getId().equals(filterId)) {
                return filter;
            }
        }
        if (filterId.equals(DatasetFilterManager.EMPTY_FILTER.getId())) {
            return DatasetFilterManager.EMPTY_FILTER;            
        }
        return null;
    }


    public DatasetFilter getActiveFilter() {
        return activeFilter;
    }

    public void setActiveFilter(DatasetFilter activeFilter) {
        this.activeFilter = activeFilter;
    }

    public void deleteFilter(DatasetFilter filter) {
        initChange();
        int index = getFilters().indexOf(filter);
        getFilters().remove(index);
        filter.disposeUIResources();
        ListUtil.notifyListDataListeners(this, listeners, index, index, ListDataEvent.INTERVAL_REMOVED);

    }

    private void addFilter(DatasetFilter filter, boolean interactive) {
        int index = getFilters().size();
        if (!interactive) {
            // allow only one temporary filter
            clearTemporaryFilters();
        }
        getFilters().add(filter);
        if (interactive) {
            ListUtil.notifyListDataListeners(this, listeners, index, index, ListDataEvent.INTERVAL_ADDED);
        }
    }

    private void clearTemporaryFilters() {
        getFilters().removeIf(DatasetFilter::isTemporary);
    }

    public void moveFilterUp(DatasetFilter filter) {
        initChange();
        int index = getFilters().indexOf(filter);
        if (index > 0) {
            getFilters().remove(filter);
            getFilters().add(index-1, filter);
            ListUtil.notifyListDataListeners(this, listeners, index-1, index, ListDataEvent.CONTENTS_CHANGED);
        }
    }

    public void moveFilterDown(DatasetFilter filter) {
        initChange();
        int index = getFilters().indexOf(filter);
        if (index < getFilters().size()-1) {
            getFilters().remove(filter);
            getFilters().add(index + 1, filter);
            ListUtil.notifyListDataListeners(this, listeners, index, index + 1, ListDataEvent.CONTENTS_CHANGED);
        }

    }


    public ConnectionId getConnectionId() {
        return connectionId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    @NotNull
    public DBDataset lookupDataset() {
        Project project = getProject();
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(connectionId);
        if (connectionHandler != null) {
            int index = datasetName.lastIndexOf('.');
            String schemaName = datasetName.substring(0, index);
            DBSchema schema = connectionHandler.getObjectBundle().getSchema(schemaName);
            if (schema != null) {
                String name = datasetName.substring(index + 1);
                DBDataset dataset = schema.getDataset(name);
                return Failsafe.nn(dataset);
            }
        }
        throw AlreadyDisposedException.INSTANCE;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof DatasetFilterGroup) {
            DatasetFilterGroup remote = (DatasetFilterGroup) obj;
            return remote.connectionId.equals(connectionId) &&
                    remote.datasetName.equals(datasetName);
        }
        return false;
    }

    private void initChange() {
        if (!changed) {
            filtersTemp.addAll(filters);
            changed = true;
        }
    }

    /*************************************************
    *                      Settings                  *
    *************************************************/
    @Override
    public String getDisplayName() {
        return null;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    public void apply() throws ConfigurationException {
        if (changed) {
            filters.clear();
            filters.addAll(filtersTemp);
            filtersTemp.clear();
            changed = false;
            if (!filters.contains(activeFilter)) {
                activeFilter = null;
            }
        }
        for (DatasetFilter filter : filters) {
            filter.apply();
        }
    }

    @Override
    public void reset() {
        if (changed) {
            filtersTemp.clear();
            changed = false;
        }
        for (DatasetFilter filter : filters) {
            filter.reset();
        }
    }

    @Override
    public void disposeUIResources() {
        for (DatasetFilter filter :filters) {
            filter.disposeUIResources();
        }
        for (DatasetFilter filter :filtersTemp) {
            filter.disposeUIResources();
        }
        listeners.clear();
        super.disposeUIResources();
    }

   @Override
   @NotNull
   public DatasetFilterForm createConfigurationEditor() {
       return new DatasetFilterForm(this, lookupDataset());
   }

    @Override
    public void readConfiguration(Element element) {
        connectionId = ConnectionId.get(element.getAttributeValue("connection-id"));
        datasetName = element.getAttributeValue("dataset");
        for (Object object : element.getChildren()){
            Element filterElement = (Element) object;
            String type = filterElement.getAttributeValue("type");
            if (type.equals("basic")) {
                DatasetFilter filter = new DatasetBasicFilter(this, null);
                filters.add(filter);
                filter.readConfiguration(filterElement);
            } else if (type.equals("custom")) {
                DatasetFilter filter = new DatasetCustomFilter(this, null);
                filters.add(filter);
                filter.readConfiguration(filterElement);
            }
        }
        String activeFilterId = element.getAttributeValue("active-filter-id");
        activeFilter = getFilter(activeFilterId);
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("connection-id", connectionId.id());
        element.setAttribute("dataset", datasetName);
        for (DatasetFilter filter : filters) {
            Element filterElement = new Element("filter");
            filter.writeConfiguration(filterElement);
            element.addContent(filterElement);
        }
        element.setAttribute("active-filter-id", activeFilter == null ? "" : activeFilter.getId());
    }

   /*************************************************
    *                     ListModel                 *
    *************************************************/
   public List<DatasetFilter> getFilters() {
        return changed ? filtersTemp : filters;
   }

   @Override
   public int getSize() {
        return getFilters().size();
    }

    @Override
    public Object getElementAt(int index) {
        return getFilters().get(index);
    }

    @Override
    public void addListDataListener(ListDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListDataListener(ListDataListener listener) {
        listeners.remove(listener);
    }
}
