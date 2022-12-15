package com.dci.intellij.dbn.common.ui.util;

import com.intellij.openapi.ui.SelectFromListDialog;
import lombok.extern.slf4j.Slf4j;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.Set;

@Slf4j
public final class Lists {
    private Lists() {}

    public static void notifyListDataListeners(Object source, Set<ListDataListener> listeners, int fromIndex, int toIndex, int eventType) {
        try {
            ListDataEvent event = new ListDataEvent(source, eventType, fromIndex, toIndex);
            Listeners.notify(listeners, l -> {
                switch (eventType) {
                    case ListDataEvent.INTERVAL_ADDED:   l.intervalAdded(event); break;
                    case ListDataEvent.INTERVAL_REMOVED: l.intervalRemoved(event); break;
                    case ListDataEvent.CONTENTS_CHANGED: l.contentsChanged(event); break;
                }
            });
        } catch (Exception e) {
            log.error("Error notifying actions model listeners", e);
        }
    }

    public static final SelectFromListDialog.ToStringAspect BASIC_TO_STRING_ASPECT = obj -> obj.toString();

}
