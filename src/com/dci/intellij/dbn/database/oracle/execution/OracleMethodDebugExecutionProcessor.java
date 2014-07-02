package com.dci.intellij.dbn.database.oracle.execution;

import com.dci.intellij.dbn.object.DBMethod;

public class OracleMethodDebugExecutionProcessor extends OracleMethodExecutionProcessor {
    public OracleMethodDebugExecutionProcessor(DBMethod method) {
        super(method);
    }

    @Override
    protected void preHookExecutionCommand(StringBuilder buffer) {
        super.preHookExecutionCommand(buffer);
    }

    @Override
    protected void postHookExecutionCommand(StringBuilder buffer) {
        buffer.append("\n");
        buffer.append("    SYS.DBMS_DEBUG.debug_off();\n");
        buffer.append("exception\n");
        buffer.append("    when others then\n");
        buffer.append("        SYS.DBMS_DEBUG.debug_off();\n");
        buffer.append("        raise;\n");
    }

}