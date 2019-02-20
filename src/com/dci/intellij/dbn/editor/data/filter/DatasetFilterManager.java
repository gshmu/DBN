package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetFilterDialog;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@State(
    name = DatasetFilterManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatasetFilterManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DatasetFilterManager";

    public static final DatasetFilter EMPTY_FILTER = new DatasetEmptyFilter();
    private Map<ConnectionId, Map<String, DatasetFilterGroup>> filters = new HashMap<>();

    private DatasetFilterManager(Project project) {
        super(project);
    }

    public void switchActiveFilter(DBDataset dataset, DatasetFilter filter){
        Project project = dataset.getProject();
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
        if (activeFilter != filter) {
            filterManager.setActiveFilter(dataset, filter);

        }
    }

    public int openFiltersDialog(DBDataset dataset, boolean isAutomaticPrompt, boolean createNewFilter, DatasetFilterType defaultFilterType) {
        DatasetFilterDialog filterDialog = new DatasetFilterDialog(dataset, isAutomaticPrompt, createNewFilter, defaultFilterType);
        filterDialog.show();
        return filterDialog.getExitCode();
    }

    public void createBasicFilter(DBDataset dataset, String columnName, Object columnValue, ConditionOperator operator, boolean interactive) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        DatasetBasicFilter filter = filterGroup.createBasicFilter(columnName, columnValue, operator, interactive);

        if (interactive) {
            DatasetFilterDialog filterDialog = new DatasetFilterDialog(dataset, filter);
            filterDialog.show();
        } else {
            filter.setNew(false);
            filter.setTemporary(true);
            setActiveFilter(dataset, filter);
            DatasetEditorManager.getInstance(getProject()).reloadEditorData(dataset);
        }
    }

    public void createBasicFilter(DBDataset dataset, String columnName, Object columnValue, ConditionOperator operator) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        DatasetBasicFilter filter = filterGroup.createBasicFilter(columnName, columnValue, operator);

        filter.setNew(false);
        filter.setTemporary(true);
        setActiveFilter(dataset, filter);
        DatasetEditorManager.getInstance(getProject()).reloadEditorData(dataset);
    }

    public void createBasicFilter(DatasetFilterInput filterInput) {
        DBDataset dataset = filterInput.getDataset();
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        DatasetBasicFilter filter = null;

        for (DBColumn column : filterInput.getColumns()) {
            Object value = filterInput.getColumnValue(column);
            if (filter == null) {
                filter = filterGroup.createBasicFilter(column.getName(), value, ConditionOperator.EQUAL);
            } else {
                filter.addCondition(column.getName(), value, ConditionOperator.EQUAL);
            }
        }

        filter.setNew(false);
        filter.setTemporary(true);
        setActiveFilter(dataset, filter);
        DatasetEditorManager.getInstance(getProject()).reloadEditorData(dataset);

    }

    public void addConditionToFilter(DatasetBasicFilter filter, DBDataset dataset, ColumnInfo columnInfo, Object value, boolean interactive) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        DatasetBasicFilterCondition condition = interactive ?
                new DatasetBasicFilterCondition(filter, columnInfo.getName(), value, ConditionOperator.EQUAL, true) :
                new DatasetBasicFilterCondition(filter, columnInfo.getName(), value, null);

        filter.addCondition(condition);
        filter.generateName();
        filterGroup.setActiveFilter(filter);
        if (interactive) {
            DatasetFilterDialog filterDialog = new DatasetFilterDialog(dataset, false, false, DatasetFilterType.NONE);
            filterDialog.show();
        } else {
            DatasetEditorManager.getInstance(getProject()).reloadEditorData(dataset);
        }

    }



    public DatasetFilter getActiveFilter(DBDataset dataset) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        return filterGroup.getActiveFilter();
    }

    public void setActiveFilter(DBDataset dataset, DatasetFilter filter) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        filterGroup.setActiveFilter(filter);
    }

    private void addFilterGroup(DatasetFilterGroup filterGroup) {
        ConnectionId connectionId = filterGroup.getConnectionId();
        String datasetName = filterGroup.getDatasetName();
        Map<String, DatasetFilterGroup> connectionFilters =
                filters.computeIfAbsent(connectionId, k -> new HashMap<>());

        connectionFilters.put(datasetName, filterGroup);
    }

    public DatasetFilterGroup getFilterGroup(DBDataset dataset) {
        ConnectionHandler connectionHandler = Failsafe.get(dataset.getConnectionHandler());
        ConnectionId connectionId = connectionHandler.getConnectionId();
        String datasetName = dataset.getQualifiedName();
        return getFilterGroup(connectionId, datasetName);
    }

    public DatasetFilterGroup getFilterGroup(DatasetFilter filter) {
        ConnectionId connectionId = filter.getConnectionId();
        String datasetName = filter.getDatasetName();
        return getFilterGroup(connectionId, datasetName);
    }

    @NotNull
    public DatasetFilterGroup getFilterGroup(ConnectionId connectionId, String datasetName) {
        Map<String, DatasetFilterGroup> filterGroups = filters.computeIfAbsent(connectionId, k -> new HashMap<>());
        return filterGroups.computeIfAbsent(datasetName, k -> new DatasetFilterGroup(getProject(), connectionId, k));
    }

    public static DatasetFilterManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatasetFilterManager.class);
    }

    /***************************************
    *            ProjectComponent           *
    ****************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }
    @Override
    public void dispose() {
        super.dispose();
        filters.clear();
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        for (ConnectionId connectionId : filters.keySet()){
            ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
            if (connectionManager.getConnectionHandler(connectionId) != null) {
                Map<String, DatasetFilterGroup> filterLists = filters.get(connectionId);
                for (String datasetName : filterLists.keySet()) {
                    DatasetFilterGroup filterGroup = filterLists.get(datasetName);
                    Element filterListElement = new Element("filter-actions");
                    filterGroup.writeConfiguration(filterListElement);
                    element.addContent(filterListElement);
                }
            }
        }
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        for (Object object : element.getChildren()) {
            Element filterListElement = (Element) object;
            DatasetFilterGroup filterGroup = new DatasetFilterGroup(getProject());
            filterGroup.readConfiguration(filterListElement);
            addFilterGroup(filterGroup);
        }
    }

}
