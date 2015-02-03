/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.editor.hover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.TypeScriptAPIFactory;
import com.axmor.eclipse.typescript.editor.Activator;
import com.axmor.eclipse.typescript.editor.TypeScriptEditor;
import com.axmor.eclipse.typescript.editor.TypeScriptProjectionViewer;

/**
 * @author Konstantin Zaitcev
 */
@SuppressWarnings("restriction")
public class TypeScriptTextHover extends DefaultTextHover implements ITextHoverExtension {

	private IInformationControlCreator infoCtrlCreator;
	private static String cssStyle;

	public TypeScriptTextHover(ISourceViewer sourceViewer) {
		super(sourceViewer);
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (textViewer instanceof TypeScriptProjectionViewer) {
			TypeScriptEditor editor = ((TypeScriptProjectionViewer) textViewer).getEditor();
			if (editor.getEditorInput() instanceof FileEditorInput) {
				IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
				JSONObject info = TypeScriptAPIFactory.getTypeScriptAPI(file.getProject()).getSignature(file,
						hoverRegion.getOffset());
				if (info != null && info.has("displayParts")) {
					try {
						StringBuffer sb = new StringBuffer();
						HTMLPrinter.insertPageProlog(sb, 0, getCSSStyles());
						JSONArray parts = info.getJSONArray("displayParts");
						sb.append("<h5>");
						for (int i = 0; i < parts.length(); i++) {
							sb.append(parts.getJSONObject(i).getString("text"));
						}
						sb.append("</h5>");
						if (info.has("documentation") && info.getJSONArray("documentation").length() > 0) {
							HTMLPrinter.addParagraph(sb,
									info.getJSONArray("documentation").getJSONObject(0).getString("text"));
						}
						HTMLPrinter.addPageEpilog(sb);
						return sb.toString();
					} catch (JSONException e) {
						// ignore
					}
				}
			}
		}
		return null;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		if (infoCtrlCreator == null) {
			infoCtrlCreator = new TypeScriptInformationControlCreator();
		}
		return infoCtrlCreator;
	}

	protected String getCSSStyles() {
		if (cssStyle == null) {
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
					cssStyle = buffer.toString();
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
		String css = cssStyle;
		if (css != null) {
			FontData fontData = JFaceResources.getFontRegistry().getFontData(
					PreferenceConstants.APPEARANCE_JAVADOC_FONT)[0];
			css = HTMLPrinter.convertTopLevelFont(css, fontData);
		}
		return css;
	}

	/**
	 * @author Konstantin Zaitcev
	 */
	private static class TypeScriptInformationControlCreator extends AbstractReusableInformationControlCreator {

		private IInformationControlCreator infoCtrlCreator;

		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			if (BrowserInformationControl.isAvailable(parent)) {
				String font = JFaceResources.DEFAULT_FONT;
				BrowserInformationControl iControl = new BrowserInformationControl(parent, font,
						EditorsUI.getTooltipAffordanceString()) {
					@Override
					public IInformationControlCreator getInformationPresenterControlCreator() {
						if (infoCtrlCreator == null) {
							init();
						}
						return infoCtrlCreator;
					}
				};
				return iControl;
			} else {
				return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		}

		private void init() {
			infoCtrlCreator = new AbstractReusableInformationControlCreator() {
				@Override
				public IInformationControl doCreateInformationControl(Shell parent) {
					ToolBarManager tbm = new ToolBarManager(SWT.FLAT);
					String font = JFaceResources.DEFAULT_FONT;
					return new BrowserInformationControl(parent, font, tbm);
				}
			};
		}
	}
}