/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * TypeScript editor settings.
 * 
 * @author Konstantin Zaitcev
 */
public final class TypeScriptEditorSettings {

    // / general settings
    /** IndentSize. */
    private int indentSize = 4;
    /** TabSize. */
    private int tabSize = 4;
    /** NewLineCharacter. */
    private String newLineCharacter = "\r\n";
    /** ConvertTabsToSpaces. */
    private boolean convertTabsToSpaces = true;

    // / formatting settings
    /** InsertSpaceAfterCommaDelimiter. */
    private boolean insertSpaceAfterCommaDelimiter = true;
    /** InsertSpaceAfterSemicolonInForStatements. */
    private boolean insertSpaceAfterSemicolon = true;
    /** InsertSpaceBeforeAndAfterBinaryOperators. */
    private boolean insertSpaceBinaryOperators = true;
    /** InsertSpaceAfterKeywordsInControlFlowStatements. */
    private boolean insertSpaceAfterKeywords = true;
    /** InsertSpaceAfterFunctionKeywordForAnonymousFunctions. */
    private boolean insertSpaceAfterFunction = false;
    /** InsertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis. */
    private boolean insertSpaceAfterNonemptyParenthesis = false;
    /** PlaceOpenBraceOnNewLineForFunctions. */
    private boolean placeOpenBraceFunctions = false;
    /** PlaceOpenBraceOnNewLineForControlBlocks. */
    private boolean placeOpenBraceControlBlocks = false;
    /** InsertCloseBrackets. */
    private boolean insertCloseBrackets = true;

    /**
     * @return the indentSize
     */
    public int getIndentSize() {
        return indentSize;
    }

    /**
     * @param indentSize the indentSize to set
     */
    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    /**
     * @return the tabSize
     */
    public int getTabSize() {
        return tabSize;
    }

    /**
     * @param tabSize the tabSize to set
     */
    public void setTabSize(int tabSize) {
        this.tabSize = tabSize;
    }

    /**
     * @return the newLineCharacter
     */
    public String getNewLineCharacter() {
        return newLineCharacter;
    }

    /**
     * @param newLineCharacter the newLineCharacter to set
     */
    public void setNewLineCharacter(String newLineCharacter) {
        this.newLineCharacter = newLineCharacter;
    }

    /**
     * @return the convertTabsToSpaces
     */
    public boolean isConvertTabsToSpaces() {
        return convertTabsToSpaces;
    }

    /**
     * @param convertTabsToSpaces the convertTabsToSpaces to set
     */
    public void setConvertTabsToSpaces(boolean convertTabsToSpaces) {
        this.convertTabsToSpaces = convertTabsToSpaces;
    }

    /**
     * @return the insertSpaceAfterCommaDelimiter
     */
    public boolean isInsertSpaceAfterCommaDelimiter() {
        return insertSpaceAfterCommaDelimiter;
    }

    /**
     * @param insertSpaceAfterCommaDelimiter the insertSpaceAfterCommaDelimiter to set
     */
    public void setInsertSpaceAfterCommaDelimiter(boolean insertSpaceAfterCommaDelimiter) {
        this.insertSpaceAfterCommaDelimiter = insertSpaceAfterCommaDelimiter;
    }

    /**
     * @return the insertSpaceAfterSemicolon
     */
    public boolean isInsertSpaceAfterSemicolon() {
        return insertSpaceAfterSemicolon;
    }

    /**
     * @param insertSpaceAfterSemicolon the insertSpaceAfterSemicolon to set
     */
    public void setInsertSpaceAfterSemicolon(boolean insertSpaceAfterSemicolon) {
        this.insertSpaceAfterSemicolon = insertSpaceAfterSemicolon;
    }

    /**
     * @return the insertSpaceBinaryOperators
     */
    public boolean isInsertSpaceBinaryOperators() {
        return insertSpaceBinaryOperators;
    }

    /**
     * @param insertSpaceBinaryOperators the insertSpaceBinaryOperators to set
     */
    public void setInsertSpaceBinaryOperators(boolean insertSpaceBinaryOperators) {
        this.insertSpaceBinaryOperators = insertSpaceBinaryOperators;
    }

    /**
     * @return the insertSpaceAfterKeywords
     */
    public boolean isInsertSpaceAfterKeywords() {
        return insertSpaceAfterKeywords;
    }

    /**
     * @param insertSpaceAfterKeywords the insertSpaceAfterKeywords to set
     */
    public void setInsertSpaceAfterKeywords(boolean insertSpaceAfterKeywords) {
        this.insertSpaceAfterKeywords = insertSpaceAfterKeywords;
    }

    /**
     * @return the insertSpaceAfterFunction
     */
    public boolean isInsertSpaceAfterFunction() {
        return insertSpaceAfterFunction;
    }

    /**
     * @param insertSpaceAfterFunction the insertSpaceAfterFunction to set
     */
    public void setInsertSpaceAfterFunction(boolean insertSpaceAfterFunction) {
        this.insertSpaceAfterFunction = insertSpaceAfterFunction;
    }

    /**
     * @return the insertSpaceAfterNonemptyParenthesis
     */
    public boolean isInsertSpaceAfterNonemptyParenthesis() {
        return insertSpaceAfterNonemptyParenthesis;
    }

    /**
     * @param insertSpaceAfterNonemptyParenthesis the insertSpaceAfterNonemptyParenthesis to set
     */
    public void setInsertSpaceAfterNonemptyParenthesis(boolean insertSpaceAfterNonemptyParenthesis) {
        this.insertSpaceAfterNonemptyParenthesis = insertSpaceAfterNonemptyParenthesis;
    }

    /**
     * @return the placeOpenBraceFunctions
     */
    public boolean isPlaceOpenBraceFunctions() {
        return placeOpenBraceFunctions;
    }

    /**
     * @param placeOpenBraceFunctions the placeOpenBraceFunctions to set
     */
    public void setPlaceOpenBraceFunctions(boolean placeOpenBraceFunctions) {
        this.placeOpenBraceFunctions = placeOpenBraceFunctions;
    }

    /**
     * @return the placeOpenBraceControlBlocks
     */
    public boolean isPlaceOpenBraceControlBlocks() {
        return placeOpenBraceControlBlocks;
    }

    /**
     * @param placeOpenBraceControlBlocks the placeOpenBraceControlBlocks to set
     */
    public void setPlaceOpenBraceControlBlocks(boolean placeOpenBraceControlBlocks) {
        this.placeOpenBraceControlBlocks = placeOpenBraceControlBlocks;
    }
    
    /**
     * @return the insertCloseBrackets
     */
    public boolean isInsertCloseBrackets() {
        return insertCloseBrackets;
    }

    /**
     * @param insertCloseBrackets the insertCloseBrackets to set
     */
    public void setInsertCloseBrackets(boolean insertCloseBrackets) {
        this.insertCloseBrackets = insertCloseBrackets;
    }

    /**
     * Loads settings from preferences.
     * 
     * @return settings
     */
    public static TypeScriptEditorSettings load() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        TypeScriptEditorSettings settings = new TypeScriptEditorSettings();
        if (store.contains("indentSize")) {
            settings.setIndentSize(store.getInt("indentSize"));
        }
        if (store.contains("tabSize")) {
            settings.setTabSize(store.getInt("tabSize"));
        }
        if (store.contains("newLineChar")) {
            settings.setNewLineCharacter(store.getString("newLineChar"));
        }
        if (store.contains("convertTabs")) {
            settings.setConvertTabsToSpaces(store.getBoolean("convertTabs"));
        }
        if (store.contains("insertSpaceComma")) {
            settings.setInsertSpaceAfterCommaDelimiter(store.getBoolean("insertSpaceComma"));
        }
        if (store.contains("insertSpaceSemicolon")) {
            settings.setInsertSpaceAfterSemicolon(store.getBoolean("insertSpaceSemicolon"));
        }
        if (store.contains("insertSpaceBinary")) {
            settings.setInsertSpaceBinaryOperators(store.getBoolean("insertSpaceBinary"));
        }
        if (store.contains("insertSpaceKeywords")) {
            settings.setInsertSpaceAfterKeywords(store.getBoolean("insertSpaceKeywords"));
        }
        if (store.contains("insertSpaceFunction")) {
            settings.setInsertSpaceAfterFunction(store.getBoolean("insertSpaceFunction"));
        }
        if (store.contains("insertSpaceParenthesis")) {
            settings.setInsertSpaceAfterNonemptyParenthesis(store.getBoolean("insertSpaceParenthesis"));
        }
        if (store.contains("placeBraceFunctions")) {
            settings.setPlaceOpenBraceFunctions(store.getBoolean("placeBraceFunctions"));
        }
        if (store.contains("placeBraceBlocks")) {
            settings.setPlaceOpenBraceControlBlocks(store.getBoolean("placeBraceBlocks"));
        }
        if (store.contains("insertCloseBrackets")) {
            settings.setInsertCloseBrackets(store.getBoolean("insertCloseBrackets"));
        }        
        return settings;
    }
    
    /**
     * Saves current settings to eclipse preferences store. 
     */
    public void save() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setValue("indentSize", getIndentSize());
        store.setValue("tabSize", getTabSize());
        store.setValue("newLineChar", getNewLineCharacter());
        store.setValue("convertTabs", isConvertTabsToSpaces());
        store.setValue("insertSpaceComma", isInsertSpaceAfterCommaDelimiter());
        store.setValue("insertSpaceSemicolon", isInsertSpaceAfterSemicolon());
        store.setValue("insertSpaceBinary", isInsertSpaceBinaryOperators());
        store.setValue("insertSpaceKeywords", isInsertSpaceAfterKeywords());
        store.setValue("insertSpaceFunction", isInsertSpaceAfterFunction());
        store.setValue("insertSpaceParenthesis", isInsertSpaceAfterNonemptyParenthesis());
        store.setValue("placeBraceFunctions", isPlaceOpenBraceFunctions());
        store.setValue("placeBraceBlocks", isPlaceOpenBraceControlBlocks());
        store.setValue("insertCloseBrackets", isInsertCloseBrackets());
    }    

    /**
     * Set to default values
     */
    public static void setToDefault() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setToDefault("indentSize");
        store.setToDefault("tabSize");
        store.setToDefault("newLineChar");
        store.setToDefault("convertTabs");
        store.setToDefault("insertSpaceComma");
        store.setToDefault("insertSpaceSemicolon");
        store.setToDefault("insertSpaceBinary");
        store.setToDefault("insertSpaceKeywords");
        store.setToDefault("insertSpaceFunction");
        store.setToDefault("insertSpaceParenthesis");
        store.setToDefault("placeBraceFunctions");
        store.setToDefault("placeBraceBlocks");
        store.setToDefault("insertCloseBrackets");        
    }    
}
