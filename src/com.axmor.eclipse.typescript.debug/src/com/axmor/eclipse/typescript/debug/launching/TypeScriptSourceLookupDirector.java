/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.launching;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * @author Konstantin Zaitcev
 */
public class TypeScriptSourceLookupDirector extends AbstractSourceLookupDirector {

    @Override
    public void initializeParticipants() {
        addParticipants(new ISourceLookupParticipant[] { new TypeScriptSourceLookupParticipant() });
    }
}
