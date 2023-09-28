package com.dci.intellij.dbn.object.filter.quick;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.common.component.Components;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.options.setting.Settings;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.Dialogs;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.quick.ui.ObjectQuickFilterDialog;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.dci.intellij.dbn.common.options.setting.Settings.newElement;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;

@Getter
@Setter
@State(
    name = ObjectQuickFilterManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ObjectQuickFilterManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.ObjectQuickFilterManager";

    private final Map<ObjectQuickFilterKey, ObjectQuickFilter<?>> quickFilters = new HashMap<>();
    private ConditionOperator lastUsedOperator = ConditionOperator.EQUAL;

    private ObjectQuickFilterManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static ObjectQuickFilterManager getInstance(@NotNull Project project) {
        return Components.projectService(project, ObjectQuickFilterManager.class);
    }

    public void openFilterDialog(DBObjectList<?> objectList) {
        Dialogs.show(() -> new ObjectQuickFilterDialog(getProject(), objectList));
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

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getComponentState() {
        Element element = new Element("state");
        Settings.setEnum(element, "last-used-operator", lastUsedOperator);
        Element filtersElement = newElement(element, "filters");

        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        for (val entry : quickFilters.entrySet()) {
            ObjectQuickFilterKey key = entry.getKey();
            if (connectionManager.isValidConnectionId(key.getConnectionId())) {
                ObjectQuickFilter<?> filter = entry.getValue();
                Element filterElement = newElement(filtersElement, "filter");

                key.writeState(filterElement);
                filter.writeState(filterElement);
            }
        }

        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        Element filtersElement = element.getChild("filters");
        lastUsedOperator = Settings.getEnum(element, "last-used-operator", lastUsedOperator);
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
