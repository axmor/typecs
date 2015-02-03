/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.test;

import org.junit.Assert;
import org.junit.Test;

import com.axmor.eclipse.typescript.core.Activator;

/**
 * @author Konstantin Zaitcev
 */
public class IndexerJobTest extends Assert {

    /**
     * Tests index initialization.
     */
    @Test
    public void testIndexInit() {
		assertFalse(Activator.getDefault().getSearchResults("**").iterator().hasNext());
    }
}
