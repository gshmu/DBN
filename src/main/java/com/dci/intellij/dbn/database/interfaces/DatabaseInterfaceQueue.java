package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.interfaces.queue.Counters;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskDefinition;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface DatabaseInterfaceQueue extends StatefulDisposable {
    @NotNull
    ConnectionHandler getConnection();

    int size();

    int maxActiveTasks();

    Counters counters();

    <R> R scheduleAndReturn(InterfaceTaskDefinition info, ThrowableCallable<R, SQLException> callable) throws SQLException;

    void scheduleAndWait(InterfaceTaskDefinition info, ThrowableRunnable<SQLException> runnable) throws SQLException;

    void scheduleAndForget(InterfaceTaskDefinition info, ThrowableRunnable<SQLException> runnable) throws SQLException;
}
