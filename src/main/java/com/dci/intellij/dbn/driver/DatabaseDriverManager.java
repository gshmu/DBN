package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.component.ApplicationComponentBase;
import com.dci.intellij.dbn.common.util.Files;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.DatabaseType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.component.Components.applicationService;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;

/**
 * JDBC Driver loader.
 * <p>
 * Features:
 * <ol>
 * <li>Supports single JDBC driver library</li>
 * <li>Supports directory scanning for JDBC drivers</li>
 * <li>Isolated classloader</li>
 * <li>Reload JDBC driver libraries</li>
 * </ol>
 */
@Slf4j
public class DatabaseDriverManager extends ApplicationComponentBase {
    private final Map<File, DriverBundle> driversCache = new ConcurrentHashMap<>();
    private final Map<DatabaseType, File> internalLibraryCache = new ConcurrentHashMap<>();

    public static DatabaseDriverManager getInstance() {
        return applicationService(DatabaseDriverManager.class);
    }

    public DatabaseDriverManager() {
        super("DBNavigator.DatabaseDriverManager");
        //TODO make this configurable
        DriverManager.setLoginTimeout(30);
    }

    public DriverBundle loadDrivers(File libraryFile, boolean force) {
        try{
            if (force) {
                DriverBundle drivers = driversCache.remove(libraryFile);
                drivers.dispose();
            }
            return driversCache.computeIfAbsent(libraryFile, f -> new DriverBundle(f));
        } catch (Exception e) {
            log.warn("failed to load drivers from library " + libraryFile, e);
            throw e;
        }
    }

    @Nullable
    public Driver getDriver(String className, boolean internal) {
        try {
            Class<Driver> driverClass = cast(Class.forName(className));
            if (internal || driverClass.getClassLoader() instanceof DriverClassLoader) {
                return driverClass.getDeclaredConstructor().newInstance();
            }

        } catch (Throwable ignore) {}

        return null;
    }

    public File getInternalDriverLibrary(DatabaseType databaseType) {
        return internalLibraryCache.computeIfAbsent(databaseType, dt -> {
            String driverLibrary = "bundled-jdbc-" + databaseType.name().toLowerCase();
            log.info("Loading driver library " + driverLibrary);

            File deploymentRoot = Files.getPluginDeploymentRoot();
            return Files.findFileRecursively(deploymentRoot, driverLibrary);
        });
    }

    public Driver getDriver(File libraryFile, String className) throws Exception {
        if (Strings.isEmptyOrSpaces(className)) {
            throw new Exception("No driver class specified.");
        }
        if (libraryFile.exists()) {
            DriverBundle drivers = loadDrivers(libraryFile, false);
            Driver driver = drivers.getDriver(className);
            if (driver != null) return driver;
        } else {
            throw new Exception("Could not find library \"" + libraryFile.getAbsolutePath() +"\".");
        }
        throw new Exception("Could not locate driver \"" + className + "\" in library \"" + libraryFile.getAbsolutePath() + "\"");
    }
}
