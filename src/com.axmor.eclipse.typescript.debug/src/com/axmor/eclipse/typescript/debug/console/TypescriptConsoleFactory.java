package com.axmor.eclipse.typescript.debug.console;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.ui.contexts.DebugContextManager;
import org.eclipse.ui.console.IConsoleFactory;

import com.axmor.eclipse.typescript.debug.Activator;
import com.axmor.eclipse.typescript.debug.model.TypeScriptDebugTarget;

@SuppressWarnings("restriction")
public class TypescriptConsoleFactory implements IConsoleFactory {

    @Override
    public void openConsole() {
        createConsole(null);
    }

    /**
     * Create a new Typescript console
     */
    public void createConsole(String additionalInitialComands) {
        try {
            ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
            for (ILaunch launch : launches) {
                if (launch.getDebugTarget() instanceof TypeScriptDebugTarget) {
                    TypeScriptDebugTarget dt = (TypeScriptDebugTarget) launch.getDebugTarget();
                    if (dt.getStackFrames().length > 0) {
                        TypescriptConsoleInterpreter interpreter = createDefaultTypescriptInterpreter(dt);
                        if (interpreter == null) {
                            return;
                        }
                        interpreter.setFrame(dt.getStackFrames()[0]);
                        createConsole(interpreter, additionalInitialComands);
                    }
                }
            }
        } catch (Exception e) {
            Activator.error(e);
        }
    }

    private TypescriptConsoleInterpreter createDefaultTypescriptInterpreter(TypeScriptDebugTarget debugTarget) {
        TypescriptConsoleInterpreter consoleInterpreter = new TypescriptConsoleInterpreter();
        consoleInterpreter.setTarget(debugTarget);
        DebugContextManager.getDefault().addDebugContextListener(consoleInterpreter);
        return consoleInterpreter;
    }

    private void createConsole(final TypescriptConsoleInterpreter interpreter, final String additionalInitialComands) {
        Job job = new Job("Create Interactive Console") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Create Interactive Console", 2);
                try {
                    connectDebugger(interpreter, additionalInitialComands, new SubProgressMonitor(monitor, 2));
                    return Status.OK_STATUS;
                } catch (Exception e) {
                    try {
                        interpreter.close();
                    } catch (Exception e_inner) {
                        Activator.error(e_inner);
                    }
                    return Status.CANCEL_STATUS;

                } finally {
                    monitor.done();
                }

            }
        };
        job.setUser(true);
        job.schedule();
    }

    private void connectDebugger(TypescriptConsoleInterpreter interpreter, String additionalInitialComands,
            SubProgressMonitor monitor) {
        monitor.beginTask("Connect Debugger", 10);
        try {
            if (interpreter.getTarget() != null) {
                monitor.worked(1);
                TypescriptConsole console = new TypescriptConsole(interpreter, additionalInitialComands);
                monitor.worked(1);
                TypeScriptConsoleManager manager = TypeScriptConsoleManager.getInstance();
                manager.add(console, true);
            }
        } finally {
            monitor.done();
        }
    }

}
