package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.routine.BasicCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;

import java.sql.SQLException;

public interface ResourceStatusAdapter<T extends Resource> {
    boolean get();

    void set(boolean value) throws SQLException;

    static <T extends Resource> ResourceStatusAdapter<T> create(
            T resource,
            ResourceStatus subject,
            ResourceStatus changing,
            ResourceStatus checking,
            long checkInterval,
            boolean terminal,
            ParametricRunnable<Boolean, SQLException> setter,
            BasicCallable<Boolean, SQLException> getter) {

        return new ResourceStatusAdapterImpl<T>(resource, subject, changing, checking, checkInterval, terminal) {
            @Override
            protected void changeInner(boolean value) throws SQLException {
                setter.run(value);
            }

            @Override
            protected boolean checkInner() throws SQLException {
                return getter.call();
            }
        };
    }
}
