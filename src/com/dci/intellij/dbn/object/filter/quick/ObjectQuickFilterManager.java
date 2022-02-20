package com.dci.intellij.dbn.object.filter.quick;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.quick.ui.ObjectQuickFilterDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.var;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;

@State(
    name = ObjectQuickFilterManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ObjectQuickFilterManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.ObjectQuickFilterManager";

    private final Map<ObjectQuickFilterKey, ObjectQuickFilter<?>> quickFilters = new HashMap<>();
    private ConditionOperator lastUsedOperator = ConditionOperator.EQUAL;

    private ObjectQuickFilterManager(Project project) {
        super(project);
    }

    public void openFilterDialog(DBObjectList<?> objectList) {
        ObjectQuickFilterDialog dialog = new ObjectQuickFilterDialog(getProject(), objectList);
        dialog.show();
    }

    public void applyFilter(DBObjectList<?> objectList, @Nullable ObjectQuickFilter filter) {
        objectList.setQuickFilter(filter);
        ProjectEvents.notify(getProject(),
                BrowserTreeEventListener.TOPIC,
                (listener) -> listener.nodeChanged(objectList, TreeEventType.STRUCTURE_CHANGED));

        ObjectQuickFilterKey key = ObjectQuickFilterKey.from(objectList);
        if (filter == null || filter.isEmpty()) {
            quickFilters.remove(key);
        } else {
            quickFilters.put(key, filter);
        }
    }

    public void restoreQuickFilter(DBObjectList<?> objectList) {
        ObjectQuickFilterKey key = ObjectQuickFilterKey.from(objectList);
        objectList.setQuickFilter(cast(quickFilters.get(key)));
    }

    public ConditionOperator getLastUsedOperator() {
        return lastUsedOperator;
    }

    public void setLastUsedOperator(ConditionOperator lastUsedOperator) {
        this.lastUsedOperator = lastUsedOperator;
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static ObjectQuickFilterManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, ObjectQuickFilterManager.class);
    }

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        SettingsSupport.setEnum(element, "last-used-operator", lastUsedOperator);
        Element filtersElement = new Element("filters");
        element.addContent(filtersElement);

        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        for (var entry : quickFilters.entrySet()) {
            ObjectQuickFilterKey key = entry.getKey();
            if (connectionManager.isValidConnectionId(key.getConnectionId())) {
                ObjectQuickFilter<?> filter = entry.getValue();
                Element filterElement = new Element("filter");
                filtersElement.addContent(filterElement);

                key.writeState(filterElement);
                filter.writeState(filterElement);
            }
        }

        return element;
    }

    @Override
    public void loadState(Element element) {
        Element filtersElement = element.getChild("filters");
        lastUsedOperator = SettingsSupport.getEnum(element, "last-used-operator", lastUsedOperator);
        if (filtersElement != null) {
            for (Element child : filtersElement.getChildren()) {
                ObjectQuickFilterKey key = new ObjectQuickFilterKey();
                key.readState(child);

                ObjectQuickFilter<?> filter = new ObjectQuickFilter<>(key.getObjectType());
                filter.readState(child);

                quickFilters.put(key, filter);
            }
        }
    }
}
