/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.contentassist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPI;
import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptUIImages;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptImageKeys;
import com.axmor.eclipse.typescript.editor.parser.TypeScriptModelKinds;
import com.axmor.eclipse.typescript.editor.preferences.TypescriptTemplateAccess;
import com.google.common.base.Throwables;

/**
 * A content assist processor which computes completions and sets code completion preferences
 * 
 * @author Asya Vorobyova
 */
@SuppressWarnings("restriction")
public class TypeScriptAssistProcessor extends TemplateCompletionProcessor {
	private static String fgCSSStyles;

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
						result.add(createCompletionProposal_1_3(original, replacement, offset, image, completions.getJSONObject(i), details));
					} else {
						result.add(createCompletionProposal_1_1(original, replacement, offset, image, details));
					}
				}
			}
			return mergeProposals(result.toArray(new ICompletionProposal[result.size()]),
					determineTemplateProposalsForContext(viewer, offset));

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
				original.length(), image, displayString, context, null);

	}

	private TypeScriptCompletionProposal createCompletionProposal_1_3(String original, String replacement, int offset,
			Image image, JSONObject completion, JSONObject details) throws JSONException {
		
		String context = "";
		String displayString = "";
		
		JSONArray parts = details.getJSONArray("displayParts");
		
		JSONObject part1 = parts.getJSONObject(0);
		String part1kind = part1.getString("kind");
		String part1text = part1.getString("text");
		
		if (parts.length() == 1) {
			
			displayString = part1text;
			
		} else if ("punctuation".equals(part1kind)) {
			
			StringBuilder sb = new StringBuilder();
			for (int i = 4; i < parts.length(); i++) {
				sb.append(parts.getJSONObject(i).getString("text"));
			}
			displayString = sb.toString();
			if (displayString.contains("\n")) {
				displayString = getFirstValue(parts, "localName");
			}
			
			Object kind = completion.get("kind");
			int leftBraceIndex = displayString.indexOf("(");
			int colonIndex = displayString.indexOf(":");
			
			int idx = -1;
			if (("method".equals(kind) || "function".equals(kind)) && leftBraceIndex > 0) {
				idx = displayString.lastIndexOf('.',leftBraceIndex);
			} else if ("property".equals(kind) && colonIndex > 0) {
				idx = displayString.lastIndexOf('.',colonIndex);
			} else {
				idx = displayString.lastIndexOf('.'); 
			}
			
			if (idx > 0) {
				context = displayString.substring(0, idx);
				displayString = displayString.substring(idx + 1, displayString.length());
			}
			
		} else {
			
			String keyWord = part1kind.equals("keyword") ? part1text : null;
			
			HashMap<String, String> map = new HashMap<>();
			
			StringBuilder fullModuleNameBldr = new StringBuilder();
			for (int i = 0; i < parts.length(); i++) {
				JSONObject jsonObject = parts.getJSONObject(i);
				String kind = jsonObject.getString("kind");
				String value = jsonObject.getString("text");
				if ("moduleName".equals(kind)) {
					if (fullModuleNameBldr.length() > 0) {
						fullModuleNameBldr.append(".");
					}
					fullModuleNameBldr.append(value);
				} else {
					map.put(kind,value);
				}
			}
			
			String fullModuleName = fullModuleNameBldr.toString();
			if ("interface".equals(keyWord)) {
				if (map.containsKey("interfaceName")) {
					displayString = map.get("interfaceName");
				} else {
					displayString = getFirstValue(parts, "localName");	
				}
				context = fullModuleName;
			} else if ("module".equals(keyWord)) {
				int idx = fullModuleName.lastIndexOf(".");
				if (idx < 0) {
					displayString = fullModuleName;
				} else {
					displayString = fullModuleName.substring(idx+1);
					context = fullModuleName;
				}
			} else if ("class".equals(keyWord)) {
				displayString = map.get("className");
				context = fullModuleName;
			} else if ("import".equals(keyWord)) {
				displayString = getFirstValue(parts,"text");
				context = getAliasDescription(parts);
			}
			
		}
				
		String doc = null;
		if (details.has("documentation")) {
			JSONArray docs = details.getJSONArray("documentation");
			if (docs != null && docs.length() > 0) {
				StringBuffer sb = new StringBuffer();
				HTMLPrinter.insertPageProlog(sb, 0, getCSSStyles());
				for (int i = 0; i < docs.length(); i++) {

					JSONObject docItem = docs.getJSONObject(i);
					if (docItem.getString("kind").equals("lineBreak")) {
						sb.append("<br>");
					} else {
						String docItemTxt = docItem.getString("text");
						if (docItemTxt.startsWith("@return")) {
							sb.append("<br>");
							HTMLPrinter.addSmallHeader(sb, "Returns:");
							sb.append("<pre>       </pre>").append(docItemTxt.trim().substring("@return".length() + 1));
						} else {
							sb.append(docItemTxt);
						}
					}
				}
				HTMLPrinter.addPageEpilog(sb);
				doc = sb.toString();
			}
		}
		return new TypeScriptCompletionProposal(original, offset - replacement.length(), replacement.length(),
				original.length(), image, displayString, prefixContext(context), doc);
	}
	
	private String prefixContext(String context) {
		return context != null && !context.isEmpty() ? " - " + context : ""; 
	}

	private String getFirstValue(JSONArray jsonArray, String kind) throws JSONException {
		String firstValue = null;
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (kind.equals(jsonObject.getString("kind"))) {
					firstValue = jsonObject.getString("text");
					break;
				}
			}
		}
		if (firstValue == null) {
			firstValue = "";
		}
		return firstValue;
	}
	
	private String getAliasDescription(JSONArray parts) throws JSONException {
		StringBuilder descriptionBuilder = new StringBuilder();
		boolean collect = false;
		if (parts != null) {
			for (int i = 0; i < parts.length(); i++) {
				JSONObject o = parts.getJSONObject(i);
				if ("punctuation".equals(o.getString("kind")) && "=".equals(o.getString("text"))) {
					collect = true;
				} else if (collect) {
					String text = o.getString("text");
					if (text != null) {
						descriptionBuilder.append(text.trim());
					}
				}
			}
		}
		return descriptionBuilder.toString();
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

	private ICompletionProposal[] mergeProposals(ICompletionProposal[] proposals1, ICompletionProposal[] proposals2) {

		ICompletionProposal[] combinedProposals = new ICompletionProposal[proposals1.length + proposals2.length];

		System.arraycopy(proposals1, 0, combinedProposals, 0, proposals1.length);
		System.arraycopy(proposals2, 0, combinedProposals, proposals1.length, proposals2.length);

		return combinedProposals;
	}

	private ICompletionProposal[] determineTemplateProposalsForContext(ITextViewer viewer, int offset) {
		ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();

		// adjust offset to end of normalized selection
		int newoffset = offset;
		if (selection.getOffset() == newoffset) {
			newoffset = selection.getOffset() + selection.getLength();
		}

		String prefix = extractPrefix(viewer, newoffset);
		Region region = new Region(newoffset - prefix.length(), prefix.length());
		TemplateContext context = createContext(viewer, region);
		if (context == null) {
			return new ICompletionProposal[0];
		}

		context.setVariable("selection", selection.getText()); // name of the selection variables {line, word}_selection //$NON-NLS-1$

		String templateContextId = getContextType(viewer, region).getId();
		Template[] templates = getTemplates(templateContextId);

		List<ICompletionProposal> matches = new ArrayList<ICompletionProposal>();
		for (int i = 0; i < templates.length; i++) {
			Template template = templates[i];
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				continue;
			}
			if (template.matches(prefix, templateContextId) && template.getName().startsWith(prefix)) {
				matches.add(createProposal(template, context, (IRegion) region, getRelevance(template, prefix)));
			}
		}

		return matches.toArray(new ICompletionProposal[matches.size()]);
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

	@Override
	protected Template[] getTemplates(String contextTypeId) {
		return TypescriptTemplateAccess.getDefault().getTemplateStore().getTemplates(contextTypeId);
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		return TypescriptTemplateAccess.getDefault().getContextTypeRegistry().getContextType("typeScript");
	}

	@Override
	protected Image getImage(Template template) {
		return TypeScriptUIImages.getImage(TypeScriptImageKeys.IMG_TEMPLATE_PROPOSAL);
	}

	protected String getCSSStyles() {
		if (fgCSSStyles == null) {
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			URL url = bundle.getEntry("/css/JavadocHoverStyleSheet.css"); //$NON-NLS-1$
			if (url != null) {
				BufferedReader reader = null;
				try {
					url = FileLocator.toFileURL(url);
					reader = new BufferedReader(new InputStreamReader(url.openStream()));
					StringBuffer buffer = new StringBuffer(200);
					String line = reader.readLine();
					while (line != null) {
						buffer.append(line);
						buffer.append('\n');
						line = reader.readLine();
					}
					fgCSSStyles = buffer.toString();
				} catch (IOException ex) {
				} finally {
					try {
						if (reader != null)
							reader.close();
					} catch (IOException e) {
					}
				}

			}
		}
		String css = fgCSSStyles;
		if (css != null) {
			FontData fontData = JFaceResources.getFontRegistry().getFontData(
					PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
			css = HTMLPrinter.convertTopLevelFont(css, fontData);
		}
		return css;
	}

}