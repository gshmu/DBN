package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.data.sorting.SortDirection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConnectionListModel extends DefaultListModel<ConnectionSettings> {
    public ConnectionListModel(ConnectionBundleSettings connectionBundleSettings) {
        List<ConnectionSettings> connections = connectionBundleSettings.getConnections();
        for (ConnectionSettings connection : connections) {
            addElement(connection);
        }
    }

    public ConnectionSettings getConnectionConfig(String name) {
        for (int i=0; i< getSize(); i++){
            ConnectionSettings connectionSettings = (ConnectionSettings) getElementAt(i);
            if (connectionSettings.getDatabaseSettings().getName().equals(name)) {
                return connectionSettings;
            }
        }
        return null;
    }

    public void sort(SortDirection sortDirection) {
        List<ConnectionSettings> list = new ArrayList<ConnectionSettings>();
        for (int i=0; i< getSize(); i++){
            ConnectionSettings connectionSettings = (ConnectionSettings) getElementAt(i);
            list.add(connectionSettings);
        }

        switch (sortDirection) {
            case ASCENDING: Collections.sort(list, ascComparator); break;
            case DESCENDING: Collections.sort(list, descComparator); break;
        }

        clear();
        for (ConnectionSettings connectionSettings : list) {
            addElement(connectionSettings);
        }
    }

    private Comparator<ConnectionSettings> ascComparator = new Comparator<ConnectionSettings>() {
        public int compare(ConnectionSettings connectionSettings1, ConnectionSettings connectionSettings2) {
            return connectionSettings1.getDatabaseSettings().getName().compareTo(connectionSettings2.getDatabaseSettings().getName());
        }
    };

    private Comparator<ConnectionSettings> descComparator = new Comparator<ConnectionSettings>() {
        public int compare(ConnectionSettings connectionSettings1, ConnectionSettings connectionSettings2) {
            return -connectionSettings1.getDatabaseSettings().getName().compareTo(connectionSettings2.getDatabaseSettings().getName());
        }
    };
}
