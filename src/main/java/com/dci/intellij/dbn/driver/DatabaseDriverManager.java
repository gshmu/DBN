package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.component.ApplicationComponentBase;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.util.Files;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.DatabaseType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.component.Components.applicationService;

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
    private final Map<DatabaseType, DriverBundle> bundledDriversCache = new ConcurrentHashMap<>();

    public static DatabaseDriverManager getInstance() {
        return applicationService(DatabaseDriverManager.class);
    }

    public DatabaseDriverManager() {
        super("DBNavigator.DatabaseDriverManager");
        //TODO make this configurable
        DriverManager.setLoginTimeout(30);
    }

    public DriverBundle loadDrivers(File libraryFile, boolean force) {
        try {
            if (force) {
                DriverBundle drivers = driversCache.remove(libraryFile);
                Disposer.dispose(drivers);
            }
            return driversCache.computeIfAbsent(libraryFile, f -> new DriverBundle(f));
        } catch (Exception e) {
            log.warn("failed to load drivers from library " + libraryFile, e);
            throw e;
        }
    }

    @Nullable
    @SneakyThrows
    private DriverBundle loadBundledDrivers(DatabaseType databaseType) {
        String libraryRoot = "bundled-jdbc-" + databaseType.name().toLowerCase();
        log.info("Loading driver library " + libraryRoot);

        File deploymentRoot = Files.getPluginDeploymentRoot();
        File library = Files.findFileRecursively(deploymentRoot, libraryRoot);
        if (library != null) return loadDrivers(library, false);


/*
        // TODO attempt to load bundled libraries from within instrumented DBN jar
        File pluginLibrary = getPluginLibrary();
        if (pluginLibrary != null) {
            return new DriverBundle(pluginLibrary, libraryRoot);
        }
*/

        return null;
    }

    @SneakyThrows
    private File getPluginLibrary() {
        Class clazz = getClass();
        URL classResource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (classResource == null) return null;

        String url = classResource.toString();
        if (!url.startsWith("jar:file:")) return null;

        String path = url.replaceAll("^jar:(file:.*[.]jar)!/.*", "$1");
        return new File(new URL(path).toURI());
    }

    @SneakyThrows
    public DriverBundle getBundledDrivers(DatabaseType databaseType) {
        return bundledDriversCache.computeIfAbsent(databaseType, dt -> loadBundledDrivers(databaseType));
    }

    public DriverBundle getDrivers(File libraryFile) throws Exception {
        if (libraryFile.exists()) {
            return loadDrivers(libraryFile, false);
        } else {
            throw new Exception("Could not find library \"" + libraryFile.getAbsolutePath() +"\".");
        }
    }

    @SneakyThrows
    public Driver getDriver(File libraryFile, String className) {
        if (Strings.isEmptyOrSpaces(className)) throw new Exception("No driver class specified.");

        DriverBundle drivers = getDrivers(libraryFile);
        Driver driver = drivers.getDriver(className);
        if (driver == null) throw new Exception("Could not locate driver \"" + className + "\" in library \"" + libraryFile.getAbsolutePath() + "\"");

        return driver;
    }
}
