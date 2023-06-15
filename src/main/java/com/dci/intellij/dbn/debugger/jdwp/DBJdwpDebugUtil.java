package com.dci.intellij.dbn.debugger.jdwp;

import com.sun.jdi.Location;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;

@Slf4j
public final class DBJdwpDebugUtil {
    private DBJdwpDebugUtil() {}

    @Nullable
    public static String getOwnerName(@Nullable Location location) {
        try {
            if (location != null) {
                String sourceUrl = location.sourcePath();
                DBJdwpSourcePath sourcePath = DBJdwpSourcePath.from(sourceUrl);
                return sourcePath.getProgramOwner();
            }
        } catch (Exception e) {
            conditionallyLog(e);
            log.error("Failed to resolve owner name", e);
        }

        return null;
    }
}
