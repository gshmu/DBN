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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DatabaseBrowserSortingSettings
        extends BasicProjectConfiguration<DatabaseBrowserSettings, DatabaseBrowserSortingSettingsForm> {

    private List<DBObjectComparator> comparators = new ArrayList<>();

    DatabaseBrowserSortingSettings(DatabaseBrowserSettings parent) {
        super(parent);
        comparators.add(DBObjectComparator.get(DBObjectType.COLUMN, SortingType.NAME));
        comparators.add(DBObjectComparator.get(DBObjectType.FUNCTION, SortingType.NAME));
        comparators.add(DBObjectComparator.get(DBObjectType.PROCEDURE, SortingType.NAME));
        comparators.add(DBObjectComparator.get(DBObjectType.ARGUMENT, SortingType.POSITION));
    }

    public <T extends DBObject> DBObjectComparator<T> getComparator(DBObjectType objectType) {
        for (DBObjectComparator comparator : comparators) {
            if (comparator.getObjectType().matches(objectType)) {
                return comparator;
            }
        }
        return null;
    }

    private static boolean contains(List<DBObjectComparator> comparators, DBObjectType objectType) {
        for (DBObjectComparator comparator : comparators) {
            if (comparator.getObjectType() == objectType){
                return true;
            }
        }
        return false;
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
        List<DBObjectComparator> newComparators = new ArrayList<DBObjectComparator>();
        List<Element> children = element.getChildren();
        for (Element child : children) {
            String objectTypeName = child.getAttributeValue("name");
            String sortingTypeName = child.getAttributeValue("sorting-type");
            DBObjectType objectType = DBObjectType.get(objectTypeName);
            SortingType sortingType = SortingType.valueOf(sortingTypeName);
            DBObjectComparator comparator = DBObjectComparator.get(objectType, sortingType);
            if (comparator != null) {
                newComparators.add(comparator);
            }
        }
        for (DBObjectComparator comparator : comparators) {
            if (!contains(newComparators, comparator.getObjectType())) {
                newComparators.add(comparator);
            }
        }
        comparators = newComparators;
    }

    @Override
    public void writeConfiguration(Element element) {
        for (DBObjectComparator comparator : comparators) {
            Element child = new Element("object-type");
            child.setAttribute("name", comparator.getObjectType().getName().toUpperCase());
            child.setAttribute("sorting-type", comparator.getSortingType().name());
            element.addContent(child);
        }
    }
}
