/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/ 
package com.axmor.eclipse.typescript.editor.parser;

/**
 * Class to get kinds of model objects
 *  
 * @author Asya Vorobyova
 *
 */
public class TypeScriptModelKinds {
    
    /**
     * Model kinds
     */
    public enum Kinds {
        PRIMITIVE_TYPE,
        KEYWORD,
        CLASS, 
        INTERFACE,
        MODULE,
        PROPERTY,
        METHOD,
        CONSTRUCTOR,
        FUNCTION,
        VAR,
        ENUM,
        PRIVATE,
        PUBLIC,
        STATIC;
        
        @Override
        public String toString() {
            if (name().equals("PRIMITIVE_TYPE")) {
                return "primitive type";
            }
            String lowercase = name().toLowerCase(java.util.Locale.US); 
            return lowercase;
        }
    };

}
