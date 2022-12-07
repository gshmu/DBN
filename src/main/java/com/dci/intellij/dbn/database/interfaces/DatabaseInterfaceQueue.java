package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceCounters;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskRequest;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface DatabaseInterfaceQueue extends StatefulDisposable {
    @NotNull
    ConnectionHandler getConnection();

    int size();

    int maxActiveTasks();

    InterfaceCounters counters();

    <R> R scheduleAndReturn(InterfaceTaskRequest request, ThrowableCallable<R, SQLException> callable) throws SQLException;

    void scheduleAndWait(InterfaceTaskRequest request, ThrowableRunnable<SQLException> runnable) throws SQLException;

    void scheduleAndForget(InterfaceTaskRequest request, ThrowableRunnable<SQLException> runnable) throws SQLException;
}
