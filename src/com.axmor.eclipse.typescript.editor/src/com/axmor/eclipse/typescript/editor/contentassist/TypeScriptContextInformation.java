/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.contentassist;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

/**
 * @author kudrin
 *
 */
public final class TypeScriptContextInformation implements IContextInformation {
    
    /** The name of the context. */
    private final String fContextDisplayString;
    /** The information to be displayed. */
    private final String fInformationDisplayString;
    /** The position of the context. */
    private final Position fPosition;
    
    /**
     * Creates a new context information.
     *
     * @param contextDisplayString the string to be used when presenting the context
     * @param informationDisplayString the string to be displayed when presenting the context information
     */
    public TypeScriptContextInformation(String contextDisplayString, String informationDisplayString, Position position) {
        fContextDisplayString = contextDisplayString;
        fInformationDisplayString = informationDisplayString;
        fPosition = position;
    }

    @Override
    public String getContextDisplayString() {
        return fContextDisplayString;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public String getInformationDisplayString() {
        return fInformationDisplayString;
    }
    
    public Position getPosition() {
        return fPosition;
    }

}
