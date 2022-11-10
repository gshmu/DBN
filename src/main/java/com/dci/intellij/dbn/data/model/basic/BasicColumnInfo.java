package com.dci.intellij.dbn.data.model.basic;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class BasicColumnInfo implements ColumnInfo {
    protected String name;
    protected int columnIndex;
    protected DBDataType dataType;

    public BasicColumnInfo(String name, DBDataType dataType, int columnIndex) {
        this.name = Strings.intern(name);
        this.columnIndex = columnIndex;
        this.dataType = dataType;
    }

    @Override
    @NotNull
    public DBDataType getDataType() {
        return Failsafe.nn(dataType);
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isSortable() {
        DBDataType dataType = getDataType();
        return dataType.isNative() &&
                dataType.getGenericDataType().is(
                        GenericDataType.LITERAL,
                        GenericDataType.NUMERIC,
                        GenericDataType.DATE_TIME);
    }


}
