package com.dci.intellij.dbn.database.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.WordTokenizer;
import com.dci.intellij.dbn.database.common.util.ResultSetAdapter;

public class MySqlArgumentsResultSet extends ResultSetAdapter {
    private class Argument {
        String name;
        String programName;
        String methodName;
        String methodType;
        int overload;
        int position;
        int sequence;
        String inOut = "IN";
        String dataTypeOwner;
        String dataTypePackage;
        String dataTypeName;
        int dataLength;
        Integer dataPrecision;
        Integer dataScale;
    }
    private Iterator<Argument> arguments;
    private Argument currentArgument;

    public MySqlArgumentsResultSet(ResultSet resultSet) throws SQLException {
        List<Argument> argumentList = new ArrayList<Argument>();
        while (resultSet.next()) {
            String argumentsString = resultSet.getString("ARGUMENTS");
            WordTokenizer wordTokenizer = new WordTokenizer(argumentsString);

            String methodName = resultSet.getString("METHOD_NAME");
            String methodType = resultSet.getString("METHOD_TYPE");
            boolean betweenBrackets = false;
            boolean typePostfixSet = false;
            int argumentPosition = methodType.equals("FUNCTION") ? 0 : 1;

            Argument argument = null; 

            for (String token : wordTokenizer.getTokens()) {
                if (argument == null) {
                    typePostfixSet = false;
                    argument = new Argument();
                    argument.methodName = methodName;
                    argument.methodType = methodType;
                    argument.position = argumentPosition;

                    argumentList.add(argument);
                    argumentPosition++;
                }

                // hit IN OUT or INOUT token and name is not set
                if ((token.equalsIgnoreCase("IN") || token.equalsIgnoreCase("OUT") || token.equalsIgnoreCase("INOUT"))) {
                    if (argument.name != null) throwParseException(argumentsString, token, "Argument name should not be set.");
                    argument.inOut = token.toUpperCase();
                    continue;
                }

                // found open bracket => set betweenBrackets flag
                if (token.equals("(")) {
                    if (betweenBrackets) throwParseException(argumentsString, token, "Bracket already opened.");
                    if (argument.dataTypeName == null) throwParseException(argumentsString, token, "Data type not set yet.");
                    betweenBrackets = true;
                    continue;
                }

                // found close bracket => reset betweenBrackets flag
                if (token.equals(")")) {
                    if (!betweenBrackets) throwParseException(argumentsString, token, "No opened bracket.");
                    if (argument.dataPrecision == null && argument.dataScale == null) throwParseException(argumentsString, token, "Data precision and scale are not set yet.");
                    betweenBrackets = false;
                    continue;
                }

                // found comma token
                if (token.equals(",")) {
                    if (betweenBrackets) {
                        // between brackets
                        if (argument.dataPrecision == null) throwParseException(argumentsString, token, "Data precision is not set yet.");
                        continue;
                    } else {
                        // not between brackets => new argument
                        if (argument.name == null) throwParseException(argumentsString, token, "Argument name not set yet.");
                        if (argument.dataTypeName == null) throwParseException(argumentsString, token, "Data type not set yet.");
                        argument = null;
                        continue;
                    }
                }

                // number token
                if (StringUtil.isInteger(token)) {
                    if (!betweenBrackets) throwParseException(argumentsString, token, "No bracket opened.");
                    if (argument.name == null) throwParseException(argumentsString, token, "Argument name not set yet.");
                    if (argument.dataTypeName == null) throwParseException(argumentsString, token, "Data type not set yet.");

                    // if precision not set then set it
                    if (argument.dataPrecision == null) {
                        argument.dataPrecision = new Integer(token);
                        continue;
                    }
                    // if scale not set then set it
                    if (argument.dataScale == null) {
                        argument.dataScale = new Integer(token);
                        continue;
                    }
                    throwParseException(argumentsString, token);
                }

                // if none of the conditions above are met
                if (argument.name == null) {
                    argument.name = token;
                    continue;
                }

                if (argument.dataTypeName == null) {
                    argument.dataTypeName = token;
                    continue;
                }

                if (!typePostfixSet) {
                    typePostfixSet = true;
                    continue;
                }

                throwParseException(argumentsString, token);
            }
        }

        arguments = argumentList.iterator();
    }

    private static void throwParseException(String argumentsString, String token) throws SQLException {
        throw new SQLException("Could not parse argument list \"" + argumentsString + "\". Unexpected token \"" + token + "\" found.");
    }

    private static void throwParseException(String argumentsString, String token, String customMessage) throws SQLException {
        throw new SQLException("Could not parse argument list \"" + argumentsString + "\". Unexpected token \"" + token + "\" found. " + customMessage);
    }

    public boolean next() throws SQLException {
        currentArgument = arguments.hasNext() ? arguments.next() : null;
        return currentArgument != null;
    }

    public String getString(String columnLabel) throws SQLException {
        return
            columnLabel.equals("ARGUMENT_NAME") ? currentArgument.name :
            columnLabel.equals("METHOD_NAME") ? currentArgument.methodName :
            columnLabel.equals("METHOD_TYPE") ? currentArgument.methodType :        
            columnLabel.equals("IN_OUT") ? currentArgument.inOut :        
            columnLabel.equals("DATA_TYPE_NAME") ? currentArgument.dataTypeName : null;
    }

    public int getInt(String columnLabel) throws SQLException {
        return
            columnLabel.equals("POSITION") ? currentArgument.position :
            columnLabel.equals("SEQUENCE") ? currentArgument.position :
            columnLabel.equals("DATA_PRECISION") ? (currentArgument.dataPrecision == null ? 0 : currentArgument.dataPrecision) :
            columnLabel.equals("DATA_SCALE") ? (currentArgument.dataScale == null ? 0 : currentArgument.dataScale) : 0;
    }

    public long getLong(String columnLabel) throws SQLException {
        return getInt(columnLabel);
    }
}
