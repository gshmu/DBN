package com.dci.intellij.dbn.connection.config.tns;

import java.util.regex.Pattern;

public class TnsNamesPattern {
    public static TnsNamesPattern INSTANCE = new TnsNamesPattern();

    private Pattern pattern;

    private TnsNamesPattern() {
        String value = "[A-Z0-9._]+";
        String protocol =   keyValue("PROTOCOL", group("protocol", value) );
        String host =       keyValue("HOST",     group("host", value));
        String port =       keyValue("PORT",     group("port", value));
        String l_protocol = keyValue("PROTOCOL", group("lprotocol", value));
        String l_host =     keyValue("HOST",     group("lhost", value));
        String l_port =     keyValue("PORT",     group("lport", value));

        String sid =         keyValue("SID",          group("sid", value));
        String server =      keyValue("SERVER",       group("server", value));
        String serviceName = keyValue("SERVICE_NAME", group("servicename", value) );
        String globalName =  keyValue("GLOBAL_NAME",  group("globalname", value));
        String any =         keyValue("[_A-Z]+",      value);

        String address =     keyValue("ADDRESS",      iteration(block(oneOf(protocol, host, port, any))));
        String l_address =   keyValue("ADDRESS",      iteration(block(oneOf(l_protocol, l_host, l_port, any))));
        String addressList = keyValue("ADDRESS_LIST", iteration(block(l_address)));
        String connectData = keyValue("CONNECT_DATA", iteration(block(oneOf(sid, server, serviceName, globalName, any))));
        String description = keyValue("DESCRIPTION",  iteration(block(oneOf(address, addressList, connectData))));
        String block =       keyValue(group("schema", value), block(description));

        pattern = Pattern.compile(block, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    }

    public Pattern get() {
        return pattern;
    }

    private static String block(String content) {
        return "[(]\\s*" + content + "\\s*[)]";
    }

    private static String keyValue(String key, String value) {
        return key + "\\s*=\\s*" + value;
    }

    private static String group(String name, String content) {
        return "(?<" + name + ">" + content + ")";
    }

    private static String oneOf(String ... contents) {
        StringBuilder result = new StringBuilder();
        for (String content : contents) {
            if (result.length() > 0) {
                result.append("|");
            }
            result.append("(").append(content).append(")");
        }

        return "(" + result + ")";
    }
    private static String iteration(String content) {
        return "(" + content + "\\s*)*";
    }
}
