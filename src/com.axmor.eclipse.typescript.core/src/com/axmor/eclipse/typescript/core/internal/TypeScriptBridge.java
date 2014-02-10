/*******************************************************************************
 * Copyright (c) 2014 Axmor Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.axmor.eclipse.typescript.core.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.axmor.eclipse.typescript.core.Activator;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

/**
 * Bridge process to TypeScript Compiler API.
 * 
 * @author Konstantin Zaitcev
 */
public class TypeScriptBridge implements Runnable {

    /** Wait lock constant. */
    private static final int WAIT_TIMEOUT = 10;

    /** Empty json object */
    private static final JSONObject EMPTY_JSON_OBJECT = new JSONObject();

    /** Buffer size for copy streams. */
    private static final int BUFF_SIZE = 1024;

    /** Location of bridge libraries in plugin. */
    private static final String LIB_BRIDGE = "lib/bridge";

    /** Lock object for correct multi-thread access. */
    private CountDownLatch lock = new CountDownLatch(1);

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

    /** Compiler version. */
    private String version;

    /**
     * Create TypeScript bridge.
     * 
     * @param baseDirectory
     *            base source directory
     */
    public TypeScriptBridge(File baseDirectory) {
        this.baseDirectory = baseDirectory;
        this.version = Activator.getDefault().getPreferenceStore().getString("compiler_version");

        if (Strings.isNullOrEmpty(this.version)) {
            this.version = "0.9.5";
        }
    }

    @Override
    public void run() {
        if (stopped) {
            return;
        }
        try {
            File bundleFile = FileLocator.getBundleFile(Activator.getDefault().getBundle());

            ProcessBuilder ps = new ProcessBuilder("node", "bridge.js", "version=" + version, "src=\""
                    + baseDirectory.getAbsolutePath().replace('\\', '/') + "\"", "serv=true", "log=debug")
                    .directory(new File(bundleFile, LIB_BRIDGE));

            p = ps.start();
            String portLine = new BufferedReader(new InputStreamReader(p.getErrorStream())).readLine();
            if (!portLine.startsWith("@") && !portLine.endsWith("@")) {
                throw new IOException("Cannot start TS bridge " + portLine);
            }
            port = Integer.parseInt(portLine.substring(1, portLine.length() - 1));
            lock.countDown();
            
            MessageConsole console = new MessageConsole("TS Bridge Console", null);

            outStream = console.newMessageStream();
            errorStream = console.newMessageStream();
            outStream.println("TS Bridge: port = " + port + ", directory: " + baseDirectory);

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
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            Activator.getDefault().getLog()
                    .log(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e));
        }

        if (!stopped) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(new Shell(), "TypeScript Runtime Error",
                            "Error in TypeScript Runtime Initialization.");
                }
            });
        }
    }

    /**
     * Stops engine and corresponding console, destroy undelie NodeJS process.
     */
    public synchronized void stop() {
        if (!stopped) {
            stopped = true;
            outStream.println("TS Bridge Terminated");
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
     * Invoke method on TS Bridge and return JSONObject from bridge.
     * 
     * @param method
     *            name of method
     * @param file
     *            optional file attribute
     * @param params
     *            optional additional parameters
     * @return result in JSON representation
     */
    public JSONObject invokeBridgeMethod(String method, IFile file, String params) {
        return invokeBridgeMethod(method, file, params, null);
    }

    /**
     * Invoke method on TS Bridge and return JSONObject from bridge.
     * 
     * @param method
     *            name of method
     * @param file
     *            optional file attribute
     * @param params
     *            optional additional JSON parameters
     * @return result in JSON representation
     */
    public JSONObject invokeBridgeMethod(String method, IFile file, JSONObject params) {
        return invokeBridgeMethod(method, file, null, params);
    }

    /**
     * Invoke method on TS Bridge and return JSONObject from bridge.
     * 
     * @param method
     *            name of method
     * @param file
     *            optional file attribute
     * @param params
     *            optional additional parameters
     * @param jsonParams
     *            JSON parameters
     * 
     * @return result in JSON representation
     */
    private JSONObject invokeBridgeMethod(String method, IFile file, String params, JSONObject jsonParams) {
        try {
            try {
                lock.await(WAIT_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore exception
            }
            try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), port)) {
                JSONObject obj = new JSONObject().put("method", method);
                if (file != null) {
                    obj.put("file", file.getProjectRelativePath().toString());
                }

                if (!Strings.isNullOrEmpty(params)) {
                    obj.put("params", params);
                } else if (jsonParams != null) {
                    obj.put("params", jsonParams);
                }

                try (PrintWriter writer = new PrintWriter(socket.getOutputStream())) {
                    writer.write(obj.toString());
                    writer.flush();
                    socket.shutdownOutput();

                    try (InputStreamReader reader = new InputStreamReader(socket.getInputStream())) {
                        String str = CharStreams.toString(reader);
                        if ("null".equals(str)) {
                            return EMPTY_JSON_OBJECT;
                        }
                        return new JSONObject(str);
                    }
                }
            }
        } catch (IOException | JSONException e) {
            throw Throwables.propagate(e);
        }
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

}
