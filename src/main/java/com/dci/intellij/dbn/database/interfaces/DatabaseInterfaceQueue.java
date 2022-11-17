package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.Counter;
import com.dci.intellij.dbn.common.Priority;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;

import java.sql.SQLException;
import java.util.concurrent.Callable;

public interface DatabaseInterfaceQueue extends StatefulDisposable {
    int size();

    int maxActiveTasks();

    Counter activeTasks();

    Counter finishedTasks();

    <R> Callable<R> callableOf(DatabaseInterfaceQueueImpl.InterfaceTask<R> task);

    <R> R scheduleAndReturn(Priority priority, ThrowableCallable<R, SQLException> callable) throws SQLException;

    void scheduleAndWait(Priority priority, ThrowableRunnable<SQLException> runnable) throws SQLException;

    void scheduleAndForget(Priority priority, ThrowableRunnable<SQLException> runnable) throws SQLException;
}
