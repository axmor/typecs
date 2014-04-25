/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.debug;

import java.io.IOException;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 * @author Konstantin Zaitcev
 *
 */
public class TypeScriptStreamProxy implements IStreamsProxy {

    @Override
    public IStreamMonitor getErrorStreamMonitor() {
        return new IStreamMonitor() {
            
            @Override
            public void removeListener(IStreamListener listener) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public String getContents() {
                return "asdasd";
            }
            
            @Override
            public void addListener(IStreamListener listener) {
                // TODO Auto-generated method stub
                
            }
        };
    }

    @Override
    public IStreamMonitor getOutputStreamMonitor() {
        return new IStreamMonitor() {
            
            @Override
            public void removeListener(IStreamListener listener) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public String getContents() {
                return "asdasd";
            }
            
            @Override
            public void addListener(IStreamListener listener) {
                // TODO Auto-generated method stub
                
            }
        };
    }

    @Override
    public void write(String input) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
