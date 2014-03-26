/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.Activator.TypeDocument;

/**
 * @author Konstantin Zaitcev
 */
public class IndexerJobTest {

    /**
     * Tests index initialization.
     */
    @Test
    public void testIndexInit() {
        TypeDocument[] searchResults = Activator.getDefault().getSearchResults("**");
        assertNotNull(searchResults);
        assertEquals(0, searchResults.length);
    }
}
