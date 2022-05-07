package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.component.ApplicationComponent;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.Files;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.openapi.application.ApplicationManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * JDBC Driver loader.
 *
 * Features:
 *
 * <ol>
 * <li>Supports single JDBC driver library</li>
 * <li>Supports directory scanning for JDBC drivers</li>
 * <li>Isolated classloader</li>
 * <li>Reload JDBC driver libraries</li>
 * </ol>
 */
@Slf4j
public class DatabaseDriverManager implements ApplicationComponent {

    private static final Map<DatabaseType, String> BUNDLED_LIBS = new HashMap<>();
    static {
        BUNDLED_LIBS.put(DatabaseType.MYSQL, "mysql-connector-java-8.0.19.jar");
        BUNDLED_LIBS.put(DatabaseType.SQLITE, "sqlite-jdbc-3.30.1.jar");
        BUNDLED_LIBS.put(DatabaseType.POSTGRES, "postgresql-42.2.12.jar");
    }


    private final Map<File, List<Driver>> driversCache = new ConcurrentHashMap<>();

    public static DatabaseDriverManager getInstance() {
        return ApplicationManager.getApplication().getComponent(DatabaseDriverManager.class);
    }

    public DatabaseDriverManager() {
        //TODO make this configurable
        DriverManager.setLoginTimeout(30);
    }

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.DatabaseDriverManager";
    }

    public List<Driver> loadDrivers(File libraryFile, boolean force) throws Exception{
        try{
            if (force) {
                List<Driver> drivers = driversCache.remove(libraryFile);
                disposeClassLoader(drivers);
            }

            return driversCache.computeIfAbsent(libraryFile, f -> loadDrivers(f));
        } catch (Exception e) {
            log.warn("failed to load drivers from library " + libraryFile, e);
            throw e;
        }
    }

    private static void disposeClassLoader(List<Driver> drivers) {
        try {
            if (drivers != null && !drivers.isEmpty()) {
                ClassLoader classLoader = drivers.get(0).getClass().getClassLoader();
                if (classLoader instanceof DriverClassLoader) {
                    DriverClassLoader driverClassLoader = (DriverClassLoader) classLoader;
                    driverClassLoader.close();
                }
            }
        } catch (Throwable t) {
            log.warn("Failed to dispose class loader", t);
        }
    }

    private List<Driver> loadDrivers(File libraryFile) {
        String taskDescription = ProgressMonitor.getTaskDescription();
        try {
            ProgressMonitor.setTaskDescription("Loading jdbc drivers from " + libraryFile);

            ClassLoader parentClassLoader = getClass().getClassLoader();
            if (libraryFile.isDirectory()) {
                List<Driver> drivers = new ArrayList<>();
                File[] files = libraryFile.listFiles();
                if (files != null) {
                    URL[] urls = Arrays.
                            stream(files).
                            filter(file -> file.getName().endsWith(".jar")).
                            map(file -> getFileUrl(file)).
                            toArray(URL[]::new);

                    ClassLoader classLoader = new DriverClassLoader(urls, parentClassLoader);
                    for (File file : files) {
                        drivers.addAll(loadDrivers(file, classLoader));
                    }
                }

                return drivers;
            } else {
                URL[] urls = new URL[]{getFileUrl(libraryFile)};
                ClassLoader classLoader = new DriverClassLoader(urls, parentClassLoader);
                return loadDrivers(libraryFile, classLoader);
            }

        } finally {
            ProgressMonitor.setTaskDescription(taskDescription);
        }
    }

    @NotNull
    @SneakyThrows
    private URL getFileUrl(File file) {
        return file.toURI().toURL();
    }

    private static List<Driver> loadDrivers(File libraryFile, ClassLoader classLoader) {
        List<Driver> drivers = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(libraryFile);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String className = name.replaceAll("/", "\\.");
                    className = className.substring(0, className.length() - 6);
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (Driver.class.isAssignableFrom(clazz)) {
                            Driver driver = (Driver) clazz.getDeclaredConstructor().newInstance();
                            drivers.add(driver);
                        }
                    } catch (Throwable t) {
                        log.debug("Failed to load driver " + className + " from library " + libraryFile, t);
                    }
                }
            }
        } catch (Throwable t) {
            log.debug("Failed to load drivers from library " + libraryFile, t);
        }
        return drivers;
    }

    @Nullable
    public Driver getDriver(String className, boolean internal) {
        try {
            Class<Driver> driverClass = (Class<Driver>) Class.forName(className);
            if (internal || driverClass.getClassLoader() instanceof DriverClassLoader) {
                return driverClass.getDeclaredConstructor().newInstance();
            }

        } catch (Throwable ignore) {}

        return null;
    }

    public File getInternalDriverLibrary(DatabaseType databaseType) throws Exception{
        String driverLibrary = BUNDLED_LIBS.get(databaseType);
        log.info("Loading driver library " + driverLibrary);

        File deploymentRoot = Files.getPluginDeploymentRoot();
        return Files.findFileRecursively(deploymentRoot, driverLibrary);
    }

    public Driver getDriver(File libraryFile, String className) throws Exception {
        if (Strings.isEmptyOrSpaces(className)) {
            throw new Exception("No driver class specified.");
        }
        if (libraryFile.exists()) {
            List<Driver> drivers = loadDrivers(libraryFile, false);
            for (Driver driver : drivers) {
                if (Objects.equals(driver.getClass().getName(), className)) {
                    return driver;
                }
            }
        } else {
            throw new Exception("Could not find library \"" + libraryFile.getAbsolutePath() +"\".");
        }
        throw new Exception("Could not locate driver \"" + className + "\" in library \"" + libraryFile.getAbsolutePath() + "\"");
    }

    public static void main(String[] args) throws Exception {
        DatabaseDriverManager m = new DatabaseDriverManager();
        File file = new File("D:\\Projects\\DBNavigator\\lib\\classes12.jar");
        List<Driver> drivers = m.loadDrivers(file, false);
    }
}
