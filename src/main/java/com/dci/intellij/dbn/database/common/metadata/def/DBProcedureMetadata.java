package com.dci.intellij.dbn.database.common.metadata.def;

import java.sql.SQLException;

public interface DBProcedureMetadata extends DBMethodMetadata {

    String getProcedureName() throws SQLException;
}
