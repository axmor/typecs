package com.axmor.eclipse.typescript.editor.console;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.IConsoleFactory;

import com.axmor.eclipse.typescript.editor.Activator;

public class TypescriptConsoleFactory implements IConsoleFactory  {

	@Override
	public void openConsole() {
		createConsole(null);		
	}
	
	/**
     * Create a new Typescript console
     */
    public void createConsole(String additionalInitialComands) {
        try {
            TypescriptConsoleInterpreter interpreter = createDefaultTypescriptInterpreter();
            if (interpreter == null) {
                return;
            }
            createConsole(interpreter, additionalInitialComands);            
        } catch (Exception e) {
        	Activator.error(e);
        }
    }

	private TypescriptConsoleInterpreter createDefaultTypescriptInterpreter() {
		TypescriptConsoleInterpreter consoleInterpreter = new TypescriptConsoleInterpreter();
		return consoleInterpreter;
	}

	private void createConsole(final TypescriptConsoleInterpreter interpreter, final String additionalInitialComands) {
		Job job = new Job("Create Interactive Console") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Create Interactive Console", 4);
                try {
                    connectDebugger(interpreter, additionalInitialComands, new SubProgressMonitor(monitor, 2));
                    enableGuiEvents(interpreter, new SubProgressMonitor(monitor, 1));
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

	private void enableGuiEvents(TypescriptConsoleInterpreter interpreter, SubProgressMonitor subProgressMonitor) {        
    }

	private void connectDebugger(TypescriptConsoleInterpreter interpreter, String additionalInitialComands,
            SubProgressMonitor monitor) {
	    monitor.beginTask("Connect Debugger", 10);
        try {
            if (interpreter.getFrame() == null) {
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
