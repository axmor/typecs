package com.axmor.eclipse.typescript.editor.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

public class TypeScriptConsoleManager {
    
    private static TypeScriptConsoleManager instance;

    /**
     * @return the singleton for the script console manager.
     */
    public static synchronized TypeScriptConsoleManager getInstance() {
        if (instance == null) {
            instance = new TypeScriptConsoleManager();
        }

        return instance;
    }

    /**
     * Reference to the console manager singleton from eclipse.
     */
    private IConsoleManager manager;

    protected TypeScriptConsoleManager() {
        this.manager = ConsolePlugin.getDefault().getConsoleManager();
    }

    /**
     * Terminates the execution of the given console and removes it from the list of available consoles.
     * @param console the console to be terminated and removed.
     */
    public void close(TypescriptConsole console) {
        console.terminate();
        manager.removeConsoles(new IConsole[] { console });
    }

    /**
     * Closes all the script consoles available.
     */
    public void closeAll() {
        IConsole[] consoles = manager.getConsoles();
        for (int i = 0; i < consoles.length; ++i) {
            IConsole console = consoles[i];
            if (console instanceof TypescriptConsole) {
                close((TypescriptConsole) console);
            }
        }
    }

    /**
     * Adds a given console to the console view.
     * 
     * @param console the console to be added to the console view
     * @param show whether it should be shown or not.
     */
    public void add(TypescriptConsole console, boolean show) {
        manager.addConsoles(new IConsole[] { console });
        if (show) {
            manager.showConsoleView(console);
        }
    }

    /**
     * Enable/Disable linking of the debug console with the suspended frame.
     */
    public void linkWithDebugSelection(TypescriptConsole console, boolean isChecked) {
        console.linkWithDebugSelection(isChecked);
    }

}
