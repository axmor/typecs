/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.axmor.eclipse.typescript.core.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptEditorSettings;

/**
 * @author Konstantin Zaitcev
 */
public class TypescriptEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    /** IndentSize field. */
    private Text indentSize;
    /** newLineChar field. */
    private Text newLineChar;
    /** tabSize field. */
    private Text tabSize;
    /** convertTabs field. */
    private Button convertTabs;
    /** insertSpaceComma field. */
    private Button insertSpaceComma;
    /** insertSpaceSemicolon field. */
    private Button insertSpaceSemicolon;
    /** insertSpaceBinary field. */
    private Button insertSpaceBinary;
    /** insertSpaceKeyword field. */
    private Button insertSpaceKeyword;
    /** insertSpaceFunction field. */
    private Button insertSpaceFunction;
    /** insertSpaceParenthesis field. */
    private Button insertSpaceParenthesis;
    /** braceNewLineFunction field. */
    private Button braceNewLineFunction;
    /** braceNewLineBlock field. */
    private Button braceNewLineBlock;
    /** insertCloseBrackets field. */
    private Button insertCloseBrackets;

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("This page allows you to configure TypeScript Editor options and formatting rules");
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, 0, 0, GridData.FILL);
        Group editor = SWTFactory.createGroup(composite, "Editor settings", 4, 1, GridData.FILL_HORIZONTAL);
        SWTFactory.createLabel(editor, "Indent size:", 1);
        indentSize = SWTFactory.createText(editor, SWT.BORDER, 1, "4");

        SWTFactory.createLabel(editor, "New line character:", 1);
        newLineChar = SWTFactory.createText(editor, SWT.BORDER, 1, "\\r\\n");

        SWTFactory.createLabel(editor, "Tab size:", 1);
        tabSize = SWTFactory.createText(editor, SWT.BORDER, 1, "4");

        SWTFactory.createHorizontalSpacer(editor, 1);
        convertTabs = SWTFactory.createCheckButton(editor, "Convert tabs to spaces", null, true, 2);

        Group formatting = SWTFactory.createGroup(composite, "Formatting rules", 1, 1, GridData.FILL_HORIZONTAL);
        insertSpaceComma = SWTFactory
                .createCheckButton(formatting, "Insert space after comma delimiter", null, true, 1);
        insertSpaceSemicolon = SWTFactory.createCheckButton(formatting,
                "Insert space after semicolon in for statements", null, true, 1);
        insertSpaceBinary = SWTFactory.createCheckButton(formatting, "Insert space before and after binary operators",
                null, true, 1);
        insertSpaceKeyword = SWTFactory.createCheckButton(formatting,
                "Insert space after keywords in control flow statements", null, true, 1);
        insertSpaceFunction = SWTFactory.createCheckButton(formatting,
                "Insert space after function keyword for anonymous functions", null, false, 1);
        insertSpaceParenthesis = SWTFactory.createCheckButton(formatting,
                "Insert space after opening and before closing non empty parenthesis", null, false, 1);
        braceNewLineFunction = SWTFactory.createCheckButton(formatting, "Place open brace on new line for functions",
                null, false, 1);
        braceNewLineBlock = SWTFactory.createCheckButton(formatting, "Place open brace on new line for control blocks",
                null, false, 1);
        insertCloseBrackets = SWTFactory.createCheckButton(formatting, "Automatically append closing characters", null,
                true, 1);

        loadSettings();
        return composite;
    }

    /**
     * Loads settings from preference store into UI.
     */
    private void loadSettings() {
        TypeScriptEditorSettings settings = TypeScriptEditorSettings.load();
        indentSize.setText(String.valueOf(settings.getIndentSize()));
        newLineChar.setText(settings.getNewLineCharacter().replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r"));
        tabSize.setText(String.valueOf(settings.getTabSize()));

        convertTabs.setSelection(settings.isConvertTabsToSpaces());
        insertSpaceComma.setSelection(settings.isInsertSpaceAfterCommaDelimiter());
        insertSpaceSemicolon.setSelection(settings.isInsertSpaceAfterSemicolon());
        insertSpaceBinary.setSelection(settings.isInsertSpaceBinaryOperators());
        insertSpaceKeyword.setSelection(settings.isInsertSpaceAfterKeywords());
        insertSpaceFunction.setSelection(settings.isInsertSpaceAfterFunction());
        insertSpaceParenthesis.setSelection(settings.isInsertSpaceAfterNonemptyParenthesis());
        braceNewLineFunction.setSelection(settings.isPlaceOpenBraceFunctions());
        braceNewLineBlock.setSelection(settings.isPlaceOpenBraceControlBlocks());
        insertCloseBrackets.setSelection(settings.isInsertCloseBrackets());
    }

    @Override
    public boolean performOk() {
        TypeScriptEditorSettings settings = TypeScriptEditorSettings.load();
        settings.setIndentSize(Integer.parseInt(indentSize.getText()));
        settings.setNewLineCharacter(newLineChar.getText().replaceAll("\\\\n", "\n").replaceAll("\\\\r", "\r"));
        settings.setTabSize(Integer.parseInt(tabSize.getText()));

        settings.setConvertTabsToSpaces(convertTabs.getSelection());
        settings.setInsertSpaceAfterCommaDelimiter(insertSpaceComma.getSelection());
        settings.setInsertSpaceAfterSemicolon(insertSpaceSemicolon.getSelection());
        settings.setInsertSpaceBinaryOperators(insertSpaceBinary.getSelection());
        settings.setInsertSpaceAfterKeywords(insertSpaceKeyword.getSelection());
        settings.setInsertSpaceAfterFunction(insertSpaceFunction.getSelection());
        settings.setInsertSpaceAfterNonemptyParenthesis(insertSpaceParenthesis.getSelection());
        settings.setPlaceOpenBraceFunctions(braceNewLineFunction.getSelection());
        settings.setPlaceOpenBraceControlBlocks(braceNewLineBlock.getSelection());
        settings.setInsertCloseBrackets(insertCloseBrackets.getSelection());
        settings.save();
        return true;
    }

    @Override
    protected void performDefaults() {
        TypeScriptEditorSettings.setToDefault();
        loadSettings();
        super.performDefaults();
    }
}
