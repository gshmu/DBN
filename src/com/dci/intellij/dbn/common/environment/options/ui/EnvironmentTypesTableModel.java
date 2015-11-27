package com.dci.intellij.dbn.common.environment.options.ui;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.Color;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentConfigLocalListener;
import com.dci.intellij.dbn.common.ui.table.DBNEditableTableModel;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

public class EnvironmentTypesTableModel extends DBNEditableTableModel {
    private EnvironmentTypeBundle environmentTypes;
    private Project project;

    public EnvironmentTypesTableModel(Project project, EnvironmentTypeBundle environmentTypes) {
        this.project = project;
        this.environmentTypes = new EnvironmentTypeBundle(environmentTypes);
        addTableModelListener(defaultModelListener);
    }

    public EnvironmentTypeBundle getEnvironmentTypes() {
        return environmentTypes;
    }

    public void setEnvironmentTypes(EnvironmentTypeBundle environmentTypes) {
        this.environmentTypes = new EnvironmentTypeBundle(environmentTypes);
        notifyListeners(0, environmentTypes.size(), -1);
    }

    public int getRowCount() {
        return environmentTypes.size();
    }
    
    TableModelListener defaultModelListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
            EnvironmentConfigLocalListener listener = EventUtil.notify(project, EnvironmentConfigLocalListener.TOPIC);
            listener.settingsChanged(environmentTypes);
        }
    };    

    public int getColumnCount() {
        return 5;
    }

    public String getColumnName(int columnIndex) {
        return columnIndex == 0 ? "Name" :
               columnIndex == 1 ? "Description" :
               columnIndex == 2 ? "Readonly Data" :
               columnIndex == 3 ? "Readonly Code" :
               columnIndex == 4 ? "Color" : null;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? String.class :
            columnIndex == 1 ? String.class :
            columnIndex == 2 ? Boolean.class :
            columnIndex == 3 ? Boolean.class:
            columnIndex == 4 ? Color.class : String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        EnvironmentType environmentType = getEnvironmentType(rowIndex);
        return
           columnIndex == 0 ? environmentType.getName() :
           columnIndex == 1 ? environmentType.getDescription() :
           columnIndex == 2 ? environmentType.isReadonlyData() :
           columnIndex == 3 ? environmentType.isReadonlyCode() :
           columnIndex == 4 ? environmentType.getColor() : null;
    }

    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        Object actualValue = getValueAt(rowIndex, columnIndex);
        if (!CommonUtil.safeEqual(actualValue, o)) {
            EnvironmentType environmentType = environmentTypes.get(rowIndex);
            if (columnIndex == 0) {
                environmentType.setName((String) o);
            } else if (columnIndex == 1) {
                environmentType.setDescription((String) o);
            } else if (columnIndex == 2) {
                environmentType.setReadonlyData((Boolean) o);
            } else if (columnIndex == 3) {
                environmentType.setReadonlyCode((Boolean) o);
            } else if (columnIndex == 4) {
                Color color = (Color) o;
                environmentType.setColor(color);
            }

            notifyListeners(rowIndex, rowIndex, columnIndex);
        }
    }

    private EnvironmentType getEnvironmentType(int rowIndex) {
        while (environmentTypes.size() <= rowIndex) {
            environmentTypes.add(new EnvironmentType());
        }
        return environmentTypes.get(rowIndex);
    }

    public void insertRow(int rowIndex) {
        environmentTypes.add(rowIndex, new EnvironmentType());
        notifyListeners(rowIndex, environmentTypes.size()-1, -1);
    }

    public void removeRow(int rowIndex) {
        if (environmentTypes.size() > rowIndex) {
            environmentTypes.remove(rowIndex);
            notifyListeners(rowIndex, environmentTypes.size()-1, -1);
        }
    }

    public void validate() throws ConfigurationException {
        for (EnvironmentType environmentType : environmentTypes) {
            if (StringUtil.isEmpty(environmentType.getName())) {
                throw new ConfigurationException("Please provide names for all environment types.");
            }
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    public void dispose() {
        super.dispose();
    }
}
