package com.axmor.eclipse.typescript.editor.console;

import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;

public class TypeScriptConsolePage extends TextConsolePage {
    
    private TypeScriptConsoleViewer viewer;

    public TypeScriptConsolePage(TextConsole console, IConsoleView view) {
        super(console, view);
    }
    
    @Override
    protected TextConsoleViewer createViewer(Composite parent) {
        TypescriptConsole console = (TypescriptConsole) getConsole();
        viewer = new TypeScriptConsoleViewer(parent, console);
        viewer.configure(new SourceViewerConfiguration());
        return viewer;
    } 

}
