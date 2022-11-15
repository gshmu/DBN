package com.dci.intellij.dbn.connection.util;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;

import java.sql.SQLException;

public final class Jdbc {
    private Jdbc() {}


    public interface Callable<T> extends ThrowableCallable<T, SQLException> {}

    public interface Runnable extends ThrowableRunnable<SQLException> {}

    public interface ConnectionCallable<T> extends ParametricCallable<DBNConnection, T, SQLException>{}

    public interface ConnectionRunnable extends ParametricRunnable<DBNConnection, SQLException> {}
}
