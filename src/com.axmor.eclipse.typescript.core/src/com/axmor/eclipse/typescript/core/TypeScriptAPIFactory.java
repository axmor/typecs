/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import com.axmor.eclipse.typescript.core.internal.TypeScriptAPIImpl;
import com.axmor.eclipse.typescript.core.internal.TypeScriptAPITestImpl;

/**
 * API Provider.
 * 
 * @author Konstantin Zaitcev
 */
public final class TypeScriptAPIFactory {
    /** For testing purpose only. */
    private static final String ETS_FAKE = "ETS.FAKE"; //$NON-NLS-1$

    /** Singleton instance. */
    private static Map<IProject, TypeScriptAPI> apis = new HashMap<>();

    /**
     * Hide constructor.
     */
    private TypeScriptAPIFactory() {
        // empty block
    }

    /**
     * @param project
     *            project
     * @return appropriate TypeScriptAPI
     */
    public static synchronized TypeScriptAPI getTypeScriptAPI(final IProject project) {
        if (!apis.containsKey(project)) {
            apis.put(project, System.getProperty(ETS_FAKE) != null ? new TypeScriptAPITestImpl()
                    : new TypeScriptAPIImpl(project));
        }
        return apis.get(project);
    }

    /**
     * Stops all API instances.
     */
    public static synchronized void stopTypeScriptAPIs() {
        for (TypeScriptAPI api : apis.values()) {
            api.dispose();
        }
        apis.clear();
    }
}
