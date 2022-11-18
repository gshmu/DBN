package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.Priority;
import com.dci.intellij.dbn.common.util.Naming;
import lombok.Getter;

@Getter
public class InterfaceTaskDefinition {
    private final String title;
    private final String description;
    private final Priority priority;
    private final InterfaceContext context;

    private InterfaceTaskDefinition(String title, String description, Priority priority, InterfaceContext context) {
        this.title = context == null ? title : Naming.getTaskTitle(title, context.getConnection());
        this.description = description;
        this.priority = priority;
        this.context = context;
    }

    public static InterfaceTaskDefinition create(String title, String description, Priority priority, InterfaceContext context) {
        return new InterfaceTaskDefinition(title, description, priority, context);
    }
}
