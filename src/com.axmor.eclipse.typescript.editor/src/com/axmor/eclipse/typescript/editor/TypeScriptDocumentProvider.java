package com.axmor.eclipse.typescript.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import com.axmor.eclipse.typescript.editor.parser.TypeScriptPartitionScanner;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptDocumentProvider extends FileDocumentProvider {
	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner = new FastPartitioner(new TypeScriptPartitionScanner(),
					TypeScriptPartitionScanner.TS_PARTITION_TYPES);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
	
	public IDocument addDocument(Object element) throws CoreException {
	    return this.createDocument(element);
	}
}
