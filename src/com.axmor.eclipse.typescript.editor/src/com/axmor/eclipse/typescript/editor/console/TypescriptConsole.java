package com.axmor.eclipse.typescript.editor.console;

import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;

public class TypescriptConsole extends TextConsole  implements ICommandHandler {
    
    public static final String CONSOLE_NAME = "TypeScript Console";
    
    public static final String DEFAULT_CONSOLE_TYPE = "com.axmor.eclipse.typescript.editor.console.TypescriptConsole";
    
    public static int nextId = -1;
    
    private TypescriptConsoleInterpreter interpreter;
    
    private TypeScriptConsolePage page;
    
    protected ScriptConsolePartitioner partitioner;

    public TypescriptConsole(TypescriptConsoleInterpreter interpreter, String additionalInitialComands) {
        super(CONSOLE_NAME + " [" + getNextId() + "]", DEFAULT_CONSOLE_TYPE, null, true);
        this.interpreter = interpreter;
        
        partitioner = new ScriptConsolePartitioner();
        getDocument().setDocumentPartitioner(partitioner);
        partitioner.connect(getDocument());
    }

    @Override
    protected IConsoleDocumentPartitioner getPartitioner() {
        return null;
    }
    
    private static String getNextId() {
        nextId += 1;
        return String.valueOf(nextId);
    }

    public void terminate() {        
    }

    public void linkWithDebugSelection(boolean isChecked) {        
    }
    
    /**
     * Creates the actual page to be shown to the user.
     */
    @Override
    public IPageBookViewPage createPage(IConsoleView view) {
        page = new TypeScriptConsolePage(this, view);
        return page;
    }
    
    /**
     * Clears the console
     */
    @Override
    public void clearConsole() {
        //page.clearConsolePage();
    }
    
    /**
     * Handles some command that the user entered
     *
     * @param userInput that's the command to be evaluated by the user.
     */
    public void handleCommand(String userInput) {        

        //executes the user input in the interpreter
        if (interpreter != null) {
            interpreter.exec(userInput);
        }

    }

}
