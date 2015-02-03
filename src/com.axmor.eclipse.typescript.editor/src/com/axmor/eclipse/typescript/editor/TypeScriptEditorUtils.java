/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptUtils;
import com.google.common.base.Throwables;
import com.ibm.icu.text.UTF16;

/**
 * Class containing helpful utilities for editing purposes
 * 
 * @author Asya Vorobyova
 * 
 */
public final class TypeScriptEditorUtils {
    /**
     * Protect from initialization.
     */
    private TypeScriptEditorUtils() {

    }

    /**
     * Opens declaration for a selection in a given editor
     * 
     * @param editor
     *            a text editor
     */
    public static void openDeclaration(TypeScriptEditor editor) {
        ISelection selection = editor.getSelectionProvider().getSelection();
        IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
        JSONArray typeDef = editor.getApi().getTypeDefinition(file, ((ITextSelection) selection).getOffset());
        if (typeDef.length() > 0) {
            try {
                JSONObject object = typeDef.getJSONObject(0);
                Position position = getPosition(object);
                String containerName = object.getString("fileName");
                if (containerName.equals(file.getProjectRelativePath().toString())) {
                    // go to the previously defined position in the same editor
                    editor.selectAndReveal(position.offset, 0);
                } else {
                    // open the editor for a corresponding file and select needful declaration
                    IPath path = new Path(containerName);
                    IFile newFile = file.getProject().getFile(path);
                    IEditorPart editorPart = IDE.openEditor(Activator.getDefault().getWorkbench()
                            .getActiveWorkbenchWindow().getActivePage(), newFile, true);
                    if (editorPart != null && editorPart instanceof AbstractTextEditor) {
                        ((AbstractTextEditor) editorPart).selectAndReveal(position.offset, 0);
                    }
                }
            } catch (JSONException | CoreException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    /**
     * Finds a region which corresponds to a word in a given document which contains a given offset.
     * Copypasted from jdt method.
     * 
     * @param document
     *            the current document
     * @param offset
     *            the given offset
     * @return the region
     */
    public static IRegion findWord(IDocument document, int offset) {
        int start = -2;
        int end = -1;

        try {
            int pos = offset;
            char c;

            while (pos >= 0) {
                c = document.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    // Check for surrogates
                    if (!UTF16.isSurrogate(c)) {
                        /*
                         * Here we should create the code point and test whether it is a Java
                         * identifier part. Currently this is not possible because
                         * java.lang.Character in 1.4 does not support surrogates and because
                         * com.ibm.icu.lang.UCharacter.isJavaIdentifierPart(int) is not correctly
                         * implemented.
                         */
                        break;
                    }
                }
                --pos;
            }
            start = pos;

            pos = offset;
            int length = document.getLength();

            while (pos < length) {
                c = document.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    if (!UTF16.isSurrogate(c)) {
                        break;
                        /*
                         * Here we should create the code point and test whether it is a Java
                         * identifier part. Currently this is not possible because
                         * java.lang.Character in 1.4 does not support surrogates and because
                         * com.ibm.icu.lang.UCharacter.isJavaIdentifierPart(int) is not correctly
                         * implemented.
                         */
                    }

                }
                ++pos;
            }
            end = pos;

        } catch (BadLocationException x) {
        }

        if (start >= -1 && end > -1) {
            if (start == offset && end == offset) {
                return new Region(offset, 0);
            } else if (start == offset) {
                return new Region(start, end - start);
            } else {
                return new Region(start + 1, end - start - 1);
            }
        }

        return null;
    }
    
    /**
     * Load content from file
     * 
     * @param filename
     *            a file name
     *            
     * @return content
     * 		content from file
     */
    public static String loadPreviewContentFromFile(InputStream input) {
		String line;
		String separator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer= new StringBuffer(512);
		BufferedReader reader= null;
		try {
			reader= new BufferedReader(new InputStreamReader(input));
			while ((line= reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			Activator.error(io);
		} finally {
			if (reader != null) {
				try { reader.close(); } catch (IOException e) {}
			}
		}
		return buffer.toString();
	}

    public static Position getPosition(JSONObject json) throws JSONException {
		if (TypeScriptUtils.isTypeScriptLegacyVersion()) {
			int startPos = json.getInt("minChar");
			int endPos = json.getInt("limChar");
			return new Position(startPos, endPos - startPos);
		} else {
			if (json.has("spans")) {
				JSONArray spans = json.getJSONArray("spans");
				if (spans != null && spans.length() > 0) {
					JSONObject span = spans.getJSONObject(0);
					return new Position(span.getInt("start"), span.getInt("length"));
				}
			} else if (json.has("textSpan")) {
				JSONObject span = json.getJSONObject("textSpan");
				return new Position(span.getInt("start"), span.getInt("length"));
			} else if (json.has("span")) {
				JSONObject span = json.getJSONObject("span");
				return new Position(span.getInt("start"), span.getInt("length"));
			}
			throw new JSONException("Object: " + json + " - does not contains position");
		}
	}
    
    public static JSONObject updatePosition(JSONObject json, int offset, int length) throws JSONException {
        if (TypeScriptUtils.isTypeScriptLegacyVersion()) {
            json.put("minChar", offset);
            json.put("limChar", offset + length);
            return json;
        } else {
            JSONObject span = new JSONObject().put("start", offset).put("length", length);
            if (json.has("spans")) {                
                JSONArray spans = new JSONArray();
                spans.put(span);
                return json.put("spans", spans);
            } else if (json.has("textSpan")) {
                return json.put("textSpan", span);
            } else if (json.has("span")) {
                return json.put("span", span);                
            }
            throw new JSONException("Object: " + json + " - does not contains position");
        }
    }
}
