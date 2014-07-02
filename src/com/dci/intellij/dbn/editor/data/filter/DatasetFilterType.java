package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.Icons;

import javax.swing.Icon;

public enum DatasetFilterType {
    NONE("None", Icons.DATASET_FILTER_EMPTY, Icons.DATASET_FILTER_EMPTY),
    BASIC("Basic", Icons.DATASET_FILTER_BASIC, Icons.DATASET_FILTER_BASIC_ERR),
    CUSTOM("Custom", Icons.DATASET_FILTER_CUSTOM, Icons.DATASET_FILTER_CUSTOM_ERR),
    GLOBAL("Global", Icons.DATASET_FILTER_GLOBAL, Icons.DATASET_FILTER_GLOBAL_ERR);

    private String displayName;
    private Icon icon;
    private Icon errIcon;

    DatasetFilterType(String displayName, Icon icon, Icon errIcon) {
        this.displayName = displayName;
        this.icon = icon;
        this.errIcon = errIcon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    public Icon getErrIcon() {
        return errIcon;
    }

    public static DatasetFilterType get(String name) {
        for (DatasetFilterType datasetFilterType : DatasetFilterType.values()) {
            if (datasetFilterType.getDisplayName().equals(name) || datasetFilterType.name().equals(name)) {
                return datasetFilterType;
            }
        }
        return null;
    }
}
