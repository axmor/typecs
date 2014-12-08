package com.axmor.eclipse.typescript.editor.console;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsoleViewer;

public class TypeScriptConsoleViewer extends TextConsoleViewer {
    
    /**
     * Listens and acts to document changes (and passes them to the shell)
     */
    private TypeScriptConsoleDocumentListener listener;

    public TypeScriptConsoleViewer(Composite parent, TextConsole console) {
        super(parent, console);
        this.listener = new TypeScriptConsoleDocumentListener(this, console);

        this.listener.setDocument(getDocument());
        
        
    }
    
    /**
     * Sets the new caret position in the console.
     *
     * TODO: async should not be allowed (only clearing the shell at the constructor still uses that)
     */
    public void setCaretOffset(final int offset, boolean async) {
        final StyledText textWidget = getTextWidget();
        if (textWidget != null) {
            if (async) {
                Display display = textWidget.getDisplay();
                if (display != null) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            textWidget.setCaretOffset(offset);
                        }
                    });
                }
            } else {
                textWidget.setCaretOffset(offset);
            }
        }
    }
    
    /**
     * Creates the styled text for the console
     */
    @Override
    protected StyledText createTextWidget(Composite parent, int styles) {
        return new StyledText(parent, styles);
    }

}
