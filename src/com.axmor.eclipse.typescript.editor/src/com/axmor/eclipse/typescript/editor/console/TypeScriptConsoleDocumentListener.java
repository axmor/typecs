package com.axmor.eclipse.typescript.editor.console;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.TextUtilities;

import com.axmor.eclipse.typescript.editor.Activator;

public class TypeScriptConsoleDocumentListener implements IDocumentListener {
    
    /**
     * Document to which this listener is attached.
     */
    private IDocument doc;
    
    private int disconnectionLevel = 0;
    
    private TypeScriptConsoleViewer viewer;
    
    private ICommandHandler handler;
    
    private static final Pattern compiled = Pattern.compile("\\r?\\n|\\r");    

    public TypeScriptConsoleDocumentListener(TypeScriptConsoleViewer typeScriptConsoleViewer, ICommandHandler console) {
        this.viewer = typeScriptConsoleViewer;
        this.handler = console;
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent arg0) {

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
        String delim = getDelimeter();
        try {
            doc.replace(offset, text.length(), "");
            text = text + doc.get(offset, doc.getLength() - offset);
            doc.replace(offset, doc.getLength() - offset, "");       
           
        } catch (BadLocationException e) {
            Activator.error(e);
        }
        int start = 0;
        int index = -1;
        List<String> commands = new ArrayList<String>();
        while ((index = text.indexOf(delim, start)) != -1) {
            String cmd = text.substring(start, index);
            commands.add(cmd);
            start = index + delim.length();
        }
        if (commands.size() > 0) {
            //Note that we'll disconnect from the document here and reconnect when the last line is executed.
            startDisconnected();
            String cmd = commands.get(0);
            execCommand(delim, cmd, commands, text, start);
        } else {            
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
    
    private void execCommand(final String delim, final String cmd, final List<String> commands, final String text, final int start) {
        int lastLine = doc.getNumberOfLines() - 1;
        try {
            int commandLineOffset = doc.getLineOffset(lastLine);
            int commandLineLength = doc.getLineLength(lastLine);
            final String commandLine = doc.get(commandLineOffset, commandLineLength);
            appendText(getDelimeter());
          //Handle the command in a thread that doesn't block the U/I.
            Job j = new Job("TypeScript Console Hander") {           

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    handler.handleCommand(commandLine);
                    return Status.OK_STATUS;
                };
            };
            j.setSystem(true);
            j.schedule();
        } catch (BadLocationException e) {
            Activator.error(e);
        }         

    }  

}
