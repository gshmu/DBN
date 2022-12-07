package com.dci.intellij.dbn.editor.data.model;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.ref.WeakRef;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ResultSets;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.value.ValueAdapter;
import com.dci.intellij.dbn.database.DatabaseFeature;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

@Getter
@Setter
public abstract class ResultSetAdapter extends ResultSets implements StatefulDisposable {
    private final boolean useSavePoints;
    private boolean insertMode;
    private final WeakRef<DatasetEditorModel> model;

    public ResultSetAdapter(DatasetEditorModel model) {
        this.model = WeakRef.of(model);
        ConnectionHandler connection = model.getConnection();
        useSavePoints = !DatabaseFeature.CONNECTION_ERROR_RECOVERY.isSupported(connection);
    }

    public DatasetEditorModel getModel() {
        return model.ensure();
    }

    public abstract void scroll(int rowIndex) throws SQLException;

    public abstract void updateRow() throws SQLException;

    public abstract void refreshRow() throws SQLException;

    public abstract void startInsertRow() throws SQLException;

    public abstract void cancelInsertRow() throws SQLException;

    public abstract void insertRow() throws SQLException;

    public abstract void deleteRow() throws SQLException;

    public abstract void setValue(int columnIndex, @NotNull ValueAdapter valueAdapter, @Nullable Object value) throws SQLException;

    public abstract void setValue(int columnIndex, @NotNull DBDataType dataType, @Nullable Object value) throws SQLException;
}
