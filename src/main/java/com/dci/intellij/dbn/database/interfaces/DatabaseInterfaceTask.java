package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.common.Priority;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DatabaseInterfaceTask implements Runnable{
    private final Priority priority;

    @Override
    public void run() {

    }
}
