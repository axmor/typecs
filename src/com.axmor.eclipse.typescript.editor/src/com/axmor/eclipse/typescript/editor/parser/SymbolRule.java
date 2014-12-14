/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.parser;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * Rule to determine a specific symbol
 * 
 * @author Asya Vorobyova
 * 
 */
public class SymbolRule implements IRule {

    /**
     * Symbol to be determined
     */
    private char fSymbol;

    /**
     * The token to be returned on success
     */
    private IToken fToken;

    /**
     * A constructor
     * 
     * @param symbol
     *            to be determined
     * @param token
     *            to be returned on success
     */
    public SymbolRule(char symbol, IToken token) {
        Assert.isNotNull(token);

        fSymbol = symbol;
        fToken = token;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        int c = scanner.read();
        if ((char) c == fSymbol) {
            return fToken;
        }
        scanner.unread();
        return Token.UNDEFINED;
    }

}
