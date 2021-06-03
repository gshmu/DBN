package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.diagnostic.Logger;

import java.util.Base64;

public class PasswordUtil {
    public static final Logger LOGGER = LoggerFactory.createLogger();

    public static String encodePassword(String password) {
        try {
            password = StringUtil.isEmpty(password) ? "" : new String(Base64.getEncoder().encode(nvl(password).getBytes()));
        } catch (Exception e) {
            // any exception would break the logic storing the connection settings
            LOGGER.error("Error encoding password", e);
        }
        return password;
    }

    public static String decodePassword(String password) {
        try {
            password = StringUtil.isEmpty(password) ? "" : new String(Base64.getDecoder().decode(nvl(password).getBytes()));
        } catch (Exception e) {
            // password may not be encoded yet
        }

        return password;
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }

}
