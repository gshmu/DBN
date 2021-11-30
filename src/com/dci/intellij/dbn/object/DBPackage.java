package com.dci.intellij.dbn.object;

import java.util.List;

public interface DBPackage extends DBProgram<DBPackageProcedure, DBPackageFunction> {
    List<DBPackageType> getTypes();
    DBPackageType getType(String name);
}