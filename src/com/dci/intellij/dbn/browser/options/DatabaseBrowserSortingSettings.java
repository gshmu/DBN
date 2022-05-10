package com.dci.intellij.dbn.browser.options;

import com.dci.intellij.dbn.browser.options.ui.DatabaseBrowserSortingSettingsForm;
import com.dci.intellij.dbn.common.options.BasicProjectConfiguration;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.sorting.DBObjectComparator;
import com.dci.intellij.dbn.object.common.sorting.SortingType;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DatabaseBrowserSortingSettings
        extends BasicProjectConfiguration<DatabaseBrowserSettings, DatabaseBrowserSortingSettingsForm> {

    private Map<DBObjectType, DBObjectComparator> comparators = new LinkedHashMap<>();

    DatabaseBrowserSortingSettings(DatabaseBrowserSettings parent) {
        super(parent);
        comparators.put(DBObjectType.COLUMN, DBObjectComparator.get(DBObjectType.COLUMN, SortingType.NAME));
        comparators.put(DBObjectType.FUNCTION, DBObjectComparator.get(DBObjectType.FUNCTION, SortingType.NAME));
        comparators.put(DBObjectType.PROCEDURE, DBObjectComparator.get(DBObjectType.PROCEDURE, SortingType.NAME));
        comparators.put(DBObjectType.ARGUMENT, DBObjectComparator.get(DBObjectType.ARGUMENT, SortingType.POSITION));
    }

    public <T extends DBObject> DBObjectComparator<T> getComparator(DBObjectType objectType) {
        return comparators.get(objectType.getGenericType());
    }

    public Collection<DBObjectComparator> getComparators() {
        return comparators.values();
    }

    public void setComparators(Collection<DBObjectComparator> comparators) {
        Map<DBObjectType, DBObjectComparator> newComparators = new LinkedHashMap<>();
        for (DBObjectComparator comparator : comparators) {
            newComparators.put(comparator.getObjectType(), comparator);
        }

        this.comparators = newComparators;
    }

    @NotNull
    @Override
    public DatabaseBrowserSortingSettingsForm createConfigurationEditor() {
        return new DatabaseBrowserSortingSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "sorting";
    }

    @Override
    public String getDisplayName() {
        return "Database Browser";
    }

    @Override
    public String getHelpTopic() {
        return "browserSettings";
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/

    @Override
    public void readConfiguration(Element element) {
        Map<DBObjectType, DBObjectComparator> newComparators = new LinkedHashMap<>();
        List<Element> children = element.getChildren();
        for (Element child : children) {
            String objectTypeName = stringAttribute(child, "name");
            String sortingTypeName = stringAttribute(child, "sorting-type");
            DBObjectType objectType = DBObjectType.get(objectTypeName);
            SortingType sortingType = SortingType.valueOf(sortingTypeName);
            DBObjectComparator comparator = DBObjectComparator.get(objectType, sortingType);
            if (comparator != null) {
                newComparators.put(comparator.getObjectType(), comparator);
            }
        }
        for (DBObjectComparator comparator : comparators.values()) {
            DBObjectType objectType = comparator.getObjectType();
            if (!newComparators.containsKey(objectType)) {
                newComparators.put(objectType, comparator);
            }
        }
        comparators = newComparators;
    }

    @Override
    public void writeConfiguration(Element element) {
        for (DBObjectComparator comparator : comparators.values()) {
            Element child = new Element("object-type");
            child.setAttribute("name", comparator.getObjectType().getName().toUpperCase());
            child.setAttribute("sorting-type", comparator.getSortingType().name());
            element.addContent(child);
        }
    }
}
