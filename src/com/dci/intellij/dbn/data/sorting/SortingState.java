package com.dci.intellij.dbn.data.sorting;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.integerAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

@Getter
@Setter
@EqualsAndHashCode
public class SortingState implements PersistentStateElement, Cloneable<SortingState> {
    private final List<SortingInstruction> sortingInstructions = new ArrayList<>();

    public boolean applySorting(String columnName, SortDirection direction, boolean keepExisting, int maxColumns) {
        SortingInstruction instruction = getInstruction(columnName);
        boolean isNewColumn = instruction == null;
        if (isNewColumn) {
            if (direction.isIndefinite()) {
                direction = SortDirection.ASCENDING;
            }
            instruction = new SortingInstruction(columnName, direction);
        } else {
            if (direction.isIndefinite()) {
                instruction.switchDirection();
            } else {
                instruction.setDirection(direction);
            }
        }


        if (keepExisting) {
            while (sortingInstructions.size() > maxColumns) {
                sortingInstructions.remove(sortingInstructions.size()-1);
            }

            if (isNewColumn) {
                if (sortingInstructions.size()== maxColumns) {
                    sortingInstructions.remove(sortingInstructions.size()-1);
                }
                sortingInstructions.add(instruction);
            }

        } else {
            sortingInstructions.clear();
            sortingInstructions.add(instruction);
        }

        updateIndexes();
        return true;
    }

    private void updateIndexes() {
        int index = 1;
        for (SortingInstruction sortingInstruction : sortingInstructions) {
            sortingInstruction.setIndex(index);
            index++;
        }
    }

    private SortingInstruction getInstruction(String columnName) {
        return sortingInstructions
                .stream()
                .filter(instruction -> Objects.equals(instruction.getColumnName(), columnName))
                .findFirst()
                .orElse(null);
    }

    public void clear() {
        sortingInstructions.clear();
    }

    public void addSortingInstruction(SortingInstruction sortingInstruction) {
        sortingInstructions.add(sortingInstruction);
    }

    public SortingInstruction addSortingInstruction(String columnName, SortDirection direction) {
        SortingInstruction sortingInstruction = new SortingInstruction(columnName, direction);
        sortingInstructions.add(sortingInstruction);
        return sortingInstruction;
    }

    public List<SortingInstruction> getSortingInstructions() {
        return sortingInstructions;
    }

    public SortingInstruction getSortingInstruction(String columnName) {
        for (SortingInstruction sortingInstruction :  sortingInstructions) {
            if (Strings.equalsIgnoreCase(sortingInstruction.getColumnName(), columnName)) {
                return sortingInstruction;
            }
        }
        return null;
    }

    public boolean isValid() {
        return true;
    }

    @Override
    public SortingState clone() {
        SortingState clone = new SortingState();
        for (SortingInstruction criterion : sortingInstructions) {
            clone.sortingInstructions.add(criterion.clone());
        }
        return clone;
    }

    @Override
    public void writeState(Element element) {
        for (SortingInstruction sortingInstruction : sortingInstructions) {
            String columnName = sortingInstruction.getColumnName();
            SortDirection sortDirection = sortingInstruction.getDirection();
            if (columnName != null && !sortDirection.isIndefinite()) {
                Element columnElement = new Element("column");
                columnElement.setAttribute("name", columnName);
                columnElement.setAttribute("index", Integer.toString(sortingInstruction.getIndex()));
                columnElement.setAttribute("direction", sortDirection.name());
                element.addContent(columnElement);
            }
        }
    }

    @Override
    public void readState(Element element) {
        if (element != null) {
            for (Element child:  element.getChildren()) {
                String columnName = stringAttribute(child, "name");
                String sortDirection = stringAttribute(child, "direction");
                SortingInstruction sortingInstruction = addSortingInstruction(columnName, SortDirection.valueOf(sortDirection));
                sortingInstruction.setIndex(integerAttribute(element, "index", 1));
            }
            updateIndexes();
        }
    }

    public int size() {
        return sortingInstructions.size();
    }
}
