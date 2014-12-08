package com.axmor.eclipse.typescript.editor.console;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.console.TextConsole;

import com.axmor.eclipse.typescript.editor.Activator;

public class TypeScriptConsoleDocumentListener implements IDocumentListener {
    
    /**
     * Document to which this listener is attached.
     */
    private IDocument doc;
    
    private int disconnectionLevel = 0;
    
    private TypeScriptConsoleViewer viewer;
    
    private static final Pattern compiled = Pattern.compile("\\r?\\n|\\r");    

    public TypeScriptConsoleDocumentListener(TypeScriptConsoleViewer typeScriptConsoleViewer, TextConsole console) {
        this.viewer = typeScriptConsoleViewer;
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void documentChanged(DocumentEvent event) {
        startDisconnected();
        try {
            int eventOffset = event.getOffset();
            String eventText = event.getText();
            proccessAddition(eventOffset, eventText);            
        } finally {
            stopDisconnected();
        }      
    }
    
    private void proccessAddition(int offset, String text) {
        //we have to do some gymnastics here to add line-by-line the contents that the user entered.
        //(mostly because it may have been a copy/paste with multi-lines)

        String indentString = "";
        boolean addedNewLine = false;
        boolean addedParen = false;
        boolean addedCloseParen = false;
        int addedLen = text.length();
        if (addedLen == 1) {
            if (text.equals("\r") || text.equals("\n")) {
                addedNewLine = true;

            } else if (text.equals("(")) {
                addedParen = true;

            } else if (text.equals(")")) {
                addedCloseParen = true;
            }

        } else if (addedLen == 2) {
            if (text.equals("\r\n")) {
                addedNewLine = true;
            }
        }

        String delim = getDelimeter();

        int newDeltaCaretPosition = doc.getLength() - (offset + text.length());

        try {
            doc.replace(offset, text.length(), ""); //$NON-NLS-1$
            text = text + doc.get(offset, doc.getLength() - offset);
            doc.replace(offset, doc.getLength() - offset, "");
            
            /*if (!offset_in_command_line) {
                offset = newDeltaCaretPosition = getCommandLineOffset();
                // Remove any existing command line text and prepend it to the text
                // we're inserting
                text = doc.get(getCommandLineOffset(), getCommandLineLength()) + text;
                doc.replace(getCommandLineOffset(), getCommandLineLength(), "");
            } else {
                // paste is within the command line
                text = text + doc.get(offset, doc.getLength() - offset);
                doc.replace(offset, doc.getLength() - offset, "");
            }*/
        } catch (BadLocationException e) {
            text = "";
            Activator.error(e);
        }

        //text = replaceNewLines(text, delim);

        //now, add it line-by-line (it won't even get into the loop if there's no
        //new line in the text added).
        int start = 0;
        int index = -1;
        List<String> commands = new ArrayList<String>();
        while ((index = text.indexOf(delim, start)) != -1) {
            String cmd = text.substring(start, index);
            commands.add(cmd);
            start = index + delim.length();
        }

        final String[] finalIndentString = new String[] { indentString };

        if (commands.size() > 0) {
            //Note that we'll disconnect from the document here and reconnect when the last line is executed.
            startDisconnected();
            String cmd = commands.get(0);
            execCommand(addedNewLine, delim, finalIndentString, cmd, commands, 0, text, addedParen, start,
                    addedCloseParen, newDeltaCaretPosition);
        } else {
            //onAfterAllLinesHandled(text, addedParen, start, offset, addedCloseParen, finalIndentString[0],
            //        newDeltaCaretPosition);
            appendText(text);
        }

    }
    
    /**
     * Appends some text at the end of the document.
     *
     * @param text the text to be added.
     */
    protected void appendText(String text) {
        int initialOffset = doc.getLength();
        try {
            doc.replace(initialOffset, 0, text);
            viewer.setCaretOffset(doc.getLength(), false);            
        } catch (BadLocationException e) {
            Activator.error(e);
        }
    }
    
    /**
     * Stop listening to changes (so that we're able to change the document in this class without having
     * any loops back into the function that will change it)
     */
    protected synchronized void startDisconnected() {
        if (disconnectionLevel == 0) {
            doc.removeDocumentListener(this);
        }
        disconnectionLevel += 1;
    }

    /**
     * Start listening to changes again.
     */
    protected synchronized void stopDisconnected() {
        disconnectionLevel -= 1;

        if (disconnectionLevel == 0) {
            doc.addDocumentListener(this);
        }
    }

    public void setDocument(IDocument document) {
        reconnect(this.doc, document);        
    }
    
    /**
     * Stops listening changes in one document and starts listening another one.
     *
     * @param oldDoc may be null (if not null, this class will stop listening changes in it).
     * @param newDoc the document that should be listened from now on.
     */
    private synchronized void reconnect(IDocument oldDoc, IDocument newDoc) {
        Assert.isTrue(disconnectionLevel == 0);

        if (oldDoc != null) {
            oldDoc.removeDocumentListener(this);
        }

        newDoc.addDocumentListener(this);
        this.doc = newDoc;

    }
    
    /**
     * @return the delimiter to be used to add new lines to the console.
     */
    public String getDelimeter() {
        return TextUtilities.getDefaultLineDelimiter(doc);
    }
    
    /**
     * Here is where we run things not using the UI thread. It's a recursive function. In summary, it'll
     * run each line in the commands received in a new thread, and as each finishes, it calls itself again
     * for the next command. The last command will reconnect to the document.
     *
     * Exceptions had to be locally handled, because they're not well tolerated under this scenario
     * (if on of the callbacks fail, the others won't be executed and we'd get into a situation
     * where the shell becomes unusable).
     */
    private void execCommand(final boolean addedNewLine, final String delim, final String[] finalIndentString,
            final String cmd, final List<String> commands, final int currentCommand, final String text,
            final boolean addedParen, final int start, final boolean addedCloseParen, final int newDeltaCaretPosition) {        
        appendText(cmd);
        
    }
    
    public static String replaceNewLines(String text, String repl) {
        return compiled.matcher(text).replaceAll(repl);
    }

}
