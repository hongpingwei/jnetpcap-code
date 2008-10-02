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
package org.jnetpcap.unix;

import junit.framework.TestCase;

/**
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class TestUnixOs
    extends TestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testTranslateConstant() {
		if (!UnixOs.isSupported()) {
			return;
		}
		assertEquals(0, UnixOs.translateConstant(UnixOs.PROTOCOL_DEFAULT));

		assertEquals(35111, UnixOs.translateConstant(UnixOs.SIOCGIFHWADDR));

		System.out.println("UnixOs.PF_INET=" +  UnixOs.translateConstant(UnixOs.PF_INET));
		System.out.println("UnixOs.SOCK_STREAM=" +  UnixOs.translateConstant(UnixOs.SOCK_STREAM));
		System.out.println("UnixOs.IPPROTO_TCP=" +  UnixOs.translateConstant(UnixOs.IPPROTO_TCP));
	}

	public void testSocket() {
		if (!UnixOs.isSupported() || !UnixOs.isSupported(UnixOs.IPPROTO_TCP)) {
			return;
		}

		int d =
		    UnixOs.socket(UnixOs.PF_INET, UnixOs.SOCK_STREAM,
		        UnixOs.IPPROTO_TCP);
		if (d == -1) {
			fail("socket():" + d + " errno=" + UnixOs.errno());
		}

		UnixOs.close(d);
	}

	public void testIoctlGETHWADDR() {
		if (!UnixOs.isSupported() || !UnixOs.isSupported(UnixOs.IPPROTO_TCP) || !UnixOs.isSupported(UnixOs.SIOCGIFHWADDR)) {
			return;
		}

		int d =
		    UnixOs.socket(UnixOs.PF_INET, UnixOs.SOCK_STREAM, UnixOs.IPPROTO_TCP);
		if (d == -1) {
			fail("socket():" + d + " errno=" + UnixOs.errno());
		}

		IfReq ir = new IfReq();
		assertNotSame("ioctl():-1", -1, UnixOs.ioctl(d, UnixOs.SIOCGIFHWADDR, ir));

		byte[] ha = ir.ifr_hwaddr();
		for (byte b : ha) {
			System.out.printf("%2X:", b);
		}

		System.out.println();

		UnixOs.close(d);

	}
}