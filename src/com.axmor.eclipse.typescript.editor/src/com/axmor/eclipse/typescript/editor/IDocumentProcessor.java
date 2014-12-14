/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

/**
 * The document processor for the TypeScript Editor.
 * 
 * @author Konstantin Zaitcev
 */
public interface IDocumentProcessor {
    
    /**
     * Invokes on document changes.
     * 
     * @param file TypeScript file corresponding to a given document 
     * @param doc the document which is changed
     */
    void processDocument(IFile file, IDocument doc);
}
