/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.ui;

import java.net.URI;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.axmor.eclipse.typescript.core.Activator;

/**
 * @author Konstantin Zaitcev
 */
public final class ErrorDialog extends MessageDialog {

    /**
     * Opens error dialog.
     * 
     * @param parent
     *            parent
     * @param title
     *            title
     * @param message
     *            message
     * @return <code>true</code> if return code success
     */
    public static boolean open(Shell parent, String title, String message) {
        ErrorDialog dialog = new ErrorDialog(parent, title, null, message, ERROR,
                new String[] { IDialogConstants.OK_LABEL }, 0);
        dialog.setShellStyle(dialog.getShellStyle() | SWT.SHEET);
        return dialog.open() == 0;
    }

    /**
     * @param parentShell
     *            shell
     * @param dialogTitle
     *            title
     * @param dialogTitleImage
     *            image
     * @param dialogMessage
     *            message
     * @param imageType
     *            tipe
     * @param buttonLabels
     *            labels
     * @param defaultIndex
     *            index
     */
    private ErrorDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
            int imageType, String[] buttonLabels, int defaultIndex) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, imageType, buttonLabels, defaultIndex);
    }

    @Override
    protected Control createMessageArea(Composite composite) {
        // create composite
        // create image
        Image image = getImage();
        if (image != null) {
            imageLabel = new Label(composite, SWT.NULL);
            image.setBackground(imageLabel.getBackground());
            imageLabel.setImage(image);
            imageLabel.getAccessible().addAccessibleListener(new AccessibleAdapter() {
                @Override
                public void getName(AccessibleEvent event) {
                    final String accessibleMessage = JFaceResources.getString("error");
                    if (accessibleMessage == null) {
                        return;
                    }
                    event.result = accessibleMessage;
                }
            });
            GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);
        }
        // create message
        if (message != null) {
            FormToolkit toolkit = new FormToolkit(Display.getDefault());
            Composite toolkitComp = toolkit.createComposite(composite);
            toolkitComp.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL));
            FormText text = toolkit.createFormText(toolkitComp, false);
            text.setText(message, true, true);
            text.setBackground(composite.getBackground());
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false)
                    .hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
                    .applyTo(toolkitComp);
            text.addHyperlinkListener(new HyperlinkAdapter() {
                public void linkActivated(HyperlinkEvent event) {
                    try {
                        URI uri = URI.create((String) event.data);
                        IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport()
                                .createBrowser("error_editor_dialog");
                        browser.openURL(uri.toURL());
                    } catch (Exception e) {
                        Activator.error(e);
                    }
                }
            });
        }
        return composite;
    }
}
