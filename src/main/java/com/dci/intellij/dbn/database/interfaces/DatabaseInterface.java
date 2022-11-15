package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;

import java.sql.SQLException;

public interface DatabaseInterface {

    default void reset() {}

    interface Callable<T> extends ThrowableCallable<T, SQLException> {}

    interface Runnable extends ThrowableRunnable<SQLException> {}

    interface ConnectionCallable<T> extends ParametricCallable<DBNConnection, T, SQLException> {}

    interface ConnectionRunnable extends ParametricRunnable<DBNConnection, SQLException> {}

}
