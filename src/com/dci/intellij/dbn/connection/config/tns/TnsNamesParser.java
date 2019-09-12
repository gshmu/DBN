package com.dci.intellij.dbn.connection.config.tns;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import oracle.net.jdbc.nl.NLException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TnsNamesParser {
    public static final FileChooserDescriptor FILE_CHOOSER_DESCRIPTOR = new FileChooserDescriptor(true, false, false, false, false, false).
            withTitle("Select TNS Names File").
            withDescription("Select a valid Oracle tnsnames.ora file").
            withFileFilter(new Condition<VirtualFile>() {
                @Override
                public boolean value(VirtualFile virtualFile) {
                    String extension = virtualFile.getExtension();
                    return extension != null && extension.equals("ora");
                }
            });

    public static TnsName[] parse(File file) throws Exception {
        // Begin by treating the file as separate lines to throw out the comments
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder tnsText = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#") && !line.equals("")) {
                tnsText.append(line);
            }
        }

        // Now switch to a streaming parser to get the actual data
        Map tnsNamesMap = new HashMap();

        // used to ascertain whether we are awaiting the RHS of an =
        boolean parsingValue = false;
        // used to indicate that we have finished a block and should either start
        // a new sibling block, or start a new tns block
        boolean endBlock = false;
        StringBuilder currentTnsKey = new StringBuilder();
        StringBuilder currentTnsValue = new StringBuilder();
        Map currentMap = tnsNamesMap;
        char[] tnsChars = tnsText.toString().toCharArray();
        Stack<Map> mapStack = new Stack<Map>();
        for (char ch : tnsChars) {
            switch (ch) {
                case ' ': {
                    break;
                }
                case '=': {
                    parsingValue = true;
                    break;
                }
                case '(': {
                    if (endBlock) {
                        endBlock = false;
                    }
                    if (parsingValue) {
                        Map newMap = new HashMap();
                        currentMap.put(currentTnsKey.toString().toUpperCase(), newMap);
                        currentTnsKey.setLength(0);
                        mapStack.push(currentMap);
                        currentMap = newMap;
                        parsingValue = false;
                    }
                    break;
                }
                case ')': {
                    if (parsingValue) {
                        currentMap.put(currentTnsKey.toString().toUpperCase(), currentTnsValue.toString());
                        currentTnsKey.setLength(0);
                        currentTnsValue.setLength(0);
                        parsingValue = false;
                        endBlock = true;
                    } else {
                        currentMap = mapStack.pop();
                    }
                    break;
                }
                default: {
                    if (parsingValue) {
                        currentTnsValue.append(ch);
                    } else {
                        if (endBlock) {
                            currentMap = mapStack.pop();
                            endBlock = false;
                        }
                        currentTnsKey.append(ch);
                    }
                    break;
                }
            }
        }

        TnsName[] tnsNames = new TnsName[tnsNamesMap.size()];

        Iterator iterator = tnsNamesMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            Map details = (Map) tnsNamesMap.get(name);
            tnsNames[i] = TnsName.createTnsName(name, details);
            i++;
        }

        return tnsNames;
    }


    public static class TNSNamesList {

        public static void main(String[] args) throws NLException, IOException {
            //String value = "[._A-Z0-9]+";
            String value = "[^\\s\\(\\)=]+";
            String protocol = block(keyValue("PROTOCOL", value, "protocol"));
            String host = block(keyValue("HOST", value, "host"));
            String port = block(keyValue("PORT", value, "port"));

            String sid = block(keyValue("SID", value, "sid"));
            String server = block(keyValue("SERVER", value, "server"));
            String serviceName = block(keyValue("SERVICE_NAME", value, "service_name"));
            String globalName = block(keyValue("GLOBAL_NAME", value, "global_name"));
            String any = block(keyValue("[_A-Z]+", value));

            String address = block(keyValue("ADDRESS", iteration(protocol, host, port/*, any*/)));
            String addressList = block(keyValue("ADDRESS_LIST", iteration(address)));
            String connectData = block(keyValue("CONNECT_DATA", iteration(sid, server, serviceName, globalName/*, any*/)));
            String description = block(keyValue("DESCRIPTION", iteration(address, addressList, connectData)));
            String block = keyValue(group("schema", value), description);
            String group = "^" + block + "$";

            Pattern pattern = Pattern.compile(group, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(TNS);

            int start = 0;
            while (matcher.find(start)) {
                System.out.println(matcher.group("schema"));
                start = matcher.end();
                System.out.println(matcher.end());
            }

            System.out.println(matcher.matches());
        }

        private static String block(String content) {
            return "\\(\\s*" + content + "\\s*\\)";
        }

        private static String keyValue(String key, String value) {
            return keyValue(key, value, null);
        }

        private static String keyValue(String key, String value, String group) {
            return group == null ?
                    key + "\\s*=\\s*" + value:
                    key + "\\s*=\\s*" + group(group, value);
        }

        private static String group(String name, String content) {
            return "(?<" + name + ">" + content + ")";
        }

        private static String iteration(String ... contents) {
            StringBuilder result = new StringBuilder();
            for (String content : contents) {
                if (result.length() > 0) {
                    result.append("|");
                }
                result.append(content);
            }

            return "[" + result + "]*";
        }
    }

    private static String TNS = "SOMESCHEMA =\n" +
            "  (DESCRIPTION =\n" +
            "    (ADDRESS_LIST =\n" +
            "      (ADDRESS = (PROTOCOL = TCP)(HOST = REMOTEHOST)(PORT = 1234))\n" +
            "    )\n" +
            "    (CONNECT_DATA = (SERVICE_NAME = REMOTE)\n" +
            "    )\n" +
            "  )\n" +
            "\n" +
            "MYSCHEMA =\n" +
            "  (DESCRIPTION =\n" +
            "    (ADDRESS = (PROTOCOL = TCP)(HOST = MYHOST)(PORT = 1234))\n" +
            "    (CONNECT_DATA =\n" +
            "      (SERVER = DEDICATED)\n" +
            "      (SERVICE_NAME = MYSERVICE.LOCAL )\n" +
            "    )\n" +
            "  )\n" +
            "\n" +
            "MYOTHERSCHEMA =\n" +
            "  (DESCRIPTION =\n" +
            "    (ADDRESS_LIST =\n" +
            "      (ADDRESS = (PROTOCOL = TCP)(HOST = MYHOST)(PORT = 1234))\n" +
            "    )\n" +
            "    (CONNECT_DATA = \n" +
            "      (SERVICE_NAME = MYSERVICE.REMOTE)\n" +
            "    )\n" +
            "\n" +
            "  )\n" +
            "\n" +
            "SOMEOTHERSCHEMA = \n" +
            "  (DESCRIPTION =\n" +
            "    (ADDRESS_LIST =\n" +
            "      (ADDRESS = (PROTOCOL = TCP)(HOST = LOCALHOST)(PORT = 1234))\n" +
            "    )\n" +
            "    (CONNECT_DATA =\n" +
            "      (SERVICE_NAME = LOCAL)\n" +
            "    )\n" +
            "  )";
    private static String TNS1 = "APYREQ1A.EQ =\n" +
            "  (DESCRIPTION =\n" +
            "    (ADDRESS = (PROTOCOL = TCP)(Host = sb007538.equateplus.net)(Port = 49350))\n" +
            "    (CONNECT_DATA =\n" +
            "      (SID = APYREQ1A)\n" +
            "      (GLOBAL_NAME = APYREQ1A.EQ)\n" +
            "    )\n" +
            "  )";
}
