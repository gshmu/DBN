package com.dci.intellij.dbn.object;

public interface DBPackageType extends DBType<DBTypeProcedure, DBTypeFunction> {
    DBPackage getPackage();
}