package com.dci.intellij.dbn.database.sqlite.adapter;

import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.database.common.util.ResultSetReader;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqliteRawMetaData {
    public static class RawForeignKeyInfo extends RawMetaData<RawForeignKeyInfo.Row> {

        public RawForeignKeyInfo(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected Row createRow(ResultSet resultSet) throws SQLException {
            return new Row(resultSet);
        }

        public static class Row {
            int id;
            short seq;
            String table;
            String from;
            String to;

            Row (ResultSet resultSet) throws SQLException {
                id = resultSet.getInt("id");
                seq = resultSet.getShort("seq");
                table = resultSet.getString("table");
                from = resultSet.getString("from");
                to = resultSet.getString("to");
            }

            public int getId() {
                return id;
            }

            public short getSeq() {
                return seq;
            }

            public String getTable() {
                return table;
            }

            public String getFrom() {
                return from;
            }

            public String getTo() {
                return to;
            }
        }
    }

    public static class RawIndexInfo extends RawMetaData<RawIndexInfo.Row> {

        public RawIndexInfo(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected Row createRow(ResultSet resultSet) throws SQLException {
            return new Row(resultSet);
        }

        public static class Row {
            int seq;
            int unique;
            int partial;
            String name;
            String origin;

            Row (ResultSet resultSet) throws SQLException {
                seq = resultSet.getInt("seq");
                name = resultSet.getString("name");
                try {unique = resultSet.getInt("unique");} catch (SQLException ignore) {}
                try {partial = resultSet.getInt("partial");} catch (SQLException ignore) {}
                try {origin = resultSet.getString("origin");} catch (SQLException ignore) {}
            }

            public int getSeq() {
                return seq;
            }

            public int getUnique() {
                return unique;
            }

            public int getPartial() {
                return partial;
            }

            public String getName() {
                return name;
            }

            public String getOrigin() {
                return origin;
            }
        }
    }

    public static class RawIndexDetailInfo extends RawMetaData<RawIndexDetailInfo.Row> {

        public RawIndexDetailInfo(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected Row createRow(ResultSet resultSet) throws SQLException {
            return new Row(resultSet);
        }

        public static class Row {
            short seqno;
            int cid;
            int desc;
            int key;
            String name;
            String coll;

            Row (ResultSet resultSet) throws SQLException {
                seqno = resultSet.getShort("seqno");
                cid = resultSet.getInt("cid");
                key = resultSet.getInt("key");
                name = resultSet.getString("name");
                desc = resultSet.getInt("desc");
                coll = resultSet.getString("coll");
            }

            public short getSeqno() {
                return seqno;
            }

            public int getCid() {
                return cid;
            }

            public int getDesc() {
                return desc;
            }

            public int getKey() {
                return key;
            }

            public String getName() {
                return name;
            }

            public String getColl() {
                return coll;
            }
        }
    }

    public static class RawTableInfo extends RawMetaData<RawTableInfo.Row> {

        public RawTableInfo(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected Row createRow(ResultSet resultSet) throws SQLException {
            return new Row(resultSet);
        }

        public static class Row {
            int cid;
            String name;
            String type;
            int notnull;
            int pk;

            Row (ResultSet resultSet) throws SQLException {
                cid = resultSet.getInt("cid");
                name = resultSet.getString("name");
                type = resultSet.getString("type");
                notnull = resultSet.getInt("notnull");
                pk = resultSet.getInt("pk");
            }

            public int getCid() {
                return cid;
            }

            public String getName() {
                return name;
            }

            public String getType() {
                return type;
            }

            public int getNotnull() {
                return notnull;
            }

            public int getPk() {
                return pk;
            }
        }
    }

    public static class TableNames extends RawMetaData<TableNames.Row> {

        public TableNames(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected Row createRow(ResultSet resultSet) throws SQLException {
            return new Row(resultSet);
        }

        public static class Row {
            String name;

            Row (ResultSet resultSet) throws SQLException {
                name = resultSet.getString("DATASET_NAME");
            }

            public String getName() {
                return name;
            }
        }
    }

    private abstract static class RawMetaData<T> extends ResultSetReader {
        private List<T> rows;
        RawMetaData(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected final void processRow(ResultSet resultSet) throws SQLException {
            T row = createRow(resultSet);
            if (rows == null) {
                rows = new ArrayList<T>();
            }
            rows.add(row);
        }

        @NotNull
        public List<T> getRows() {
            return Commons.nvl(rows, Collections.emptyList());
        }

        protected abstract T createRow(ResultSet resultSet) throws SQLException;
    }
}
