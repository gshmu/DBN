package com.dci.intellij.dbn.data.value;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.sql.Connection;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;
import static com.dci.intellij.dbn.common.util.Unsafe.silent;

@Getter
public class XmlTypeDelegate {

    @Getter(lazy = true)
    private static final XmlTypeDelegate instance = new XmlTypeDelegate();

    private final Class<?> xmlTypeClass;
    private final Class<?> opaqueClass;
    private final Class<?> datumClass;
    private final Class<?> statementClass;
    private final Class<?> resultSetClass;

    private final Method statementOpaqueMethod;
    private final Method resultSetOpaqueMethod;
    private final Method createXmlFromOpaqueMethod;
    private final Method createXmlFromStringMethod;
    private final Method updateResultSetObjectMethod;
    private final Method stringValueMethod;

    @SneakyThrows
    public XmlTypeDelegate() {
        xmlTypeClass = Class.forName("oracle.xdb.XMLType");
        opaqueClass = Class.forName("oracle.sql.OPAQUE");
        datumClass = Class.forName("oracle.sql.Datum");
        statementClass = Class.forName("oracle.jdbc.OracleCallableStatement");
        resultSetClass = Class.forName("oracle.jdbc.OracleResultSet");

        statementOpaqueMethod = statementClass.getMethod("getOPAQUE", int.class);
        resultSetOpaqueMethod = resultSetClass.getMethod("getOPAQUE", int.class);
        createXmlFromOpaqueMethod = xmlTypeClass.getMethod("createXML", opaqueClass);
        createXmlFromStringMethod = xmlTypeClass.getMethod("createXML", Connection.class, String.class);
        updateResultSetObjectMethod = resultSetClass.getMethod("updateOracleObject", int.class, datumClass);
        stringValueMethod = xmlTypeClass.getMethod("getStringVal");
    }

    public boolean isXmlType(Object opaque) {
        return xmlTypeClass.isAssignableFrom(opaque.getClass());
    }

    public <T> T invoke(Method method, Object obj, Object ... args) {
        return cast(silent(null, () -> method.invoke(obj, args)));
    }
}
