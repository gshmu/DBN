package com.dci.intellij.dbn.driver;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.SystemProperties;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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

public class DatabaseDriverManager implements ApplicationComponent {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private static Map<DatabaseType, Map<String, String>> INTERNAL_LIB_MAP = new HashMap<>();
    static {
        HashMap<String, String> mysql = new HashMap<>();
        //mysql.put("1.", "mysql-connector-java-5.1.35-bin.jar");
        mysql.put("1.", "mysql-connector-java-8.0.15.jar");
        INTERNAL_LIB_MAP.put(DatabaseType.MYSQL, mysql);

        HashMap<String, String> sqlite = new HashMap<>();
        sqlite.put("1.", "sqlite-jdbc-3.23.1.jar");
        INTERNAL_LIB_MAP.put(DatabaseType.SQLITE, sqlite);

        HashMap<String, String> postgres = new HashMap<>();
        postgres.put("1.", "postgresql-42.2.5.jar");
        INTERNAL_LIB_MAP.put(DatabaseType.POSTGRES, postgres);
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

    public List<Driver> loadDriverClassesWithProgressBar(String libraryName) {
        LoaderThread loader = new LoaderThread(libraryName);
        Project project = ActionUtil.getProject();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(loader, Constants.DBN_TITLE_PREFIX + "Loading database drivers" , false, project);
        return loader.getDrivers();
    }

    class LoaderThread implements Runnable{
        public LoaderThread(String libraryName) {
            this.libraryName = libraryName;
        }

        List<Driver> drivers;
        String libraryName;
        @Override
        public void run() {
            drivers = loadDrivers(libraryName);
        }
        public List<Driver> getDrivers() {
                return drivers;
        }
    }


    public List<Driver> loadDrivers(String libraryName) {
        File libraryFile = new File(libraryName);

        if (libraryFile.isFile()) {
            Synchronized.run(this,
                    () -> !driversCache.containsKey(libraryName),
                    () -> {
                        String taskDescription = ProgressMonitor.getTaskDescription();
                        ProgressMonitor.setTaskDescription("Loading jdbc drivers from " + libraryName);
                        try {
                            List<Driver> drivers = new ArrayList<Driver>();
                            URL[] urls = new URL[]{libraryFile.toURI().toURL()};
                            URLClassLoader classLoader = URLClassLoader.newInstance(urls, getClass().getClassLoader());

                            JarFile jarFile = new JarFile(libraryName);
                            Enumeration<JarEntry> entries = jarFile.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (name.endsWith(".class")) {

                                    int index = name.lastIndexOf('.');
                                    String className = name.substring(0, index);
                                    className = className.replace('/', '.').replace('\\', '.');

                                    try {
                                        if (className.contains("Driver") || className.contains("JDBC")) {
                                            Class<?> clazz = classLoader.loadClass(className);
                                            if (Driver.class.isAssignableFrom(clazz)) {
                                                Driver driver = (Driver) clazz.newInstance();
                                                drivers.add(driver);
                                            }
                                        }
                                    } catch (Throwable throwable) {
                                        // ignore
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
        Map<String, String> libMap = INTERNAL_LIB_MAP.get(databaseType);
        if (libMap != null) {
            String javaVersion = SystemProperties.getJavaVersion();
            LOGGER.info(javaVersion);
            for (String version : libMap.keySet()) {
                if (javaVersion.startsWith(version)) {
                    String libFile = libMap.get(version);
                    ClassLoader classLoader = getClass().getClassLoader();
                    URL url = classLoader.getResource("/" + libFile);
                    return url == null ? null : new File(url.toURI()).getPath();
                }
            }
        }
        return null;
    }

    public Driver getDriver(String libraryName, String className) throws Exception {
        if (StringUtil.isEmptyOrSpaces(className)) {
            throw new Exception("No driver class specified.");
        }
        if (new File(libraryName).exists()) {
            List<Driver> drivers = loadDrivers(libraryName);
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
        List<Driver> drivers = m.loadDrivers(file.getPath());
    }
}
