package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class DatabaseDriverManager implements ApplicationComponent {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private static Map<DatabaseType, String> INTERNAL_LIB_MAP = new HashMap<>();
    static {
        INTERNAL_LIB_MAP.put(DatabaseType.MYSQL, "mysql-connector-java-8.0.15.jar");
        INTERNAL_LIB_MAP.put(DatabaseType.SQLITE, "sqlite-jdbc-3.23.1.jar");
        INTERNAL_LIB_MAP.put(DatabaseType.POSTGRES, "postgresql-42.2.5.jar");
    }


    private Map<String, List<Driver>> driversCache = new HashMap<>();

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

    @Override
    public void initComponent() {}
    @Override
    public void disposeComponent() {}

    /**
     *
     * @param libraryName jar library path or directory containing driver/s and all required dependencies
     * @param force reload isolated classloader with (updated) drivers without restart IDE
     *                                   and no need to create a new connection definition
     */
    public List<Driver> loadDrivers(String libraryName, boolean force) {

        File libraryFile = new File(libraryName);
        List<Driver> drivers = new ArrayList<>();
        try {
            ClassLoader parentClassLoader = getClass().getClassLoader();
            if (libraryFile.isFile()) {
                URL[] urls = new URL[]{libraryFile.toURI().toURL()};
                // creates an isolated classloader, parent = null
                URLClassLoader classLoader = URLClassLoader.newInstance(urls, parentClassLoader);
                return loadDriversJar(libraryName, classLoader, force);
            } else {
                File[] directoryListing = libraryFile.listFiles();
                List<URL> urls = new ArrayList<>();
                if (directoryListing != null) {
                    // build classpath with all found jars
                    for (File child : directoryListing) {
                        if (child.getName().endsWith(".jar")) {
                            urls.add(child.toURI().toURL());
                        }
                    }
                    // creates an isolated classloader, parent = null
                    URLClassLoader classLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]), parentClassLoader);
                    // find and load drivers
                    for (File child : directoryListing) {
                        if (child.getName().endsWith(".jar")) {
                            List<Driver> drvs = loadDriversJar(child.getAbsolutePath(), classLoader, force);
                            if (drvs != null) {
                                drivers.addAll(drvs);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error loading drivers from library " + libraryName, e);
        }
        return drivers;
    }

    private List<Driver> loadDriversJar(String libraryName, final URLClassLoader classLoader, boolean force) {
        File libraryFile = new File(libraryName);

        if (libraryFile.isFile()) {
            Synchronized.run(this,
                    () -> !driversCache.containsKey(libraryName) || force,
                    () -> {
                        if (driversCache.containsKey(libraryName) && force){
                            try{
                                // clean up old classloader opened resources
                                List<Driver> drivers = driversCache.get(libraryName);
                                if (!drivers.isEmpty()) {
                                    ClassLoader cl = drivers.get(0).getClass().getClassLoader();
                                    if (cl instanceof URLClassLoader) {
                                        try {
                                            ((URLClassLoader)cl).close();
                                        } catch (IOException e) {
                                            //ignored
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                //ignored
                            }
                        }

                        String taskDescription = ProgressMonitor.getTaskDescription();
                        ProgressMonitor.setTaskDescription("Loading jdbc drivers from " + libraryName);
                        try {
                            List<Driver> drivers = new ArrayList<>();
                            JarFile jarFile = new JarFile(libraryName);
                            Enumeration<JarEntry> entries = jarFile.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();

                                //if (entry.isDirectory()) continue;

                                String name = entry.getName();
                                if (name.endsWith(".class")) {
                                    String className = name.replaceAll("/", "\\.");
                                    className = className.substring(0, className.length() - 6);
                                    try {
                                        // unsafe but fast driver loading, class name must contain "Driver"
                                        // TODO really "unsafe" assuming all drivers contain the word "Driver" in the class name
                                        //String[] clsTokens = className.split("\\.");
                                        //if (clsTokens[clsTokens.length-1].toLowerCase().contains("driver")) {
                                            Class<?> clazz = classLoader.loadClass(className);
                                            if (Driver.class.isAssignableFrom(clazz)) {
                                                Driver driver = (Driver)clazz.newInstance();
                                                drivers.add(driver);
                                            }
                                        //}
                                    } catch (Throwable throwable) {
                                        LOGGER.debug("Error loading driver "+className+" from library " + libraryName, throwable);
                                    }
                                }
                            }
                            driversCache.put(libraryName, drivers);
                        } catch (Exception e) {
                            LOGGER.warn("Error loading drivers from library " + libraryName, e);
                        } finally {
                            ProgressMonitor.setTaskDescription(taskDescription);
                        }
                    });
        }
        return driversCache.get(libraryName);
    }

    @Nullable
    public Driver getDriver(String className) throws Exception {
        try {
            Class<Driver> driverClass = (Class<Driver>) Class.forName(className);
            return driverClass.newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public String getInternalDriverLibrary(DatabaseType databaseType) throws Exception{
        String driverLibrary = INTERNAL_LIB_MAP.get(databaseType);
        LOGGER.info("Loading driver library " + driverLibrary);
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource("/" + driverLibrary);
        if (url == null) {
            LOGGER.warn("Failed to load driver library " + driverLibrary);
        } else {
            LOGGER.info("Loaded driver file " + url.getPath());
        }
        return url == null ? null : new File(url.toURI()).getPath();
    }

    public Driver getDriver(String libraryName, String className) throws Exception {
        if (StringUtil.isEmptyOrSpaces(className)) {
            throw new Exception("No driver class specified.");
        }
        if (new File(libraryName).exists()) {
            List<Driver> drivers = loadDrivers(libraryName, false);
            for (Driver driver : drivers) {
                if (driver.getClass().getName().equals(className)) {
                    return driver;
                }
            }
        } else {
            throw new Exception("Could not find library \"" + libraryName +"\".");
        }
        throw new Exception("Could not locate driver \"" + className + "\" in library \"" + libraryName + "\"");
/*        ClassLoader classLoader = classLoaders.get(libraryName);
        try {
            return (Driver) Class.forName(className, true, classLoader).newInstance();
        } catch (Exception e) {
            throw new Exception(
                    "Could not load class \"" + className + "\" " +
                    "from library \"" + libraryName + "\". " +
                    "[" + NamingUtil.getClassName(e.getClass()) + "] " + e.getMessage());
        }*/
    }

    public static void main(String[] args) {
        DatabaseDriverManager m = new DatabaseDriverManager();
        File file = new File("D:\\Projects\\DBNavigator\\lib\\classes12.jar");
        List<Driver> drivers = m.loadDrivers(file.getPath(), false);
    }
}
