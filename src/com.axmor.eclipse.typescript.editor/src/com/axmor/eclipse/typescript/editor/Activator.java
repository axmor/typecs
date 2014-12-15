package com.axmor.eclipse.typescript.editor;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "com.axmor.eclipse.typescript.editor"; //$NON-NLS-1$

	/**
	 * The shared instance
	 */
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			plugin = null;
			TypeScriptUIImages.disposeImageDescriptorRegistry();
		} finally {
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * The method first checks, if the thread calling this method has an associated display. If so,
	 * this display is returned. Otherwise the method returns the default display.
	 * 
	 * @return the standard display to be used
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	protected ImageRegistry createImageRegistry() {
		return TypeScriptUIImages.initializeImageRegistry();
	}

	/**
	 * Print error message to Error log.
	 * 
	 * @param e
	 *            exception
	 */
	public static void error(final Exception e) {
		plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, e.getMessage(), e));
	}
}
