
package com.axmor.eclipse.typescript.core.test.bridge;

import java.io.File;
import java.net.ServerSocket;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.axmor.eclipse.typescript.core.internal.TypeScriptBridge;

public class TSBridgeTest {

	public static class TestableTypeScriptBridge extends TypeScriptBridge {
		
		public TestableTypeScriptBridge(final File directory) {
			super(directory);
		}
		
		// change visibility of method
		@Override
		public TSocket openBridgeSocket(String hostname, int port, long timeoutInMilliseconds)
				throws TTransportException {
			return super.openBridgeSocket(hostname, port, timeoutInMilliseconds);
		}
		
	}
	
	/**
	 * This test does not work unless the logLevel is aquired through static methods
	 * in constructor of TypeScriptBridge.
	 * 
	 * @throws Exception
	 */
	@Ignore 
	@Test
	public void testOpenSocketFails() throws Exception {
		
		final TestableTypeScriptBridge bridge = new TestableTypeScriptBridge(
				File.createTempFile("typescript-plugin_", "_testdirectory"));
		
		try {
			bridge.openBridgeSocket("localhost", 4711, 1000);
			Assert.fail("openBridgeSocket should throw an exception!");
		} catch (TTransportException e) {
			// everthing is fine
		}
		
	}
	
	/**
	 * This test does not work unless the logLevel is aquired through static methods
	 * in constructor of TypeScriptBridge.
	 * 
	 * @throws Exception
	 */
	@Ignore 
	@Test
	public void testOpenSocketSucceeds() throws Exception {
		
		final TestableTypeScriptBridge bridge = new TestableTypeScriptBridge(
				File.createTempFile("typescript-plugin_", "_testdirectory"));
		
		final ServerSocket server = new ServerSocket(4711);
		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					server.accept();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		thread.start();
		
		try {
			bridge.openBridgeSocket("localhost", 4711, 1000);
		} catch (TTransportException e) {
			e.printStackTrace();
			Assert.fail("openBridgeSocket must not throw an exception");
		} finally {
			
			try {
				server.close();
			} catch (Throwable e) {
				// never mind
			}
			
		}
		
	}

}
