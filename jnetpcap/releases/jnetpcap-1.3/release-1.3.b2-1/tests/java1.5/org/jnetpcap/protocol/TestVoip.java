/**
 * Copyright (C) 2009 Sly Technologies, Inc. This library is free software; you
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
package org.jnetpcap.protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.TestUtils;
import org.jnetpcap.protocol.voip.Rtp;
import org.jnetpcap.protocol.voip.Sdp;
import org.jnetpcap.protocol.voip.Sip;

/**
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class TestVoip
    extends
    TestUtils {

	private static final String SIP = "tests/test-sip-rtp.pcap";

	private static final String SIP_G711 = "tests/test-sip-rtp-g711.pcap";

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

	public void testSip() {
		Sip sip = new Sip();
		Sdp sdp = new Sdp();
		PcapPacket packet = super.getPcapPacket(SIP, 223 - 1);
		if (packet.hasHeader(sip)) {
			System.out.printf("%s", sip);

			if (packet.hasHeader(sdp)) {
				System.out.printf("%s", sdp);

			}
		} else {
			System.out.printf(packet.toString());
		}
	}

	public void testRtpHeuristics() {

		JPacket packet = super.getPcapPacket(SIP_G711, 499 - 1);

		// System.out.println(JRegistry.toDebugString());
		System.out.println(packet.getState().toDebugString());
		System.out.println(packet);
		System.out.flush();

		assertNotNull(packet);
		assertTrue(packet.hasHeader(JProtocol.RTP_ID));
	}

	public void SKIP_testRtpAudioExtract() throws IOException {
		Rtp rtp = new Rtp();

		try {
			for (PcapPacket packet : super.getIterable(SIP_G711)) {
				assertNotNull(packet);
				if (packet.hasHeader(rtp)) {

					if (rtp.hasPostfix() || rtp.paddingLength() != 0) {
						System.out.println(rtp);
					}

					FileOutputStream out = getOutput(rtp.ssrc());

					out.write(rtp.getPayload());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (FileOutputStream o : map.values()) {
			o.close();
		}
	}

	Map<Long, FileOutputStream> map = new HashMap<Long, FileOutputStream>();

	private FileOutputStream getOutput(long id) throws FileNotFoundException {
		if (map.containsKey(id)) {
			return map.get(id);
		} else {
			File file = new File("C:\\temp\\" + id + ".au");
			if (file.exists()) {
				file.delete();
			}

			FileOutputStream out = new FileOutputStream(file);
			map.put(id, out);

			return out;
		}
	}
}
