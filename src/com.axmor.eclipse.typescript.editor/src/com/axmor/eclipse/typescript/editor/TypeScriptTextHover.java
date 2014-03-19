/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;

/**
 * @author Asya Vorobyova
 *
 */
public class TypeScriptTextHover implements ITextHover, ITextHoverExtension2 {

    @Override
    public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
        // TODO Auto-generated method stub
        return null;
    }

}
