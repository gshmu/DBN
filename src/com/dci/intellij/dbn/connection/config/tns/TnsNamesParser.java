package com.dci.intellij.dbn.connection.config.tns;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dci.intellij.dbn.common.util.CommonUtil.nvlf;

public class TnsNamesParser {
    public static final FileChooserDescriptor FILE_CHOOSER_DESCRIPTOR = new FileChooserDescriptor(true, false, false, false, false, false).
            withTitle("Select TNS Names File").
            withDescription("Select a valid Oracle tnsnames.ora file").
            withFileFilter(new Condition<VirtualFile>() {
                @Override
                public boolean value(VirtualFile virtualFile) {
                    String extension = virtualFile.getExtension();
                    return Objects.equals(extension, "ora");
                }
            });


    public static List<TnsName> parse(File file) throws Exception {
        List<TnsName> tnsNames = new ArrayList<>();
        String tnsContent = new String(Files.readAllBytes(Paths.get(file.getPath())));

        Pattern pattern = TnsNamesPattern.INSTANCE.get();
        Matcher matcher = pattern.matcher(tnsContent);

        int start = 0;
        while (matcher.find(start)) {
            String schema         = matcher.group("schema");
            String protocol       = nvlf(matcher.group("protocol1"), matcher.group("protocol2"), matcher.group("protocol3"));
            String host           = nvlf(matcher.group("host1"), matcher.group("host2"), matcher.group("host3"));
            String port           = nvlf(matcher.group("port1"), matcher.group("port2"), matcher.group("port3"));
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
}
