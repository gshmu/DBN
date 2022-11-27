package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.Priority;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceContext;
import lombok.Getter;

@Getter
public class InterfaceTaskDefinition {
    private final String title;
    private final String description;
    private final Priority priority;
    private final DatabaseInterfaceContext context;

    private InterfaceTaskDefinition(String title, String description, Priority priority, DatabaseInterfaceContext context) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.context = context;
    }

    public static InterfaceTaskDefinition create(Priority priority, String title, String description, DatabaseInterfaceContext context) {
        return new InterfaceTaskDefinition(title, description, priority, context);
    }
}
