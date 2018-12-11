package com.dci.intellij.dbn.data.model.basic;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.value.ArrayValue;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.dci.intellij.dbn.editor.data.model.RecordStatus;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.project.Project;

public class BasicDataModelCell extends PropertyHolderImpl<RecordStatus> implements DataModelCell {
    protected BasicDataModelRow row;
    protected Object userValue;
    private String formattedUserValue;
    protected int index;

    public BasicDataModelCell(Object userValue, BasicDataModelRow row, int index) {
        this.userValue = userValue;
        this.row = row;
        this.index = index;
    }

    @Override
    protected RecordStatus[] properties() {
        return RecordStatus.values();
    }

    public Project getProject() {
        return getRow().getProject();
    }

    public TextContentType getContentType() {
        DataModelState state = getModel().getState();
        String contentTypeName = state.getTextContentTypeName(getColumnInfo().getName());
        if (contentTypeName == null) {
            DBDataType dataType = getColumnInfo().getDataType();
            if (dataType.isNative()) {
                contentTypeName = dataType.getNativeDataType().getDataTypeDefinition().getContentTypeName();
            }
        }

        return TextContentType.get(getProject(), contentTypeName);
    }

    public void setContentType(TextContentType contentType) {
        DataModelState state = getModel().getState();
        state.setTextContentType(getColumnInfo().getName(), contentType.getName());
    }



    @NotNull
    public BasicDataModelRow getRow() {
        return FailsafeUtil.get(row);
    }

    public void setUserValue(Object userValue) {
        this.userValue = userValue;
        this.formattedUserValue = null;
    }

    public void updateUserValue(Object userValue, boolean bulk) {
        setUserValue(userValue);
    }

    public Object getUserValue() {
        return userValue;
    }

    public boolean isLobValue() {
        return userValue instanceof LargeObjectValue;
    }
    public boolean isArrayValue() {
        return userValue instanceof ArrayValue;
    }

    @Override
    public String getFormattedUserValue() {
        if (userValue != null) {
            Formatter formatter = getFormatter();
            return formatter.formatObject(userValue);
        }
        return null;
    }

    @NotNull
    @Override
    public BasicDataModel getModel() {
        return getRow().getModel();
    }

    public String getName() {
        return getColumnInfo().getName();
    }

    @Override
    public DBDataType getDataType() {
        return getColumnInfo().getDataType();
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.COLUMN;
    }

    public ColumnInfo getColumnInfo() {
        return getModel().getColumnInfo(index);
    }

    public int getIndex() {
        return index;
    }

    public String toString() {
        return userValue == null ? null : userValue.toString();
    }

    @NotNull
    public Formatter getFormatter() {
        return getModel().getFormatter();
    }

    /*
    public boolean equals(Object obj) {
        DataModelCell remoteCell = (DataModelCell) obj;
        return CommonUtil.safeEqual(getUserValue(), remoteCell.getUserValue());
    }
*/

    @Override
    public boolean equals(Object obj) {
        if (!isDisposed() && obj instanceof BasicDataModelCell) {
            BasicDataModelCell cell = (BasicDataModelCell) obj;
            return cell.index == index &&
                    cell.getRow().getIndex() == getRow().getIndex() &&
                    cell.getModel() == getModel();
        }
        return false;
    }

    @Override
    public int hashCode() {
        BasicDataModelRow row = getRow();
        return index + row.getIndex() + this.row.getModel().hashCode();
    }

    public void dispose() {
        if (!isDisposed()) {
            set(RecordStatus.DISPOSED, true);
            row = null;
            userValue = null;
            formattedUserValue = null;
        }
    }


    public boolean isDisposed() {
        return is(RecordStatus.DISPOSED);
    }
}
