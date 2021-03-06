/**
 * Copyright (C) 2008 Sly Technologies, Inc. This library is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.jnetpcap.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Properties;
import java.util.concurrent.Exchanger;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnetpcap.util.config.JConfig;

/**
 * Java Logging mechanism. This is a slight extension to JRE's logger that
 * initializes the global logging environmet and adds a couple of convenience
 * methods that are jNetPcap specific. Default jNetPcap properties are loaded as
 * a resource found in CLASSPATH under the resources directory.
 * <p>
 * Note: standard system properties for JRE's logging system:
 * 
 * <pre>
 * &quot;java.util.logging.config.file&quot; - specifies a .properties file
 * &quot;java.util.logging.config.class&quot; - a class configures the LogManager from its 
 *    constructor
 * </pre>
 * 
 * </p>
 * 
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class JLogger
    extends Logger {

	/**
	 * Default resource file with logger configurations
	 */
	public static final String PROPERTIES_CONFIG =
	    "resources/builtin-logger.properties";

	private static boolean triggerConfigInit = true;

	/**
	 * Initialize using our config.
	 */
	static {
		try {
			InputStream in =
			    JLogger.class.getClassLoader().getResourceAsStream(PROPERTIES_CONFIG);
			if (in == null) {
				Logger.getLogger("").severe(
				    "JLogger.static<>: Unable to find builtin-logger.properties. "
				        + "Is resources directory missing in JAR File?");
			} else {
				// TODO: disabled logger properties reload, causing issues for customers
				in.close();
//				LogManager.getLogManager().readConfiguration(in);
			}
		} catch (Exception e) {
			Logger.getLogger("").log(Level.SEVERE,
			    "Unable to find jNetPcap logger.properties", e);
		}
	}

	/**
	 * @param name
	 * @param resourceBundleName
	 */
	public JLogger(String name, String resourceBundleName) {
		super(name, resourceBundleName);
	}

	public static Logger getLogger(Class<?> c) {
		if (triggerConfigInit && c != JConfig.class) {
			/*
			 * This is needed because of the timing. JConfig reloads the logger
			 * properties and causes all prior initialization to be lost. So we
			 * basically trigger the JConfig initialization on the first getLogger
			 * request. If JConfig has already been initialized some other means, it
			 * won't do it again, but since we don't know we call its init method, the
			 * first time we do anyway. JConfig also preserves its own level, so
			 */
			triggerConfigInit = false;
			JConfig.init();
		}

		return getLogger(c.getName());
	}

	public static Logger getLogger(Package p) {
		if (triggerConfigInit) {
			/*
			 * This is needed because of the timing. JConfig reloads the logger
			 * properties and causes all prior initialization to be lost. So we
			 * basically trigger the JConfig initialization on the first getLogger
			 * request. If JConfig has already been initialized some other means, it
			 * won't do it again, but since we don't know we call its init method, the
			 * first time we do anyway. JConfig also preserves its own level, so
			 */
			triggerConfigInit = false;
			JConfig.init();
		}

		return getLogger(p.getName());
	}

	public static LogManager readConfiguration(final Properties properties)
	    throws SecurityException, IOException {

		LogManager man = LogManager.getLogManager();
		
		// TODO: disabled logging reload. Causing issues for customers
		if (true) {
			return man;
		}

		final PipedOutputStream buf = new PipedOutputStream();
		final Exchanger<IOException> io = new Exchanger<IOException>();
		Thread worker = new Thread(new Runnable() {

			public void run() {
				IOException error = null;
				try {
					properties.store(buf, "");
					buf.close();
				} catch (IOException e) {
					error = e;
				}

				try {
					io.exchange(error);
				} catch (InterruptedException e1) {
				}

			}

		}, "property writer");

		worker.start();

		man.readConfiguration(new PipedInputStream(buf));

		try {
			IOException e = io.exchange(null);
			if (e != null) {
				throw e; // Rethrow original exception
			}
		} catch (InterruptedException e) {
		}

		return man;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.logging.Logger#setLevel(java.util.logging.Level)
	 */
	@Override
	public void setLevel(Level newLevel) throws SecurityException {
		super.setLevel(newLevel);

		if (triggerConfigInit) {
			triggerConfigInit = false;
			JConfig.init();
		}
	}

}
