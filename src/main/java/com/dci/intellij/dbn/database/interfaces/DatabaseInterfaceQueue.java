package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.Priority;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.interfaces.queue.Counters;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface DatabaseInterfaceQueue extends StatefulDisposable {
    @NotNull
    ConnectionHandler getConnection();

    int size();

    int maxActiveTasks();

    Counters counters();

    <R> R scheduleAndReturn(String title, Priority priority, ThrowableCallable<R, SQLException> callable) throws SQLException;

    void scheduleAndWait(String title, Priority priority, ThrowableRunnable<SQLException> runnable) throws SQLException;

    void scheduleAndForget(String title, Priority priority, ThrowableRunnable<SQLException> runnable) throws SQLException;
}
