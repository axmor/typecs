/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.builder.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * @author Konstantin Zaitcev
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.axmor.eclipse.typescript.builder.i18n.messages"; //$NON-NLS-1$
    public static String ProjectPrefPage_browseButton;
    public static String ProjectPrefPage_compileSection;
    public static String ProjectPrefPage_declaration;
    public static String ProjectPrefPage_generationSection;
    public static String ProjectPrefPage_mapRootLabel;
    public static String ProjectPrefPage_moduleLabel;
    public static String ProjectPrefPage_noImlicitAny;
    public static String ProjectPrefPage_noResolve;
    public static String ProjectPrefPage_removeComments;
    public static String ProjectPrefPage_rootSelectionDesc;
    public static String ProjectPrefPage_rootSelectionTitle;
    public static String ProjectPrefPage_sourceLabel;
    public static String ProjectPrefPage_sourceMap;
    public static String ProjectPrefPage_sourceSelectionDesc;
    public static String ProjectPrefPage_sourceSelectionTitle;
    public static String ProjectPrefPage_targetLabel;
    public static String ProjectPrefPage_targetRelativePathBasedOnSource;
    public static String ProjectPrefPage_targetSelectionDesc;
    public static String ProjectPrefPage_targetSelectionTitle;
    public static String ProjectPrefPage_targetVersion;
    public static String ProjectPrefPage_title;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
