package com.dci.intellij.dbn.database.interfaces;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class DatabaseInterfaceQueue {
    private final Queue<DatabaseInterfaceTask> tasks = new PriorityQueue<>(Comparator.comparing(t -> t.getPriority()));

}
