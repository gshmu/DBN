package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.connection.ResourceUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Queue;

public class MultipartResultSet extends WrappedResultSet {
    private Queue<ResultSet> queue = ContainerUtil.newLinkedList();

    public MultipartResultSet(ResultSet ... resultSets) {
        super(null);
        add(resultSets);
    }

    public static MultipartResultSet create(ResultSet ... resultSets) {
        return new MultipartResultSet(resultSets);
    }

    public MultipartResultSet add(@Nullable ResultSet ... resultSets) {
        if (resultSets != null) {
            for (ResultSet resultSet : resultSets) {
                if (resultSet != null) {
                    queue.add(resultSet);
                }
            }
        }

        return this;
    }



    @Override
    public boolean next() throws SQLException {
        while (inner == null && !queue.isEmpty()) {
            inner = queue.poll();
        }

        if (inner == null) {
            return false;

        } else if (inner.next()) {
            return true;

        } else {
            ResourceUtil.close(inner);
            inner = null;
            return next();
        }
    }
}
