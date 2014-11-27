/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.contentassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.editor.TypeScriptUIImages;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptModelKinds;
import com.google.common.base.Throwables;

/**
 * A content assist processor which computes completions and sets code completion preferences
 * 
 * @author Asya Vorobyova
 */
public class TypeScriptAssistProcessor implements IContentAssistProcessor {

	/** TypeScript API. */
	private TypeScriptAPI api;
	/** Working file. */
	private IFile file;

	/**
	 * @param api
	 *            api
	 * @param file
	 *            file
	 */
	public TypeScriptAssistProcessor(TypeScriptAPI api, IFile file) {
		this.api = api;
		this.file = file;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		api.updateFileContent(file, viewer.getDocument().get());
		JSONObject completionList = api.getCompletion(file, offset);
		try {
			TypeScriptUIImages imagesFactory = new TypeScriptUIImages();
			String replacement = extractPrefix(viewer.getDocument().get(), offset);
			if (!completionList.has("entries")) {
				return new ICompletionProposal[0];
			}
			JSONArray completions = completionList.getJSONArray("entries");
			int completionsLength = completions.length();
			List<ICompletionProposal> result = new ArrayList<ICompletionProposal>(completionsLength);
			for (int i = 0; i < completionsLength; i++) {

				String original = completions.getJSONObject(i).getString("name");
				if (original.length() < replacement.length())
					continue;
				String prefix = original.substring(0, replacement.length());
				if (prefix.equalsIgnoreCase(replacement)) {
					Image image = imagesFactory.getImageForModelObject(completions.getJSONObject(i));
					JSONObject details = api.getCompletionDetails(file, offset, original);
					if (details.has("displayParts")) { // displayParts was introduced in TS 1.3
						result.add(createCompletionProposal_1_3(original, replacement, offset, image, details));
					} else {
						result.add(createCompletionProposal_1_1(original, replacement, offset, image, details));
					}
				}
			}
			return (ICompletionProposal[]) result.toArray(new ICompletionProposal[result.size()]);

		} catch (JSONException e) {
			throw Throwables.propagate(e);
		}
	}

	private TypeScriptCompletionProposal createCompletionProposal_1_1(String original, String replacement, int offset,
			Image image, JSONObject details) throws JSONException {
		String displayString = original;
		String context = "";
		if (details != null && details.has("kind")) {
			String kind = details.getString("kind");
			if (!TypeScriptModelKinds.Kinds.PRIMITIVE_TYPE.toString().equals(kind)
					&& !TypeScriptModelKinds.Kinds.KEYWORD.toString().equals(kind)
					&& !TypeScriptModelKinds.Kinds.METHOD.toString().equals(kind)
					&& !TypeScriptModelKinds.Kinds.FUNCTION.toString().equals(kind)) {
				displayString += " : ";
			}

			displayString += details.getString("type");
			String fullSymbolName = details.getString("fullSymbolName");
			String[] parts = fullSymbolName.split("\\.");
			if (parts.length > 1) {
				String parentName = fullSymbolName.substring(0,
						fullSymbolName.length() - parts[parts.length - 1].length() - 1);
				context = " - " + parentName;
			}

			if (TypeScriptModelKinds.Kinds.METHOD.toString().equals(kind)) {
				original += "()";
			}
		}
		return new TypeScriptCompletionProposal(original, offset - replacement.length(), replacement.length(),
				original.length(), image, displayString, context);

	}

	private TypeScriptCompletionProposal createCompletionProposal_1_3(String original, String replacement, int offset,
			Image image, JSONObject details) throws JSONException {
		HashMap<String, String> map = new HashMap<>();
		JSONArray parts = details.getJSONArray("displayParts");
		for (int i = 0; i < parts.length(); i++) {
			map.put(parts.getJSONObject(i).getString("kind"), parts.getJSONObject(i).getString("text"));
		}
		JSONObject part1 = parts.getJSONObject(0);
		String part1kind = part1.getString("kind");
		String part1text = part1.getString("text");
		
		String context = "";
		String displayString = "";

		if (parts.length() == 1) {
			displayString = part1text;
		} else if (part1kind.equals("punctuation")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 4; i < parts.length(); i++) {
				sb.append(parts.getJSONObject(i).getString("text"));
			}
			displayString = sb.toString();
			if (displayString.contains("\n")) {
				displayString = map.get("localName");
			}
			if (displayString.indexOf('.') > 0) {
				int idx = displayString.lastIndexOf('.');
				context = " - " + displayString.substring(0, idx);
				displayString = displayString.substring(idx + 1, displayString.length());
			}
		} else if (part1kind.equals("keyword") && part1text.equals("interface")) {
			displayString = parts.getJSONObject(3).getString("text");
		} else if (part1kind.equals("keyword") && part1text.equals("module")) {
			displayString = parts.getJSONObject(3).getString("text");
		} else if (part1kind.equals("keyword") && part1text.equals("class")) {
			displayString = map.get("className");
			if (map.containsKey("moduleName")) {
				context = " - " + map.get("moduleName");
			}
		} else {
			System.err.println("!!!!! " + parts.toString());
		}
		return new TypeScriptCompletionProposal(original, offset - replacement.length(), replacement.length(),
				original.length(), image, displayString, context);
	}

	/**
	 * Calculates word part before a position corresponding to an offset
	 * 
	 * @param text
	 *            a document to get word in
	 * @param offset
	 *            the given offset
	 * @return the word
	 */
	private String extractPrefix(String text, int offset) {
		String currentPrefix;
		int startOfWordToken = offset;

		char token = 'a';
		if (startOfWordToken > 0) {
			token = text.charAt(startOfWordToken - 1);
		}

		while (startOfWordToken > 0 && (Character.isJavaIdentifierPart(token)) && !('$' == token)) {
			startOfWordToken--;
			if (startOfWordToken == 0) {
				break; // word goes right to the beginning of the doc
			}
			token = text.charAt(startOfWordToken - 1);
		}

		if (startOfWordToken != offset) {
			currentPrefix = text.substring(startOfWordToken, offset);
		} else {
			currentPrefix = "";
		}
		return currentPrefix;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}