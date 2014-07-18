package com.dci.intellij.dbn.database.oracle.execution;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.List;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessorImpl;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeAttribute;

public class OracleMethodExecutionProcessor extends MethodExecutionProcessorImpl<DBMethod> {
    public OracleMethodExecutionProcessor(DBMethod method) {
        super(method);
    }

    protected void preHookExecutionCommand(StringBuilder buffer) {}
    protected void postHookExecutionCommand(StringBuilder buffer) {}


    @Override
    public String buildExecutionCommand(MethodExecutionInput executionInput) throws SQLException {
        DBArgument returnArgument = getReturnArgument();

        StringBuilder buffer = new StringBuilder();
        buffer.append("declare\n");

        // variable declarations
        List<DBArgument> arguments = getArguments();
        for (DBArgument argument : arguments) {
            DBDataType dataType = argument.getDataType();
            if (dataType.isDeclared()) {
                buffer.append("    ");
                appendVariableName(buffer, argument);
                buffer.append(" ").append(dataType.getDeclaredType().getQualifiedName()).append(";\n");
            } else if (isBoolean(dataType)) {
                appendVariableName(buffer, argument);
                buffer.append(" boolean;\n");
            }
        }

        buffer.append("begin\n");

        preHookExecutionCommand(buffer);

        // input variable initialization
        for (DBArgument argument : arguments) {
            DBDataType dataType = argument.getDataType();

            if (argument.isInput()) {
                if (dataType.isDeclared()) {
                    List<DBTypeAttribute> attributes = dataType.getDeclaredType().getAttributes();
                    for (DBTypeAttribute attribute : attributes) {
                        buffer.append("    ");
                        appendVariableName(buffer, argument);
                        buffer.append(".").append(attribute.getName()).append(" := ?;\n");
                    }
                } else if(isBoolean(dataType)) {
                    String stringValue = parseBoolean(argument.getName(), executionInput.getInputValue(argument));

                    buffer.append("    ");
                    appendVariableName(buffer, argument);
                    buffer.append(" := ").append(stringValue).append(";\n");
                }
            }
        }

        // method call
        buffer.append("\n    ");
        if (returnArgument != null) {
            DBDataType dataType = returnArgument.getDataType();
            if (dataType.isDeclared() || isBoolean(dataType))
                appendVariableName(buffer, returnArgument); else
                buffer.append("?");

            buffer.append(" := ");
        }

        // method parameters
        buffer.append(getMethod().getQualifiedName()).append("(");
        for (DBArgument argument : arguments) {
            if (argument != returnArgument) {
                DBDataType dataType = argument.getDataType();
                if (dataType.isDeclared() || isBoolean(dataType))
                    appendVariableName(buffer, argument); else
                    buffer.append("?");
                boolean isLast = arguments.indexOf(argument) == arguments.size() - 1;
                if (!isLast) buffer.append(", ");
            }
        }
        buffer.append(");\n\n");


        // output variable initialization
        for (DBArgument argument : arguments) {
            if (argument.isOutput()) {
                DBDataType dataType = argument.getDataType();
                if (dataType.isDeclared()) {
                    List<DBTypeAttribute> attributes = dataType.getDeclaredType().getAttributes();
                    for (DBTypeAttribute attribute : attributes) {
                        buffer.append("    ? := ");
                        appendVariableName(buffer, argument);
                        buffer.append(".").append(attribute.getName()).append(";\n");
                    }
                } else if (isBoolean(dataType)) {
                    buffer.append("    ? := case when ");
                    appendVariableName(buffer, argument);
                    buffer.append(" then 'true' else 'false' end;\n");
                }
            }
        }


        postHookExecutionCommand(buffer);
        buffer.append("\nend;\n");
        return buffer.toString();
    }

    private StringBuilder appendVariableName(StringBuilder buffer, DBArgument argument) {
        return buffer.append("var_").append(argument.getPosition());
    }


    @Override
    protected void bindParameters(MethodExecutionInput executionInput, CallableStatement callableStatement) throws SQLException {
        DBArgument returnArgument = getReturnArgument();

        // bind input variables
        int parameterIndex = 1;
        for (DBArgument argument : getArguments()) {
            if (argument.isInput()) {
                DBDataType dataType = argument.getDataType();
                DBType type = dataType.getDeclaredType();
                if (dataType.isDeclared()) {
                    List<DBTypeAttribute> attributes = type.getAttributes();
                    for (DBTypeAttribute attribute : attributes) {
                        String stringValue = executionInput.getInputValue(argument, attribute);
                        setParameterValue(callableStatement, parameterIndex, attribute.getDataType(), stringValue);
                        parameterIndex++;
                    }
                }
            }
        }

        // bind return variable (functions only)
        if (returnArgument != null) {
            DBDataType dataType = returnArgument.getDataType();
            if(!dataType.isDeclared() && !isBoolean(dataType)) {
                callableStatement.registerOutParameter(parameterIndex, returnArgument.getDataType().getSqlType());
                parameterIndex++;
            }
        }

        // bind input/output parameters
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (!argument.equals(returnArgument) && dataType.isNative() && !isBoolean(dataType)) {
                if (argument.isInput()) {
                    String stringValue = executionInput.getInputValue(argument);
                    setParameterValue(callableStatement, parameterIndex, dataType, stringValue);
                }
                if (argument.isOutput()){
                    callableStatement.registerOutParameter(parameterIndex, dataType.getSqlType());
                }
                parameterIndex++;
            }
        }

        // bind output variables
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (argument.isOutput()) {
                if (dataType.isDeclared()) {
                    List<DBTypeAttribute> attributes = dataType.getDeclaredType().getAttributes();
                    for (DBTypeAttribute attribute : attributes) {
                        callableStatement.registerOutParameter(parameterIndex, attribute.getDataType().getSqlType());
                        parameterIndex++;
                    }
                } else if (isBoolean(dataType)){
                    callableStatement.registerOutParameter(parameterIndex, dataType.getSqlType());
                    parameterIndex++;
                }
            }
        }
    }

    @Override
    public void loadValues(MethodExecutionResult executionResult, CallableStatement callableStatement) throws SQLException {
        DBArgument returnArgument = getReturnArgument();

        // increment parameter index for input variables
        int parameterIndex = 1;
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (dataType.isDeclared()) {
                if (argument.isInput()) {
                    parameterIndex = parameterIndex + dataType.getDeclaredType().getAttributes().size();
                }
            }
        }

        // get return value (functions only)
        if (returnArgument != null) {
            DBDataType dataType = returnArgument.getDataType();
            if (!dataType.isDeclared() && !isBoolean(dataType)) {
                Object result = callableStatement.getObject(parameterIndex);
                executionResult.addArgumentValue(returnArgument, result);
                parameterIndex++;

            }
        }

        // get output parameter values
        for (DBArgument argument : getArguments()) {
            if (!argument.equals(returnArgument)) {
                DBDataType dataType = argument.getDataType();
                if (dataType.isNative() && !isBoolean(dataType)) {
                    if (argument.isOutput()){
                        Object result = callableStatement.getObject(parameterIndex);
                        executionResult.addArgumentValue(argument, result);
                    }
                    parameterIndex++;
                }
            }
        }

        // get output variable values
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (argument.isOutput()) {
                if (dataType.isDeclared()) {
                    executionResult.addArgumentValue(argument, null);
                    List<DBTypeAttribute> attributes = dataType.getDeclaredType().getAttributes();
                    for (DBTypeAttribute attribute : attributes) {
                        Object result = callableStatement.getObject(parameterIndex);
                        executionResult.addArgumentValue(argument, attribute, result);
                        parameterIndex++;
                    }
                } else if (isBoolean(dataType)) {
                    Object result = callableStatement.getObject(parameterIndex);
                    executionResult.addArgumentValue(argument, result);
                    parameterIndex++;

                }
            }
        }
    }

    private boolean isBoolean(DBDataType dataType) {
        DBNativeDataType nativeDataType = dataType.getNativeDataType();
        return nativeDataType != null && nativeDataType.getDataTypeDefinition().getGenericDataType() == GenericDataType.BOOLEAN;
    }

    private static String parseBoolean(String argumentName, String booleanString) throws SQLException {
        if (booleanString != null && !booleanString.equalsIgnoreCase("true") && !booleanString.equalsIgnoreCase("false")) {
            throw new SQLException("Invalid boolean value for argument '" + argumentName + "'. true / false expected");
        }
        return Boolean.toString(Boolean.parseBoolean(booleanString));
    }

}