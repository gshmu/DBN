package com.dci.intellij.dbn.debugger.jdwp;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.sun.jdi.Location;
import org.jetbrains.annotations.Nullable;

public interface DBJdwpDebugUtil {
    Logger LOGGER = LoggerFactory.createLogger();

    @Nullable
    default String getOwnerName(@Nullable Location location) {
        try {
            if (location != null) {
                String sourceUrl = location.sourcePath();
                DBJdwpSourcePath sourcePath = DBJdwpSourcePath.from(sourceUrl);
                return sourcePath.getProgramOwner();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to resolve owner name", e);
        }

        return null;
    }
}
