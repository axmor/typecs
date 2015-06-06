package com.axmor.eclipse.typescript.debug.console;

import com.axmor.eclipse.typescript.debug.console.TypeScriptConsoleDocumentListener.Callback;

public interface ICommandHandler {

    void handleCommand(String userInput, Callback<Object, String> onContentsReceived);

}
