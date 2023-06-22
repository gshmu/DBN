package com.dci.intellij.dbn.driver;

import com.intellij.openapi.Disposable;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Driver;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

@Slf4j
@Getter
public class DriverBundle implements Disposable {
    private final DriverClassLoader classLoader;
    private final Map<Class<Driver>, Driver> instances = new ConcurrentHashMap<>();

    public DriverBundle(File library, String location) {
        this.classLoader = new DriverClassLoaderJarImpl(library, location);
    }

    public DriverBundle(File library) {
        this.classLoader = new DriverClassLoaderImpl(library, getClass().getClassLoader());
    }

    @Override
    public void dispose() {
        try {
            classLoader.close();
        } catch (Throwable e) {
            conditionallyLog(e);
            log.warn("Failed to dispose class loader", e);
        }
    }

    @Nullable
    public Driver getDriver(String className) {
        for (Class<Driver> driver : getDrivers()) {
            if (Objects.equals(driver.getName(), className)) {
                return instances.computeIfAbsent(driver, d -> createDriver(d));
                // cached driver instances seem to work better (at least for oracle)
                //return createDriver(driver);
            }
        }
        return null;
    }

    @Nullable
    public Driver createDriver(String className) {
        for (Class<Driver> driver : getDrivers()) {
            if (Objects.equals(driver.getName(), className)) {
                return createDriver(driver);
            }
        }
        return null;
    }

    @NotNull
    @SneakyThrows
    private static Driver createDriver(Class<Driver> driverClass){
        return driverClass.getDeclaredConstructor().newInstance();
    }

    public File getLibrary() {
        return classLoader.getLibrary();
    }

    public List<Class<Driver>> getDrivers() {
        return classLoader.getDrivers();
    }

    public List<File> getJars() {
        return classLoader.getJars();
    }

    public boolean isEmpty() {
        return getDrivers().isEmpty();
    }
}
