/**
 * Copyright (C) 2007 Sly Technologies, Inc. This library is free software; you
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
package org.jnetpcap;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class TestPcapJNI
    extends TestCase {

	// private final static String device =
	// "\\Device\\NPF_{BC81C4FC-242F-4F1C-9DAD-EA9523CC992D}";

	private final static String win =
	    "\\Device\\NPF_{BC81C4FC-242F-4F1C-9DAD-EA9523CC992D}";

	private final static String linux = "any";

	private final static boolean isWindows =
	    "Windows XP".equals(System.getProperty("os.name"));

	private final static String device = (isWindows) ? win : linux;

	private final static String fname = "tests/test-l2tp.pcap";

	private static final int OK = 0;

	private static final int snaplen = 64 * 1024;

	private static final int promisc = 1;

	private static final int oneSecond = 1000;

	/**
	 * Will generate HTTP traffic to a website. Use start() to start in a test
	 * method, and always put stop() in tearDown. Safe to call stop even when
	 * never started.
	 */
	private static final HttpTrafficGenerator gen = new HttpTrafficGenerator();

	private static File tmpFile;

	static {
		try {
			tmpFile = File.createTempFile("temp-", "-TestPcapJNI");
		} catch (IOException e) {
			tmpFile = null;
			System.err.println("Unable to initialize a temporary file");
		}

	}

	/**
	 * Command line launcher to run the jUnit tests cases in this test class.
	 * 
	 * @param args
	 *          -h for help
	 */
	public static void main(String[] args) {
		if (args.length == 1 && "-h".equals(args[0])) {
			System.out
			    .println("Usage: java -jar jnetpcap.jar [-h]\n"
			        + "  -h  This help message\n"
			        + "   (No other command line options are supported.)\n"
			        + "----------------------------------------------------------------\n\n"
			        + "The 'main' method invoked here, runs several dozen jUnit tests\n"
			        + "which test the functionality of this jNetPcap library.\n"
			        + "The tests are actual excersizes using native libpcap\n"
			        + "library linked with 'jnetpcap.dll' or 'libjnetpcap.so' on\n"
			        + "unix systems.\n\n"
			        + "If you are having trouble linking the native library and get\n"
			        + "'UnsatisfiedLinkError', which means java is not finding the\n"
			        + "library, here are a few pointers:\n\n"
			        + "Java's native library loader DOES NOT USE CLASSPATH variable\n"
			        + "to locate native libraries. Each operating system uses different\n"
			        + "algorithm to locate files, as described below. You can always\n"
			        + "force java to look for native library with Java VM command\n"
			        + "line option 'java -Djava.library.path=lib' where lib is\n"
			        + "a directory where 'jnetpcap.dll' or 'libjnetpcap.so' resides\n"
			        + "relative to the installation directory of jNetStream package.\n"
			        + "Or replace lib with the directory where you have installed the\n"
			        + "library.\n\n"
			        + "On Win32 systems:\n"
			        + "  Windows systems use /windows and /windows/system32 folder\n"
			        + "  to search for jnetpcap.dll. Also the 'PATH' variable, the same\n"
			        + "  one used to specify executable commands, is used as well.\n\n"
			        + "On Unix systems:\n"
			        + "  All unix systems use the standard 'LD_LIBRARY_PATH' variable.\n\n"
			        + "Of course as mentioned earlier, to override this behaviour use\n"
			        + "the '-Djava.library.path=' directory, to force java to look in\n"
			        + "that particular directory. Do not set the path which includes the\n"
			        + "name of the library itself, just the directory to search in.\n\n"
			        + "Final note, native librariers can not be loaded from jar files.\n"
			        + "You have to extract it to a physical directory if you want java to\n"
			        + "load it. This was done purposely by Sun for security reasons.");

			return;
		}

		TestRunner.main(new String[] { "org.jnetpcap.TestPcapJNI" });

	}

	private StringBuilder errbuf;

	private final PcapHandler<?> doNothingHandler = new PcapHandler<Object>() {

		public void nextPacket(Object userObject, long seconds, int useconds,
		    int caplen, int len, ByteBuffer buffer) {
			// Do nothing handler
		}
	};

	/**
	 * @throws java.lang.Exception
	 */
	protected void setUp() throws Exception {

		errbuf = new StringBuilder();

		if (tmpFile.exists()) {
			assertTrue(tmpFile.delete());
		}

	}

	/**
	 * Test disabled, as it requires live packets to capture. To enable the test
	 * just rename the method, by removing the prefix SKIP. Then make sure there
	 * are live packets to be captured.
	 */
	public void SKIPtestOpenLiveAndDispatch() {

		Pcap pcap = Pcap.openLive(device, 10000, 1, 60 * 1000, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		PcapHandler<String> handler = new PcapHandler<String>() {

			public void nextPacket(String user, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				// System.out.printf("%s, ts=%s caplen=%d len=%d capacity=%d\n", user
				// .toString(), new Date(seconds * 1000).toString(), caplen, len,
				// buffer.capacity());

			}

		};

		pcap.dispatch(10, handler, "Hello");

		pcap.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	protected void tearDown() throws Exception {
		errbuf = null;

		/*
		 * Stop the traffic generator, even when not running need to call to make
		 * sure its not running.
		 */
		gen.stop();

		if (tmpFile != null && tmpFile.exists()) {
			tmpFile.delete();
		}
	}

	public void testCompileNoPcapNullPtrHandling() {
		try {
			Pcap.compileNoPcap(1, 1, null, null, 1, 1);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		}
	}

	public void testCompileNullPtrHandling() {
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		try {
			pcap.compile(null, null, 1, 0);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		} finally {
			pcap.close();
		}
	}

	public void testDataLinkNameToValNullPtrHandling() {
		try {
			Pcap.datalinkNameToVal(null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		}
	}

	public void testPcapClosedExceptionHandling() {
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		pcap.close();

		try {
			pcap.breakloop();
			fail("Expected PcapClosedException");
		} catch (PcapClosedException e) {
			// Success
		}
	}

	public void testDatalinkNameToValue() {
		assertEquals(1, Pcap.datalinkNameToVal("EN10MB"));
	}

	public void testDatalinkValueToDescription() {
		assertEquals("Ethernet", Pcap.datalinkValToDescription(1));

	}

	public void testDatalinkValueToName() {
		assertEquals("EN10MB", Pcap.datalinkValToName(1));

	}

	public void testDispatchNullPtrHandling() {
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		try {
			pcap.dispatch(1, null, "");
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		} finally {
			pcap.close();
		}
	}

	public void testErrbuf() throws SocketException, InterruptedException {

		// Test using a bogus device name that's sure to fail
		errbuf.append("def"); // Set dummy message and it should be replaced
		Pcap pcap = Pcap.openLive("abc", 101, 1, 60, errbuf);
		assertNull(pcap);

		assertFalse("Our pre-initialized error message should have been cleared",
		    "def".equals(errbuf.toString()));

		assertTrue("Error buffer should contain an error message",
		    errbuf.length() != 0);
	}

	public void testFilterCompileAndSetFilter() {
		PcapBpfProgram bpf = new PcapBpfProgram();
		String str = "host 192.168.101";

		// System.out.println("trying to compiler the filter() OK\n");
		// System.out.flush();
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		// System.out.println("filter was compiled OK\n"); System.out.flush();
		assertNotNull(errbuf.toString(), pcap);

		int r = pcap.compile(bpf, str, 0, 0);
		assertEquals(pcap.getErr(), 0, r);

		PcapHandler<String> handler = new PcapHandler<String>() {
			public void nextPacket(String user, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				// System.out.printf("%s, ts=%s caplen=%d len=%d capacity=%d\n", user
				// .toString(), new Date(seconds * 1000).toString(), caplen, len,
				// buffer.capacity());
			}
		};

		// System.out.println("trying to set the filter() OK\n");
		// System.out.flush();
		assertEquals(OK, pcap.setFilter(bpf));
		// System.out.println("filter was set OK\n"); System.out.flush();
		assertEquals(OK, pcap.loop(10, handler, str));

		Pcap.freecode(bpf);

		pcap.close();
	}

	public void testFilterCompileNoPcapAndAccessors() {
		PcapBpfProgram bpf = new PcapBpfProgram();

		String str = "host 192.168.1.1";

		int r = Pcap.compileNoPcap(1024, 1, bpf, str, 0, 0);
		assertEquals(OK, r);

		assertEquals(26, bpf.getInstructionCount());
		assertEquals(120259084320L, bpf.getInstruction(10));

		// Boundary checks
		try {
			bpf.getInstruction(-10);
			fail("Failed to generate exception on low index boundary");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}

		// Boundary checks
		try {
			bpf.getInstruction(26);
			fail("Failed to generate exception on upper index boundary");
		} catch (IndexOutOfBoundsException e) {
			// OK
		}

		Pcap.freecode(bpf);
	}

	public void testFindAllDevs() {
		List<PcapIf> devs = new ArrayList<PcapIf>(); // List filled in by
		// findAllDevs

		int r = Pcap.findAllDevs(devs, errbuf);
		assertEquals(errbuf.toString(), 0, r);
		assertFalse(devs.isEmpty());

		// System.out.println(devs);
	}

	public void testFindAllDevsNullPtrHandling() {
		try {
			Pcap.findAllDevs(null, null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		}
	}

	public void testFreeAllDevsNullPtrHandling() {
		try {
			Pcap.freeAllDevs(null, null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		}
	}

	public void testFreeCodeNullPtrHandling() {
		try {
			Pcap.freecode(null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		}
	}

	public void testGetNonBlockNullPtrHandling() {
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		try {
			pcap.getNonBlock(null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		} finally {
			pcap.close();
		}
	}

	public void testLibVersion() {
		assertNotNull(Pcap.libVersion());
	}

	public void testLoopNullPtrHandling() {
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		try {
			pcap.loop(1, null, null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		} finally {
			pcap.close();
		}
	}

	public void testNextExNullPtrHandling() {
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		try {
			pcap.nextEx(null, null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		} finally {
			pcap.close();
		}
	}

	public void testNextNullPtrHandling() {
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		try {
			pcap.next(null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		} finally {
			pcap.close();
		}
	}

	public void testOpenDeadAndClose() {

		Pcap pcap = Pcap.openDead(1, 10000); // DLT, SNAPLEN
		assertNotNull(errbuf.toString(), pcap);

		pcap.close();
	}

	public void testOpenLiveAndDatalinkAndClose() throws SocketException,
	    InterruptedException {

//		System.out.println(System.getProperty("os.name"));
		Pcap pcap = Pcap.openLive(device, 101, 1, 60, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		// Physical field initialized from JNI space
		assertFalse("0".equals(pcap.toString()));

		// Check linklayer 1 is for DLT_EN10MB
		// assertEquals(113, pcap.datalink());

		pcap.close();

		try {
			pcap.close();
			fail();
		} catch (IllegalStateException e) {
			// Expecting this exception on second call to close()
		}
	}

	public void testOpenLiveAndLoopWithBreakloop() {

		Pcap pcap = Pcap.openLive(device, 10000, 1, 60 * 1000, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		PcapHandler<String> handler = new PcapHandler<String>() {

			public void nextPacket(String user, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				// System.out.printf("%s, ts=%s caplen=%d len=%d capacity=%d\n", user
				// .toString(), new Date(seconds * 1000).toString(), caplen, len,
				// buffer.capacity());

			}

		};

		pcap.breakloop(); // Should cause it to exit immediately
		assertEquals(
		    "Error code does not indicate breakloop interrupted the loop when it should have",
		    -2, pcap.loop(10, handler, "Hello"));

		pcap.close();
	}

	public void testOpenOfflineAndClose() {

		Pcap pcap = Pcap.openOffline(fname, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		PcapHandler<String> handler = new PcapHandler<String>() {

			public void nextPacket(String user, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				// System.out.printf("%s, ts=%s caplen=%d len=%d capacity=%d\n", user
				// .toString(), new Date(seconds * 1000).toString(), caplen, len,
				// buffer.capacity());

			}

		};

		assertEquals("Expected to receive exactly 10 packets", 10, pcap.dispatch(
		    10, handler, "Hello"));
		pcap.close();
	}

	public void testOpenOfflineAndLoop() {

		Pcap pcap = Pcap.openOffline(fname, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		PcapHandler<String> handler = new PcapHandler<String>() {

			public void nextPacket(String user, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				// System.out.printf("%s, ts=%s caplen=%d len=%d capacity=%d\n", user
				// .toString(), new Date(seconds * 1000).toString(), caplen, len,
				// buffer.capacity());

			}

		};

		assertEquals(OK, pcap.loop(10, handler, "Hello"));

		pcap.close();
	}

	public void testOpenOfflineAndNext() {

		Pcap pcap = Pcap.openOffline(fname, errbuf);
		assertNotNull(errbuf.toString(), pcap);
		PcapPktHdr hdr = new PcapPktHdr();

		ByteBuffer buffer = pcap.next(hdr);

		assertEquals(114, buffer.capacity()); // length of the packet should match
		assertEquals(114, hdr.getCaplen()); // Should match within the header too
		assertEquals(114, hdr.getLen()); // Should match within the header too

		// System.out.println(new Date(hdr.getSeconds() * 1000).toString());

		pcap.close();
	}

	public void testOpenOfflineAndNextEx() {

		Pcap pcap = Pcap.openOffline(fname, errbuf);
		assertNotNull(errbuf.toString(), pcap);
		PcapPktHdr hdr = new PcapPktHdr();
		PcapPktBuffer buf = new PcapPktBuffer();

		int r = pcap.nextEx(hdr, buf);
		assertEquals(1, r);
		assertNotNull(buf.getBuffer());

		assertEquals(114, buf.getBuffer().capacity()); // length of the packet
		// should match
		assertEquals(114, hdr.getCaplen()); // Should match within the header too
		assertEquals(114, hdr.getLen()); // Should match within the header too

		// System.out.println(new Date(hdr.getSeconds() * 1000).toString());

		pcap.close();
	}

	public void testPcapDLTAndDoNameToValueComparison() {
		int match = 0; // counts how many constants compared OK

		for (PcapDLT c : PcapDLT.values()) {
			int dlt = c.value;
			String libName = Pcap.datalinkValToName(dlt);
			if (libName == null) {
				// System.out.printf("no dlt: dlt=%d enum=%s\n", dlt, c.toString());
				continue;
			}

			if (libName.equals(c.name())) {
				match++;

				// System.out.printf("matched: dlt=%d enum=%s pcap=%s desc=%s\n", dlt, c
				// .toString(), libName, c.description);
			} else {
				// System.out.printf("unmatched: dlt=%d enum=%s pcap=%s desc=%s\n", dlt,
				// c
				// .toString(), libName, c.description);
			}
		}

		// System.out.println("Have " + match + " matches out of "
		// + PcapDLT.values().length);

		assertTrue(
		    "Something is wrong, most constants should match native pcap library",
		    match > 20);

		// for (int dlt = 0; dlt < 100; dlt ++) {
		// String libName = Pcap.datalinkValToName(dlt);
		// PcapDLT c = PcapDLT.valueOf(dlt);
		//			
		// if (c == null && libName != null) {
		// System.out.printf("We don't have dlt=%d pcap=%s\n", dlt, libName);
		// }
		// }
	}

	public void testPcapOpenLiveNullPtrHandling() {
		try {
			Pcap.openLive(null, 1, 1, 1, null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		}
	}

	public void testPcapOpenOfflineNullPtrHandling() {
		try {
			Pcap.openOffline(null, null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		}
	}

	public void testSetAndGetNonblock() {
		Pcap pcap = Pcap.openLive(device, 10000, 1, 60 * 1000, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		assertEquals(OK, pcap.getNonBlock(errbuf));

		pcap.close();
	}

	public void testSetFilterNullPtrHandling() {
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		try {
			pcap.setFilter(null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		} finally {
			pcap.close();
		}
	}

	public void testSetNonBlockNullPtrHandling() {
		Pcap pcap = Pcap.openOffline(fname, errbuf);
		try {
			pcap.setNonBlock(1, null);
			fail("Expected a NULL pointer exception.");
		} catch (NullPointerException e) {
			// OK
		} finally {
			pcap.close();
		}
	}

	/**
	 * <p>
	 * Test case in response to
	 * <code>Bug #1767744 - PcapHandler object ptr error in loop() and dispatch()</code>.
	 * The bug was that PcapHandler jobject ptr in JNI jnetpcap.cpp, was set
	 * incorrectly to jobject of the parent which is the Pcap object itself. The
	 * neccessary method "nextPacket" was looked up correctly using the proper
	 * object but method execution was based on the parent Pcap object not the
	 * PcapHandler object passed in. Therefore Java code when it was setting and
	 * accessing properties within the PcapHandler sub-class, it was actually
	 * clobering data within the Pcap object. Both object's states were terribly
	 * incosinstent, private fields had bogus values, that changed for no reason,
	 * etc... Very easy fix, the jobject for 'jhandler' was substituted for
	 * jobject 'obj' that was used to fix this problem. The problem was both in
	 * dispatch() and loop() methods since they are nearly an identical copy of
	 * each other.
	 * </p>
	 * <p>
	 * To test this we have to create a PcapHandler object set private fields
	 * within it, we'll use an anonymous class, then read in the contents of the
	 * entire contents of a test datafile, while updating the value of the field.
	 * Then at the end we should have consitent value in that private field. Since
	 * the problem seemed complex, but was actually a very easy fix, this should
	 * never really break again, but we will check for it anyhow.
	 * </p>
	 */
	public void testPcapHandlerParentOverrideBugUsingLoop() {

		Pcap pcap = Pcap.openOffline(fname, errbuf);
		if (pcap == null) {
			fail("Unable to open test data file because " + errbuf.toString());
		}
		final Pcap parent = pcap;

		// Tracking variable #1
		final AtomicInteger pcapCount = new AtomicInteger();

		final PcapHandler<?> handler = new PcapHandler<Object>() {

			// Tracking variable #2
			private int count = 0;

			public void nextPacket(Object userObject, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				pcapCount.addAndGet(1);
				count++;

				if (pcapCount.get() != count) {
					parent.breakloop(); // We exit with breakloop which means FAIL
				}
			}
		};

		int r = pcap.loop(Pcap.LOOP_INFINATE, handler, null);
		if (r == Pcap.LOOP_INTERRUPTED) {

			/*
			 * Tracking variables are used to make sure they can sustain their
			 * assigned values. The bug caused fields and object state to be overriden
			 * by object of PcapHandler type.
			 */
			fail("Handler indicates that 2 tracking variables in 2 objects, "
			    + "did not match");
		} else if (r != Pcap.OK) {
			fail("Error occured: " + pcap.getErr());
		}

		pcap.close();
	}

	/**
	 * <p>
	 * Test case in response to
	 * <code>Bug #1767744 - PcapHandler object ptr error in loop() and dispatch()</code>.
	 * The bug was that PcapHandler jobject ptr in JNI jnetpcap.cpp, was set
	 * incorrectly to jobject of the parent which is the Pcap object itself. The
	 * neccessary method "nextPacket" was looked up correctly using the proper
	 * object but method execution was based on the parent Pcap object not the
	 * PcapHandler object passed in. Therefore Java code when it was setting and
	 * accessing properties within the PcapHandler sub-class, it was actually
	 * clobering data within the Pcap object. Both object's states were terribly
	 * incosinstent, private fields had bogus values, that changed for no reason,
	 * etc... Very easy fix, the jobject for 'jhandler' was substituted for
	 * jobject 'obj' that was used to fix this problem. The problem was both in
	 * dispatch() and loop() methods since they are nearly an identical copy of
	 * each other.
	 * </p>
	 * <p>
	 * To test this we have to create a PcapHandler object set private fields
	 * within it, we'll use an anonymous class, then read in the contents of the
	 * entire contents of a test datafile, while updating the value of the field.
	 * Then at the end we should have consitent value in that private field. Since
	 * the problem seemed complex, but was actually a very easy fix, this should
	 * never really break again, but we will check for it anyhow.
	 * </p>
	 */
	public void testPcapHandlerParentOverrideBugUsingDispatch() {

		Pcap pcap = Pcap.openOffline(fname, errbuf);
		if (pcap == null) {
			fail("Unable to open test data file because " + errbuf.toString());
		}
		final Pcap parent = pcap;

		// Tracking variable #1
		final AtomicInteger pcapCount = new AtomicInteger();

		final PcapHandler<?> handler = new PcapHandler<Object>() {

			// Tracking variable #2
			private int count = 0;

			public void nextPacket(Object userObject, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				pcapCount.addAndGet(1);
				count++;

				if (pcapCount.get() != count) {
					parent.breakloop(); // We exit with breakloop which means FAIL
				}
			}
		};

		int r = pcap.dispatch(Pcap.DISPATCH_BUFFER_FULL, handler, null);
		if (r == Pcap.LOOP_INTERRUPTED) {

			/*
			 * Tracking variables are used to make sure they can sustain their
			 * assigned values. The bug caused fields and object state to be overriden
			 * by object of PcapHandler type.
			 */
			fail("Handler indicates that 2 tracking variables in 2 objects, "
			    + "did not match");
		} else if (r != Pcap.OK) {
			fail("Error occured: " + pcap.getErr());
		}

		pcap.close();
	}

	public void testPcapDumperUsingLoop() {

		Pcap pcap = Pcap.openOffline(fname, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		PcapDumper dumper = pcap.dumpOpen(tmpFile.getPath());
		assertNotNull(pcap.getErr(), dumper);

		PcapHandler<PcapDumper> handler = new PcapHandler<PcapDumper>() {

			public void nextPacket(PcapDumper dumper, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				dumper.dump(seconds, useconds, caplen, len, buffer);
			}
		};

		int r = pcap.loop(Pcap.LOOP_INFINATE, handler, dumper);
		assertTrue("Something happened in the loop", r == Pcap.OK);

		dumper.close();
		pcap.close();

		// System.out.printf("%s: tmp=%d, source=%d\n", tmpFile.getName(), tmpFile
		// .length(), new File(fname).length());
		//
		assertEquals("dumped file and source file lengths don't match", tmpFile
		    .length(), new File(fname).length());
	}

	public void testPcapDumperUsingDispatch() {

		Pcap pcap = Pcap.openOffline(fname, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		PcapDumper dumper = pcap.dumpOpen(tmpFile.getPath());
		assertNotNull(pcap.getErr(), dumper);

		PcapHandler<PcapDumper> handler = new PcapHandler<PcapDumper>() {

			public void nextPacket(PcapDumper dumper, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				dumper.dump(seconds, useconds, caplen, len, buffer);
			}
		};

		/*
		 * Our test file is small, about 24K bytes in size, should fit inside a
		 * buffer full.
		 */
		int r = pcap.dispatch(Pcap.DISPATCH_BUFFER_FULL, handler, dumper);
		assertTrue("Something happened in dispatch", r == Pcap.OK);

		dumper.close();
		pcap.close();

		// System.out.printf("%s: tmp=%d, source=%d\n", tmpFile.getName(), tmpFile
		// .length(), new File(fname).length());
		//
		assertEquals("dumped file and source file lengths don't match", tmpFile
		    .length(), new File(fname).length());
	}

	public void testStats() {
		PcapStat stats = new PcapStat();

		Pcap pcap = Pcap.openLive(device, snaplen, promisc, oneSecond, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		pcap.loop(5, doNothingHandler, null);
		pcap.stats(stats);
		// System.out.printf("stats=%s\n", stats.toString());

		pcap.loop(5, doNothingHandler, null);
		pcap.stats(stats);
		// System.out.printf("stats=%s\n", stats.toString());

		pcap.close();
	}

	/**
	 * This is a tricky test that must be disabled by default. We create a dummy
	 * packet all filled with 0xFF for 14 bytes which is the size of ethernet
	 * frame. This should produce a broadcast frame.
	 */
	public void SKIPtestSendPacket() {

		Pcap pcap = Pcap.openLive(device, snaplen, 1, 10 * oneSecond, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		byte[] a = new byte[14];
		Arrays.fill(a, (byte) 0xff);

		ByteBuffer b = ByteBuffer.wrap(a);

		if (pcap.sendPacket(b) != Pcap.OK) {
			fail(pcap.getErr());
		}

		pcap.close();

	}

	public void SKIPtestDumper() {

		gen.start(); // Generate network traffic - async method

		System.out.printf("tmpFile=%s\n", tmpFile.getAbsoluteFile());

		Pcap pcap = Pcap.openLive(device, snaplen, promisc, oneSecond, errbuf);
		assertNotNull(errbuf.toString(), pcap);

		PcapDumper dumper = pcap.dumpOpen(tmpFile.getAbsolutePath());
		assertNotNull(pcap.getErr(), dumper);

		PcapHandler<PcapDumper> dumpHandler = new PcapHandler<PcapDumper>() {

			public void nextPacket(PcapDumper dumper, long seconds, int useconds,
			    int caplen, int len, ByteBuffer buffer) {

				dumper.dump(seconds, useconds, caplen, len, buffer);
			}
		};

		pcap.loop(10, dumpHandler, dumper);

		assertTrue("Empty dump file " + tmpFile.getAbsolutePath(),
		    tmpFile.length() > 0);

		// System.out.printf("Temp dumpfile size=%s\n", tmpFile.length());
		pcap.close();

	}

	public void testLookupDevAndLookupNet() {
		String device = Pcap.lookupDev(errbuf);
		assertNotNull(errbuf.toString(), device);

		PcapInteger netp = new PcapInteger();
		PcapInteger maskp = new PcapInteger();

		int r = Pcap.lookupNet(device, netp, maskp, errbuf);
		assertEquals(errbuf.toString(), 0, r);

//		System.out.printf("device=%s netp=%X maskp=%X errbuf=%s\n", device, netp.getValue(),
//		    maskp.getValue(), errbuf.toString());
	}
}
