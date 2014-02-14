/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * Represents a TypeScript text hyperlink.
 * 
 * @author Asya Vorobyova
 *
 */
public class TypeScriptElementHyperlink implements IHyperlink {
    
    /**
     * A TypeScript editor
     */
    private TypeScriptEditor editor;
    
    /**
     * A region corresponding to an element
     */
    private IRegion region;
    
    /**
     * @param editor an editor
     * @param region a region
     */
    public TypeScriptElementHyperlink(TypeScriptEditor editor, IRegion region) {
        super();
        this.editor = editor;
        this.region = region;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return region;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return null;
    }

    @Override
    public void open() {
        TypeScriptEditorUtils.openDeclaration(editor);
    }

}
