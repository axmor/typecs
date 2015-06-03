package com.axmor.eclipse.typescript.debug.console;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.TextConsoleViewer;

public class TypeScriptConsoleViewer extends TextConsoleViewer {

    /**
     * Listens and acts to document changes (and passes them to the shell)
     */
    private TypeScriptConsoleDocumentListener listener;

    public TypeScriptConsoleViewer(Composite parent, TypescriptConsole console) {
        super(parent, console);
        this.listener = new TypeScriptConsoleDocumentListener(this, console);
        this.listener.setDocument(getDocument());
        this.listener.appendInvitation(false);
        final StyledText styledText = getTextWidget();
        styledText.addFocusListener(new FocusListener() {

            /**
             * When the initial focus is gained, set the caret position to the last position (just
             * after the prompt)
             */
            public void focusGained(FocusEvent e) {
                setCaretOffset(getDocument().getLength(), true);
                // just a 1-time listener
                styledText.removeFocusListener(this);
            }

            public void focusLost(FocusEvent e) {

            }
        });
    }

    /**
     * Sets the new caret position in the console. *
     * 
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

    /**
     * Used to clear the contents of the document
     */
    public void clear(boolean addInitialCommands) {
        listener.clear(addInitialCommands);
    }

}
