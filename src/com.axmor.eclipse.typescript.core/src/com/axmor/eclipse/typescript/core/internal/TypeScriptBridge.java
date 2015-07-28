/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.axmor.eclipse.typescript.core.TypeScriptUtils;
import com.axmor.eclipse.typescript.core.i18n.Messages;
import com.axmor.eclipse.typescript.core.internal.TSBridgeService.Client;
import com.axmor.eclipse.typescript.core.ui.ErrorDialog;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;

/**
 * Bridge process to TypeScript Compiler API.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptBridge implements Runnable {
    /** Indicate that problem with bridge was notified. */
    private static final AtomicBoolean NOTIFY_ERROR = new AtomicBoolean(false);

    /** Empty json object */
    private static final JSONObject EMPTY_JSON_OBJECT = new JSONObject();

    /** Buffer size for copy streams. */
    private static final int BUFF_SIZE = 1024;

    /** Location of bridge libraries in plugin. */
	private static final String LIB_BRIDGE = "lib/typescript-bridge";

    /** Underlie NodeJS process. */
    private Process p;

    /** Stop indication. */
    private boolean stopped = false;

    /** Base directory for source scanning. */
    private final File baseDirectory;

    /** Output stream from NodeJS process. */
    private MessageConsoleStream outStream;
    /** Error stream from NodeJS process. */
    private MessageConsoleStream errorStream;

    /** Communication port. */
    private int port;
	/** TS Console Log level. */
	private String logLevel;

	/** TSBridgeService client. */
	private Client client;

	private TSocket transport;

    /**
     * Create TypeScript bridge.
     * 
     * @param baseDirectory
     *            base source directory
     */
    public TypeScriptBridge(File baseDirectory) {
        this.baseDirectory = baseDirectory;
		this.logLevel = Activator.getDefault().getPreferenceStore().getString("ts_log_level");
    }

    @Override
    public void run() {
        if (stopped || NOTIFY_ERROR.get()) {
            return;
        }

        try {
			this.port = getPort();
			File bundleFile = FileLocator.getBundleFile(Activator.getDefault().getBundle());
            String nodeJSPath = TypeScriptUtils.findNodeJS();
			ProcessBuilder ps = new ProcessBuilder(nodeJSPath,
					new File(bundleFile, LIB_BRIDGE + "/js/new_bridge.js").getCanonicalPath(), "src="
							+ baseDirectory.getAbsolutePath().replace('\\', '/'), "serv=true", "port=" + port,
					"log=" + logLevel);
			ps.directory(baseDirectory.getCanonicalFile());
            p = ps.start();
		} catch (IOException e) {
            Activator.getDefault().getLog()
                    .log(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));

            if (!NOTIFY_ERROR.getAndSet(true)) {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        ErrorDialog.open(new Shell(), Messages.TSBridge_InitErrorTitle,
                                Messages.TSBridge_InitErrorMessage);
                    }
                });
            }
        }

        if (p != null) {
            MessageConsole console = new MessageConsole(Messages.TSBridge_Console, null);

            outStream = console.newMessageStream();
            errorStream = console.newMessageStream();
            outStream.println("TS Bridge: port = " + port + ", directory: " + baseDirectory);

			transport = new TSocket("localhost", port);

			try {
				try {
					transport.open();
				} catch (TTransportException e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						// ignore exception
					}
					transport.open();
				}
			} catch (TTransportException e) {
				Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			}

			TBinaryProtocol protocol = new TBinaryProtocol(transport, 10000000, 10000000, false, false);
			client = new TSBridgeService.Client(protocol);

			JSONObject versionObject = invokeBridgeMethod("getVersion", null);
			try {
				outStream.println("TypeScript Version: " + versionObject.getString("version"));
			} catch (JSONException e) {
				Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			}
            // decorate error stream
            errorStream.setActivateOnWrite(true);
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    errorStream.setColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
                }
            });

            connectStreams(p.getErrorStream(), errorStream);
            connectStreams(p.getInputStream(), outStream);
            ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                // ignore exception
            }

        }
    }

    /**
     * Stops engine and corresponding console, destroy undelie NodeJS process.
     */
    public synchronized void stop() {
        if (!stopped) {
            stopped = true;
			transport.close();
            outStream.println("TS bridge closed");
            try {
                Closeables.close(outStream, true);
                Closeables.close(errorStream, true);
            } catch (IOException e) {
                // ignore exception
            }
            p.destroy();
        }
    }

    /**
     * @return the stopped
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
	 * Invoke method on TS Bridge and return JSONObject from bridge.
	 * 
	 * @param method
	 *            name of method
	 * @param file
	 *            optional file attribute
	 * @param position
	 *            optional position
	 * @return result in JSON representation
	 */
	public JSONObject invokeBridgeMethod(String method, IFile file) {
		return invokeBridgeMethod(method, file, 0, null);
	}

	/**
	 * Invoke method on TS Bridge and return JSONObject from bridge.
	 * 
	 * @param method
	 *            name of method
	 * @param file
	 *            optional file attribute
	 * @param position
	 *            optional position
	 * @return result in JSON representation
	 */
	public JSONObject invokeBridgeMethod(String method, IFile file, int position) {
		return invokeBridgeMethod(method, file, position, null);
	}

    /**
	 * Invoke method on TS Bridge and return JSONObject from bridge.
	 * 
	 * @param method
	 *            name of method
	 * @param file
	 *            optional file attribute
	 * @param position
	 *            optional position
	 * @param params
	 *            optional additional parameters
	 * @param jsonParams
	 *            JSON parameters
	 * 
	 * @return result in JSON representation
	 */
	public synchronized JSONObject invokeBridgeMethod(String method, IFile file, int position, String params) {
        try {
            if (port == 0) {
                return EMPTY_JSON_OBJECT;
            }
			String path = "";
			if (file != null) {
				if ("addFile".equals(method) || "compile".equals(method)) {
					path = file.getLocation().toFile().getAbsolutePath().replace('\\', '/');
				} else {
					path = file.getProjectRelativePath().toString();
				}
			}
			if (client == null) {
				return EMPTY_JSON_OBJECT;
			}
			String result = client.invoke(method, path, position, params);
			if ("null".equals(result) || result.trim().isEmpty()) {
				return EMPTY_JSON_OBJECT;
			}
			return new JSONObject(result);
		} catch (JSONException | TException e) {
            throw Throwables.propagate(e);
        }
    }
    
	@SuppressWarnings("unused")
	private boolean isFileNameExist(String name) {
		JSONObject res = invokeBridgeMethod("getScriptFileNames", null);
    	JSONArray existNames;
    	List<String> list = new ArrayList<String>();
		try {
			existNames = res.getJSONArray("names");			
	    	for(int i = 0; i < existNames.length(); i++){
	    	    list.add(existNames.getString(i));
	    	}
		} catch (JSONException e) {
			throw Throwables.propagate(e);
		}
		return list.contains(name);
    }

    /**
     * Connect input streams to message console stream for text pipeline.
     * 
     * @param in
     *            input stream from process
     * @param out
     *            console output stream
     */
    private void connectStreams(final InputStream in, final MessageConsoleStream out) {
        // run stream pipeline
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] buff = new byte[BUFF_SIZE];
                    int len = 0;
                    while ((len = in.read(buff)) >= 0) {
                        if (len > 0) {
                            out.write(buff, 0, len);
                        }
                    }
                    TypeScriptBridge.this.stop();
                } catch (IOException e) {
                    // ignore error
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * @return path to lib.d.ts file.
     */
    public static String getStdLibPath() {
		try {
			File bundleFile = FileLocator.getBundleFile(Activator.getDefault().getBundle());
			return new File(bundleFile, LIB_BRIDGE + "/ts/lib.d.ts")
					.getCanonicalPath();
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
    }

	private int getPort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}
}
