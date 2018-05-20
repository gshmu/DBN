package com.dci.intellij.dbn.connection.config.tns;


import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TnsName implements Comparable<TnsName>{

    private String name;
    private String host;
    private String port;
    private String sid;

    public static TnsName createTnsName(String name, Map details) {
        TnsName tns = new TnsName();
        tns.name = name;
        tns.host = get( new String[] { "DESCRIPTION", "ADDRESS_LIST", "ADDRESS", "HOST" }, details);
        if(tns.host == null) {
            tns.host = get( new String[] { "DESCRIPTION", "ADDRESS", "HOST" }, details);
        }
        tns.port = get( new String[] { "DESCRIPTION", "ADDRESS_LIST", "ADDRESS", "PORT" }, details);
        if(tns.port == null) {
            tns.port = get( new String[] { "DESCRIPTION", "ADDRESS", "PORT" }, details);
        }
        tns.sid = get( new String[] { "DESCRIPTION", "CONNECT_DATA", "SID" }, details);
        if(tns.sid == null) {
            tns.sid = get( new String[] { "DESCRIPTION", "CONNECT_DATA", "SERVICE_NAME" }, details);
            int endIndex = tns.sid.indexOf('.');
            tns.sid = endIndex == -1 ? tns.sid.substring( 0) : tns.sid.substring( 0, endIndex);
        }
        return tns;
    }

    private static String get(String[] keys, Map map) {
        for(int i=0; i<(keys.length-1); i++) {
            map = (Map) map.get(keys[i]);
            if(map == null) {
                return null;
            }
        }
        return (String) map.get(keys[keys.length-1]);
    }

    public String getHost() {
        return host;
    }
    public String getName() {
        return name;
    }
    public String getPort() {
        return port;
    }
    public String getSid() {
        return sid;
    }

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull TnsName o) {
        return name.compareTo(o.name);
    }
}
