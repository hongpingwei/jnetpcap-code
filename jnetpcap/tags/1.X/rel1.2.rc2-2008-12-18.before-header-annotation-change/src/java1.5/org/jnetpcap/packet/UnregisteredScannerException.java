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
package org.jnetpcap.packet;

/**
 * Thrown when a header scanner for a protocol does exist but is needed
 * 
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class UnregisteredScannerException
    extends Exception {

	/**
	 * 
	 */
	public UnregisteredScannerException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnregisteredScannerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public UnregisteredScannerException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UnregisteredScannerException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1962136699573367680L;

}
