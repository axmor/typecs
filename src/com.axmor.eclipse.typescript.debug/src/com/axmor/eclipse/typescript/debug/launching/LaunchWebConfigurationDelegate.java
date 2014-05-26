/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.debug.launching;

import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_LAUNCH_WEB_HOST;
import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_LAUNCH_WEB_PORT;
import static com.axmor.eclipse.typescript.debug.launching.TypeScriptDebugConstants.TS_LAUNCH_WEB_WIP;

import org.chromium.debug.core.model.JavascriptVmEmbedder.ConnectionToRemote;
import org.chromium.debug.core.model.JavascriptVmEmbedder;
import org.chromium.debug.core.model.JavascriptVmEmbedderFactory;
import org.chromium.debug.core.model.NamedConnectionLoggerFactory;
import org.chromium.debug.ui.DialogBasedTabSelector;
import org.chromium.sdk.ConnectionLogger;
import org.chromium.sdk.util.Destructable;
import org.chromium.sdk.util.DestructingGuard;
import org.chromium.sdk.wip.WipBackend;
import org.chromium.sdk.wip.eclipse.BackendRegistry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import com.axmor.eclipse.typescript.debug.model.TypeScriptDebugTarget;

/**
 * Launcher for debug TypeScript application from Web browser remote connection.
 * 
 * @author Konstantin Zaitcev
 */
public class LaunchWebConfigurationDelegate implements
		ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			final ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		String host = configuration.getAttribute(TS_LAUNCH_WEB_HOST,
				"localhost");
		int port = configuration.getAttribute(TS_LAUNCH_WEB_PORT, 9222);
		int backendId = configuration.getAttribute(TS_LAUNCH_WEB_WIP, 0);
		WipBackend backend = BackendRegistry.INSTANCE.getBackends().get(
				backendId);

		ConnectionToRemote remoteServer = JavascriptVmEmbedderFactory
				.connectToWipBrowser(host, port, backend,
						NO_CONNECTION_LOGGER_FACTORY,
						NO_CONNECTION_LOGGER_FACTORY,
						DialogBasedTabSelector.WIP_INSTANCE);

		try {


			DestructingGuard destructingGuard = new DestructingGuard();
			try {
				Destructable lauchDestructor = new Destructable() {
					public void destruct() {
						if (!launch.hasChildren()) {
							DebugPlugin.getDefault().getLaunchManager()
									.removeLaunch(launch);
						}
					}
				};

				destructingGuard.addValue(lauchDestructor);

				DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
				
				JavascriptVmEmbedder.VmConnector connector = remoteServer.selectVm();
				if (connector == null) {
					return;
				}
				final IDebugTarget target = new TypeScriptDebugTarget(launch, connector);

				Destructable targetDestructor = new Destructable() {
					public void destruct() {
						try {
							target.terminate();
						} catch (DebugException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				destructingGuard.addValue(targetDestructor);

				launch.addDebugTarget(target);

				monitor.done();

				// All OK
				destructingGuard.discharge();
			} finally {
				destructingGuard.doFinally();
			}

		} finally {
			remoteServer.disposeConnection();
		}
	}

	private static final NamedConnectionLoggerFactory NO_CONNECTION_LOGGER_FACTORY = new NamedConnectionLoggerFactory() {
		public ConnectionLogger createLogger(String title) {
			return null;
		}
	};
}
