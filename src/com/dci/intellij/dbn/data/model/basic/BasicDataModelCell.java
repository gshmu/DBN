package com.dci.intellij.dbn.data.model.basic;

import com.dci.intellij.dbn.common.dispose.SafeDisposer;
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
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class BasicDataModelCell<
        R extends BasicDataModelRow<M, ? extends BasicDataModelCell<R, M>>,
        M extends BasicDataModel<R, ? extends BasicDataModelCell<R, M>>>
        extends PropertyHolderImpl<RecordStatus>
        implements DataModelCell<R, M> {

    protected WeakRef<R> row;
    protected Object userValue;
    private String formattedUserValue;
    protected int index;

    public BasicDataModelCell(Object userValue, R row, int index) {
        this.userValue = userValue;
        this.row = WeakRef.of(row);
        this.index = index;
    }

    @Override
    protected RecordStatus[] properties() {
        return RecordStatus.values();
    }

    @Override
    public Project getProject() {
        return getRow().getProject();
    }

    @Override
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

    @Override
    public void setContentType(TextContentType contentType) {
        DataModelState state = getModel().getState();
        state.setTextContentType(getColumnInfo().getName(), contentType.getName());
    }



    @Override
    @NotNull
    public R getRow() {
        return row.ensure();
    }

    @Override
    public void setUserValue(Object userValue) {
        this.userValue = userValue;
        this.formattedUserValue = null;
    }

    @Override
    public void updateUserValue(Object userValue, boolean bulk) {
        setUserValue(userValue);
    }

    @Override
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
    public M getModel() {
        return getRow().getModel();
    }

    @Override
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

    @Override
    public ColumnInfo getColumnInfo() {
        return getModel().getColumnInfo(index);
    }

    @Override
    public int getIndex() {
        return index;
    }

    public String toString() {
        // IMPORTANT return user value for copy to clipboard support
        return getFormattedUserValue();
    }

    @NotNull
    public Formatter getFormatter() {
        return getModel().getFormatter();
    }

    @Override
    public void dispose() {
        SafeDisposer.nullify(this);
    }
}
