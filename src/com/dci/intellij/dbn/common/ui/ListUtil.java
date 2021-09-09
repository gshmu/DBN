package com.dci.intellij.dbn.common.ui;

import com.intellij.openapi.ui.SelectFromListDialog;
import lombok.extern.slf4j.Slf4j;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.List;
import java.util.Set;

@Slf4j
public final class ListUtil {
    private ListUtil() {}

    public static void notifyListDataListeners(Object source, Set<ListDataListener> listDataListeners, int fromIndex, int toIndex, int eventType) {
        try {
            ListDataEvent event = new ListDataEvent(source, eventType, fromIndex, toIndex);
            for (ListDataListener listener : listDataListeners) {
                switch (eventType) {
                    case ListDataEvent.INTERVAL_ADDED:   listener.intervalAdded(event); break;
                    case ListDataEvent.INTERVAL_REMOVED: listener.intervalRemoved(event); break;
                    case ListDataEvent.CONTENTS_CHANGED: listener.contentsChanged(event); break;
                }
            }
        } catch (Exception e) {
            log.error("Error notifying actions model listeners", e);
        }
    }

    public static final SelectFromListDialog.ToStringAspect BASIC_TO_STRING_ASPECT = new SelectFromListDialog.ToStringAspect() {
        @Override
        public String getToStirng(Object obj) {
            return obj.toString();
        }
    };
    
    public static <T> T getLast(List<T> list) {
        return list == null || list.size() == 0 ? null : list.get(list.size() - 1);
    }

    public static <T> T getFirst(List<T> list) {
        return list == null || list.size() == 0 ? null : list.get(0);
    }
}
