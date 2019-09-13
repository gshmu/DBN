package com.dci.intellij.dbn.connection.config.tns;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import oracle.net.jdbc.nl.NLException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dci.intellij.dbn.common.util.CommonUtil.nvln;

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


    public static List<TnsName> parse(File file) throws Exception {
        List<TnsName> tnsNames = new ArrayList<>();
        String tnsContent = FileUtils.readFileToString(file, Charset.defaultCharset());

        Pattern pattern = TnsNamesPattern.INSTANCE.get();
        Matcher matcher = pattern.matcher(tnsContent);

        int start = 0;
        while (matcher.find(start)) {
            String schema         = matcher.group("schema");
            String protocol       = nvln(matcher.group("protocol"), matcher.group("lprotocol"));
            String host           = nvln(matcher.group("host"), matcher.group("lhost"));
            String port           = nvln(matcher.group("port"), matcher.group("lport"));
            String server         = matcher.group("server");
            String sid            = matcher.group("sid");
            String serviceName    = matcher.group("servicename");
            String globalName     = matcher.group("globalname");
            String failover   = matcher.group("failover");
            String failoverType   = matcher.group("failovertype");
            String failoverMethod = matcher.group("failovermethod");
            start = matcher.end();

            if (StringUtil.isNotEmpty(schema)) {
                TnsName tnsName = new TnsName(
                        schema,
                        protocol,
                        host,
                        port,
                        server,
                        sid,
                        serviceName,
                        globalName,
                        failover,
                        failoverType,
                        failoverMethod);
                tnsNames.add(tnsName);
            }
       }
        return tnsNames;
    }


    public static class TNSNamesList {
        public static void main(String[] args) throws NLException, IOException {

            Pattern pattern = TnsNamesPattern.INSTANCE.get();
            Matcher matcher = pattern.matcher(TNS1);

            int start = 0;
            while (matcher.find(start)) {
                int count = matcher.groupCount();
/*
                for (int i=0; i<count; i++) {
                    System.out.println("------------------------");
                    System.out.println(matcher.group(i));
                }
*/
                System.out.println("SCHEMA:       " + matcher.group("schema"));
                System.out.println("PROTOCOL:     " + matcher.group("protocol"));
                System.out.println("HOST:         " + matcher.group("host"));
                System.out.println("PORT:         " + matcher.group("port"));
                System.out.println("PROTOCOL:     " + matcher.group("lprotocol"));
                System.out.println("HOST:         " + matcher.group("lhost"));
                System.out.println("PORT:         " + matcher.group("lport"));
                System.out.println("SERVER:       " + matcher.group("server"));
                System.out.println("SID:          " + matcher.group("sid"));
                System.out.println("SERVICE_NAME: " + matcher.group("servicename"));
                System.out.println("GLOBAL_NAME:  " + matcher.group("globalname"));
                System.out.println("------------------------\n");
                start = matcher.end();
            }

            System.out.println(matcher.matches());
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
            "      (ADDRESS = (PROTOCOL = TCP)(HOST = LOCALHOST1)(PORT = 12345))\n" +
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
            "      (GLOBAL_ABC = APYREQ1A.EQ)\n" +
            "    )\n" +
            "  )";

    private static String TNSBLA = "SOME_NAME_OF_AN_ENTRY=(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=HOST_NAME)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=DB_NAME)))";
}
