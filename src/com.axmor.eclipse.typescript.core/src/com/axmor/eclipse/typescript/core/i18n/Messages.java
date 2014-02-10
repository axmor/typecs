/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author Konstantin Zaitcev
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.axmor.eclipse.typescript.core.i18n.messages"; //$NON-NLS-1$
    public static String TypeScriptBridge_NodeJSStopError;
    public static String TypescriptWorkbenchPreferencePage_compiler_version;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
