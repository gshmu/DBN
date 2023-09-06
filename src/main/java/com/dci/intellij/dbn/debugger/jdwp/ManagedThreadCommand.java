package com.dci.intellij.dbn.debugger.jdwp;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

@Getter
public abstract class ManagedThreadCommand extends DebuggerCommandImpl{
    private ManagedThreadCommand(Priority priority) {
        super(priority);
    }

    @Deprecated
    @Compatibility // TODO verify if used in earlier versions, decommission otherwise
    public void execute() {
        try {
            action();
        } catch (Exception e) {
            conditionallyLog(e);
        }
    }

    public static void schedule(DebugProcess debugProcess, Priority priority, Runnable action) {
        ManagedThreadCommand command = create(priority, action);
        ((DebugProcessImpl) debugProcess).getManagerThread().schedule(command);
    }


    public static void invoke(DebugProcess debugProcess, Priority priority, Runnable action) {
        ManagedThreadCommand command = create(priority, action);
        ((DebugProcessImpl) debugProcess).getManagerThread().invoke(command);
    }

    @NotNull
    private static ManagedThreadCommand create(Priority priority, Runnable action) {
        return new ManagedThreadCommand(priority) {
            @Override
            protected void action() throws Exception {
                action.run();
            }
        };
    }
}
