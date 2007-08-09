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
package org.jnetpcap.winpcap;

import java.nio.ByteBuffer;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapExtensionNotAvailableException;
import org.jnetpcap.PcapHandler;
import org.jnetpcap.PcapIf;
import org.jnetpcap.PcapPktHdr;

/**
 * WinPcap specific extensions to libpcap library. To access WinPcap extensions,
 * you must use WinPcap class and its methods. <code>WinPcap</code> class
 * extends Pcap class so you have all of the typeical <code>Pcap</code> class
 * functionality. WinPcap provides many additional methods which are only
 * available on platforms what support WinPcap. First you must use static
 * <code>WinPcap.isSupported()</code> method call which will return a boolean
 * that will indicate if WinPcap extensions are supported on this particular
 * platform. If you try and use any method in this class when WinPcap extensions
 * are not supported, another words <code>WinPcap.isSupport()</code> returned
 * false, every method in this calls will throw
 * <code>UnsupportOperationException</code>.
 * 
 * @see Pcap
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class WinPcap
    extends Pcap {

	/**
	 * default capture mode
	 */
	public static final int MODE_CAPT = 0;

	/**
	 * statistical mode
	 */
	public static final int MODE_STAT = 1;

	/**
	 * Flag used with {@link #sendQueueTransmit(WinPcapSendQueue, int)}, to tell
	 * kernel to send packets as fast as possible, without synchronizing with
	 * packet timestamps found in headers.
	 */
	public static final int TRANSMIT_SYNCH_ASAP = 0;

	/**
	 * Flag used with {@link #sendQueueTransmit(WinPcapSendQueue, int)}, to tell
	 * kernel to send packets at the rate that is determined by the timestamp with
	 * in the sendqueue. The transmittion is synchronized with timestamps.
	 */
	public static final int TRANSMIT_SYNCH_USE_TIMESTAMP = 1;

	/**
	 * Internal representation of the type of source in use (file, remote/local
	 * interface).
	 */
	public final static int SRC_FILE = 2;

	/**
	 * Internal representation of the type of source in use (file, remote/local
	 * interface).
	 */
	public final static int SRC_IFLOCAL = 3;

	/**
	 * Internal representation of the type of source in use (file, remote/local
	 * interface).
	 */
	public final static int SRC_IFREMOTE = 4;

	static {
		initIDs();

		// Make sure some dependency classes get loaded
		try {
			Class.forName("org.jnetpcap.winpcap.WinPcapStat");
			Class.forName("org.jnetpcap.winpcap.WinPcapSamp");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Unable to find class: ", e);
		}
	}

	/**
	 * Create a list of network devices that can be opened with pcap_open().
	 * </p>
	 * <p>
	 * This function is a superset of the old 'pcap_findalldevs()', which is
	 * obsolete, and which allows listing only the devices present on the local
	 * machine. Vice versa, pcap_findalldevs_ex() allows listing the devices
	 * present on a remote machine as well. Additionally, it can list all the pcap
	 * files available into a given folder. Moreover, pcap_findalldevs_ex() is
	 * platform independent, since it relies on the standard pcap_findalldevs() to
	 * get addresses on the local machine.
	 * </p>
	 * <p>
	 * In case the function has to list the interfaces on a remote machine, it
	 * opens a new control connection toward that machine, it retrieves the
	 * interfaces, and it drops the connection. However, if this function detects
	 * that the remote machine is in 'active' mode, the connection is not dropped
	 * and the existing socket is used.
	 * </p>
	 * <p>
	 * The 'source' is a parameter that tells the function where the lookup has to
	 * be done and it uses the same syntax of the pcap_open().
	 * </p>
	 * <p>
	 * Differently from the pcap_findalldevs(), the interface names (pointed by
	 * the alldevs->name and the other ones in the linked list) are already ready
	 * to be used in the pcap_open() call. Vice versa, the output that comes from
	 * pcap_findalldevs() must be formatted with the new pcap_createsrcstr()
	 * before passing the source identifier to the pcap_open().
	 * </p>
	 * <p>
	 * The error message is returned in the 'errbuf' variable. An error could be
	 * due to several reasons:
	 * <ul>
	 * <li>libpcap/WinPcap was not installed on the local/remote host
	 * <li>the user does not have enough privileges to list the devices / files
	 * <li> a network problem
	 * <li> the RPCAP version negotiation failed
	 * <li> other errors (not enough memory and others).
	 * </ul>
	 * </p>
	 * <p>
	 * Warning:<br>
	 * There may be network devices that cannot be opened with pcap_open() by the
	 * process calling pcap_findalldevs(), because, for example, that process
	 * might not have sufficient privileges to open them for capturing; if so,
	 * those devices will not appear on the list. The interface list must be
	 * deallocated manually by using the pcap_freealldevs().
	 * </p>
	 * 
	 * @param source
	 *          a char* buffer that keeps the 'source localtion', according to the
	 *          new WinPcap syntax. This source will be examined looking for
	 *          adapters (local or remote) (e.g. source can be 'rpcap://' for
	 *          local adapters or 'rpcap://host:port' for adapters on a remote
	 *          host) or pcap files (e.g. source can be 'file://c:/myfolder/').
	 *          The strings that must be prepended to the 'source' in order to
	 *          define if we want local/remote adapters or files is defined in the
	 *          new Source Specification Syntax
	 * @param auth
	 *          a pointer to a pcap_rmtauth structure. This pointer keeps the
	 *          information required to authenticate the RPCAP connection to the
	 *          remote host. This parameter is not meaningful in case of a query
	 *          to the local host: in that case it can be NULL.
	 * @param alldevs
	 *          a list of all the network interfaces
	 * @param errbuf
	 *          error message (in case there is one)
	 * @return '0' if everything is fine, '-1' if some errors occurred; this
	 *         function returns '-1' also in case the system does not have any
	 *         interface to list
	 */
	public native static int findAllDevsEx(String source, WinPcapRmtAuth auth,
	    List<PcapIf> alldevs, StringBuilder errbuf);

	/**
	 * Initialize JNI method, field and class IDs.
	 */
	private static native void initIDs();

	/**
	 * Checks if WinPcap extensions are available on this platform.
	 * 
	 * @return true means WinPcap extensions are available and loaded, otherwise
	 *         false
	 */
	public static native boolean isSupported();

	/**
	 * Returns if a given filter applies to an offline packet. This function is
	 * used to apply a filter to a packet that is currently in memory. This
	 * process does not need to open an adapter; we need just to create the proper
	 * filter (by settings parameters like the snapshot length, or the link-layer
	 * type) by means of the pcap_compile_nopcap(). The current API of libpcap
	 * does not allow to receive a packet and to filter the packet after it has
	 * been received. However, this can be useful in case you want to filter
	 * packets in the application, instead of into the receiving process. This
	 * function allows you to do the job.
	 * 
	 * @param program
	 *          bpf filter
	 * @param header
	 *          packets header
	 * @param buf
	 *          buffer containing packet data
	 * @return snaplen of the packet or 0 if packet should be rejected
	 */
	public static native int offlineFilter(PcapBpfProgram program,
	    PcapPktHdr header, ByteBuffer buf);

	/**
	 * Open a generic source in order to capture/send (WinPcap only) traffic. The
	 * <code>open</code> replaces all the <code>openXxx()</code> methods with
	 * a single call. This method hides the differences between the different
	 * <code>openXxx()</code> methods so that the programmer does not have to
	 * manage different opening function. In this way, the 'true' open method is
	 * decided according to the source type, which is included into the source
	 * string (in the form of source prefix). This function can rely on the
	 * <code>createSrcStr</code> to create the string that keeps the capture
	 * device according to the new syntax, and the <code>parseSrcStr</code> for
	 * the other way round.
	 * <p>
	 * <b>Warning:</b> The source cannot be larger than PCAP_BUF_SIZE. The
	 * following formats are not allowed as 'source' strings:
	 * <ul>
	 * <li>rpcap:// [to open the first local adapter]
	 * <li>rpcap://hostname/ [to open the first remote adapter]
	 * </ul>
	 * 
	 * @param source
	 *          The source name has to include the format prefix according to the
	 *          new Source Specification Syntax and it cannot be NULL. <br>
	 *          On on Linux systems with 2.2 or later kernels, a device argument
	 *          of "any" (i.e. rpcap://any) can be used to capture packets from
	 *          all interfaces. In order to makes the source syntax easier, please
	 *          remember that:
	 *          <ul>
	 *          <li>the adapters returned by the pcap_findalldevs_ex() can be
	 *          used immediately by the pcap_open()
	 *          <li> in case the user wants to pass its own source string to the
	 *          pcap_open(), the pcap_createsrcstr() helps in creating the correct
	 *          source identifier.
	 *          </ul>
	 * @param snaplen
	 *          length of the packet that has to be retained. For each packet
	 *          received by the filter, only the first 'snaplen' bytes are stored
	 *          in the buffer and passed to the user application. For instance,
	 *          snaplen equal to 100 means that only the first 100 bytes of each
	 *          packet are stored.
	 * @param flags
	 *          keeps several flags that can be needed for capturing packet
	 * @param timeout
	 *          read timeout in milliseconds. The read timeout is used to arrange
	 *          that the read not necessarily return immediately when a packet is
	 *          seen, but that it waits for some amount of time to allow more
	 *          packets to arrive and to read multiple packets from the OS kernel
	 *          in one operation. Not all platforms support a read timeout; on
	 *          platforms that don't, the read timeout is ignored.
	 * @param auth
	 *          a pointer to a 'struct pcap_rmtauth' that keeps the information
	 *          required to authenticate the user on a remote machine. In case
	 *          this is not a remote capture, this pointer can be set to NULL.
	 * @param errbuf
	 *          a pointer to a user-allocated buffer which will contain the error
	 *          in case this function fails. The pcap_open() and findalldevs() are
	 *          the only two functions which have this parameter, since they do
	 *          not have (yet) a pointer to a pcap_t structure, which reserves
	 *          space for the error string. Since these functions do not have
	 *          (yet) a pcap_t pointer (the pcap_t pointer is NULL in case of
	 *          errors), they need an explicit 'errbuf' variable. 'errbuf' may
	 *          also be set to warning text when pcap_open_live() succeds; to
	 *          detect this case the caller should store a zero-length string in
	 *          'errbuf' before calling pcap_open_live() and display the warning
	 *          to the user if 'errbuf' is no longer a zero-length string.
	 * @return in case of problems, it returns null and the 'errbuf' variable
	 *         keeps the error message.
	 */
	public native static WinPcap open(String source, int snaplen, int flags,
	    int timeout, WinPcapRmtAuth auth, StringBuilder errbuf);

	/**
	 * Accept a set of strings (host name, port, ...), and it returns the complete
	 * source string according to the new format (e.g. 'rpcap://1.2.3.4/eth0').
	 * This function is provided in order to help the user creating the source
	 * string according to the new format. An unique source string is used in
	 * order to make easy for old applications to use the remote facilities. Think
	 * about tcpdump, for example, which has only one way to specify the interface
	 * on which the capture has to be started. However, GUI-based programs can
	 * find more useful to specify hostname, port and interface name separately.
	 * In that case, they can use this function to create the source string before
	 * passing it to the pcap_open() function.
	 * 
	 * @param source
	 *          will contain the complete source string wen the function returns
	 * @param type
	 *          its value tells the type of the source we want to created
	 * @param host
	 *          an user-allocated buffer that keeps the host (e.g. "foo.bar.com")
	 *          we want to connect to. It can be NULL in case we want to open an
	 *          interface on a local host
	 * @param port
	 *          an user-allocated buffer that keeps the network port (e.g. "2002")
	 *          we want to use for the RPCAP protocol. It can be NULL in case we
	 *          want to open an interface on a local host.
	 * @param name
	 *          an user-allocated buffer that keeps the interface name we want to
	 *          use (e.g. "eth0"). It can be NULL in case the return string (i.e.
	 *          'source') has to be used with the pcap_findalldevs_ex(), which
	 *          does not require the interface name.
	 * @param errbuf
	 *          buffer that will contain the error message (in case there is one).
	 * @return '0' if everything is fine, '-1' if some errors occurred. The string
	 *         containing the complete source is returned in the 'source'
	 *         variable.
	 */
	public native static int createSrcStr(StringBuilder source, int type,
	    String host, String port, String name, StringBuilder errbuf);

	/**
	 * Create a pcap_t structure without starting a capture. pcap_open_dead() is
	 * used for creating a pcap_t structure to use when calling the other
	 * functions in libpcap. It is typically used when just using libpcap for
	 * compiling BPF code.
	 * 
	 * @see Pcap#openDead(int, int)
	 * @param linktype
	 *          pcap DLT link type integer value
	 * @param snaplen
	 *          filters generated using the pcap structure will truncate captured
	 *          packets to this length
	 * @return WinPcap structure that can only be used to generate filter code and
	 *         none of its other capture methods should be called or null if error
	 *         occured
	 */
	public native static WinPcap openDead(int linktype, int snaplen);

	/**
	 * <p>
	 * This method, overrides the generic libpcap based <code>openLive</code>
	 * method, and allocates a peer pcap object that allows WinPcap extensions.
	 * </p>
	 * <p>
	 * Open a live capture associated with the specified network interface device.
	 * pcap_open_live() is used to obtain a packet capture descriptor to look at
	 * packets on the network. device is a string that specifies the network
	 * device to open; on Linux systems with 2.2 or later kernels, a device
	 * argument of "any" or NULL can be used to capture packets from all
	 * interfaces. snaplen specifies the maximum number of bytes to capture. If
	 * this value is less than the size of a packet that is captured, only the
	 * first snaplen bytes of that packet will be captured and provided as packet
	 * data. A value of 65535 should be sufficient, on most if not all networks,
	 * to capture all the data available from the packet. promisc specifies if the
	 * interface is to be put into promiscuous mode. (Note that even if this
	 * parameter is false, the interface could well be in promiscuous mode for
	 * some other reason.)
	 * </p>
	 * <p>
	 * For now, this doesn't work on the "any" device; if an argument of "any" or
	 * NULL is supplied, the promisc flag is ignored. to_ms specifies the read
	 * timeout in milliseconds. The read timeout is used to arrange that the read
	 * not necessarily return immediately when a packet is seen, but that it wait
	 * for some amount of time to allow more packets to arrive and to read
	 * multiple packets from the OS kernel in one operation. Not all platforms
	 * support a read timeout; on platforms that don't, the read timeout is
	 * ignored. A zero value for to_ms, on platforms that support a read timeout,
	 * will cause a read to wait forever to allow enough packets to arrive, with
	 * no timeout. errbuf is used to return error or warning text. It will be set
	 * to error text when pcap_open_live() fails and returns NULL. errbuf may also
	 * be set to warning text when pcap_open_live() succeds; to detect this case
	 * the caller should store a zero-length string in errbuf before calling
	 * pcap_open_live() and display the warning to the user if errbuf is no longer
	 * a zero-length string.
	 * </p>
	 * <p>
	 * <b>Special note about <code>snaplen</code> argument.</b> The behaviour
	 * of this argument may be suprizing to some. The <code>argument</code> is
	 * only applied when there is a filter set using <code>setFilter</code>
	 * method after the <code>openLive</code> call. Otherwise snaplen, even non
	 * zero is ignored. This is the behavior of all BSD systems utilizing BPF and
	 * WinPcap. This may change in the future, but that is the current behavior.
	 * (For more detailed explanation and discussion please see jNetPcap website
	 * and its FAQs.)
	 * </p>
	 * 
	 * @see Pcap#openLive(String, int, int, int, StringBuilder)
	 * @param device
	 *          buffer containing a C, '\0' terminated string with the the name of
	 *          the device
	 * @param snaplen
	 *          amount of data to capture per packet; (see special note in doc
	 *          comments about when this argument is ignored even when non-zero)
	 * @param promisc
	 *          1 means open in promiscious mode, a 0 means non-propmiscous
	 * @param timeout
	 *          timeout in ms
	 * @param errbuf
	 *          a buffer that will contain any error messages if the call to open
	 *          failed
	 * @return a raw structure the data of <code>pcap_t</code> C structure as
	 *         returned by native libpcap call to open
	 */
	public native static WinPcap openLive(String device, int snaplen,
	    int promisc, int timeout, StringBuilder errbuf);

	/**
	 * Open a savefile in the tcpdump/libpcap format to read packets.
	 * pcap_open_offline() is called to open a "savefile" for reading. fname
	 * specifies the name of the file to open. The file has the same format as
	 * those used by tcpdump(1) and tcpslice(1). The name "-" in a synonym for
	 * stdin. Alternatively, you may call pcap_fopen_offline() to read dumped data
	 * from an existing open stream fp. Note that on Windows, that stream should
	 * be opened in binary mode. errbuf is used to return error text and is only
	 * set when pcap_open_offline() or pcap_fopen_offline() fails and returns
	 * NULL.
	 * 
	 * @see Pcap#openOffline(String, StringBuilder)
	 * @param fname
	 *          filename of the pcap file
	 * @param errbuf
	 *          any error messages in UTC8 encoding
	 * @return WinPcap structure or null if error occured
	 */
	public native static WinPcap openOffline(String fname, StringBuilder errbuf);

	/**
	 * Allocate a send queue. This method allocats a send queue, i.e. a buffer
	 * containing a set of raw packets that will be transmittted on the network
	 * with {@link #sendQueueTransmit} method.
	 * 
	 * @see #sendQueueTransmit(WinPcapSendQueue, int)
	 * @param size
	 *          size of the sendqueue to allocate
	 * @return allocated sendqueue
	 */
	public static WinPcapSendQueue sendQueueAlloc(int size) {

		if (isSupported() == false) {
			throw new PcapExtensionNotAvailableException();
		}

		return new WinPcapSendQueue(size);
	}

	/**
	 * Destroy a send queue. Deletes a send queue and frees all the memory
	 * associated with it.
	 * 
	 * @param queue
	 *          the queue to free up
	 */
	public static void sendQueueDestroy(WinPcapSendQueue queue) {

		if (isSupported() == false) {
			throw new PcapExtensionNotAvailableException();
		}

		// Nothing to do, queue java allocated
	}

	/**
	 * Set the minumum amount of data received by the kernel in a single call.
	 * pcap_setmintocopy() changes the minimum amount of data in the kernel buffer
	 * that causes a read from the application to return (unless the timeout
	 * expires). If the value of size is large, the kernel is forced to wait the
	 * arrival of several packets before copying the data to the user. This
	 * guarantees a low number of system calls, i.e. low processor usage, and is a
	 * good setting for applications like packet-sniffers and protocol analyzers.
	 * Vice versa, in presence of a small value for this variable, the kernel will
	 * copy the packets as soon as the application is ready to receive them. This
	 * is useful for real time applications that need the best responsiveness from
	 * the kernel.
	 * 
	 * @see #openLive(String, int, int, int, StringBuilder)
	 * @see #loop(int, PcapHandler, Object)
	 * @see #dispatch(int, PcapHandler, Object)
	 * @param size
	 *          minimum size
	 * @return the return value is 0 when the call succeeds, -1 otherwise
	 */
	public static native int setMinToCopy(int size);

	/**
	 * WinPcap objects make no sense unless they have been allocated from JNI
	 * space and the physical address field has been set in super.physical.
	 */
	private WinPcap() {
		super();
	}

	/**
	 * dumps the network traffic from an interface to a file. Using this function
	 * the dump is performed at kernel level, therefore it is more efficient than
	 * using Pcap.dump(). The parameters of this function are an interface
	 * descriptor (obtained with openLive()), a string with the name of the dump
	 * file, the maximum size of the file (in bytes) and the maximum number of
	 * packets that the file will contain. Setting maxsize or maxpacks to 0 means
	 * no limit. When maxsize or maxpacks are reached, the dump ends. liveDump()
	 * is non-blocking, threfore Return immediately. liveDumpEnded() can be used
	 * to check the status of the dump process or to wait until it is finished.
	 * Pcap.close() can instead be used to end the dump process. Note that when
	 * one of the two limits is reached, the dump is stopped, but the file remains
	 * opened. In order to correctly flush the data and put the file in a
	 * consistent state, the adapter must be closed with Pcap.close().
	 * 
	 * @param fname
	 *          file name
	 * @param maxsize
	 *          maximum file size
	 * @param maxpackets
	 *          maximum number of packets to store
	 * @return 0 on success otherwise -1
	 */
	public native int liveDump(String fname, int maxsize, int maxpackets);

	/**
	 * Return the status of the kernel dump process, i.e. tells if one of the
	 * limits defined with pcap_live_dump() has been reached.
	 * pcap_live_dump_ended() informs the user about the limits that were set with
	 * a previous call to pcap_live_dump() on the interface pointed by p: if the
	 * return value is nonzero, one of the limits has been reched and the dump
	 * process is currently stopped. If sync is nonzero, the function blocks until
	 * the dump is finished, otherwise Return immediately. Warning: if the dump
	 * process has no limits (i.e. if the maxsize and maxpacks arguments of
	 * pcap_live_dump() were both 0), the dump process will never stop, therefore
	 * setting sync to TRUE will block the application on this call forever.
	 * 
	 * @param sync
	 *          if sync is nonzero, the function blocks until the dump is
	 *          finished, otherwise returns immediately
	 * @return non zero value means that dump process has finished, a zero means
	 *         its still in progress
	 */
	public native int liveDumpEnded(int sync);

	/**
	 * Send a queue of raw packets to the network. This function transmits the
	 * content of a queue to the wire. p is a pointer to the adapter on which the
	 * packets will be sent, queue points to a pcap_send_queue structure
	 * containing the packets to send (see pcap_sendqueue_alloc() and
	 * pcap_sendqueue_queue()), sync determines if the send operation must be
	 * synchronized: if it is non-zero, the packets are sent respecting the
	 * timestamps, otherwise they are sent as fast as possible. The return value
	 * is the amount of bytes actually sent. If it is smaller than the size
	 * parameter, an error occurred during the send. The error can be caused by a
	 * driver/adapter problem or by an inconsistent/bogus send queue. Note: Using
	 * this function is more efficient than issuing a series of pcap_sendpacket(),
	 * because the packets are buffered in the kernel driver, so the number of
	 * context switches is reduced. Therefore, expect a better throughput when
	 * using pcap_sendqueue_transmit. When Sync is set to TRUE, the packets are
	 * synchronized in the kernel with a high precision timestamp. This requires a
	 * non-negligible amount of CPU, but allows normally to send the packets with
	 * a precision of some microseconds (depending on the accuracy of the
	 * performance counter of the machine). Such a precision cannot be reached
	 * sending the packets with pcap_sendpacket().
	 * 
	 * @param queue
	 *          queue containing the data to be sent
	 * @param synch
	 *          if it is non-zero, the packets are sent respecting the timestamps,
	 *          otherwise they are sent as fast as possible
	 * @return amount of bytes actually sent; error if less then queues len
	 *         parameter
	 */
	public int sendQueueTransmit(WinPcapSendQueue queue, int synch) {
		checkIsActive(); // Check if Pcap.close wasn't called

		final ByteBuffer buffer = queue.getBuffer();
		final int len = queue.getLen();
		final int maxlen = queue.getMaxLen();

		return sendQueueTransmitPrivate(buffer, len, maxlen, synch);
	}

	/**
	 * Do the actual JNI call. It already checks for isSupported, so no need to do
	 * it again in any wrapper methods.
	 * 
	 * @param buffer
	 *          must be a direct buffer with the data
	 * @param len
	 *          how much data is to be sent from the buffer
	 * @param maxlen
	 *          buffer capacity
	 * @param synch
	 *          if it is non-zero, the packets are sent respecting the timestamps,
	 *          otherwise they are sent as fast as possible
	 * @return amount of bytes actually sent; error if less then len parameter
	 */
	private native int sendQueueTransmitPrivate(ByteBuffer buffer, int len,
	    int maxlen, int synch);

	/**
	 * Set the size of the kernel buffer associated with an adapter. If an old
	 * buffer was already created with a previous call to pcap_setbuff(), it is
	 * deleted and its content is discarded. pcap_open_live() creates a 1 MByte
	 * buffer by default.
	 * 
	 * @see #openLive(String, int, int, int, StringBuilder)
	 * @see #loop(int, PcapHandler, Object)
	 * @see #dispatch(int, PcapHandler, Object)
	 * @param dim
	 *          specifies the size of the buffer in bytes
	 * @return the return value is 0 when the call succeeds, -1 otherwise
	 */
	public native int setBuff(int dim);

	/**
	 * Set the working mode of the interface p to mode. Valid values for mode are
	 * MODE_CAPT (default capture mode) and MODE_STAT (statistical mode).
	 * 
	 * @param mode
	 *          pcap capture mode
	 * @return the return value is 0 when the call succeeds, -1 otherwise
	 */
	public native int setMode(int mode);

	/**
	 * <p>
	 * Define a sampling method for packet capture. This function allows applying
	 * a sampling method to the packet capture process. The currently sampling
	 * methods (and the way to set them) are described into the struct pcap_samp.
	 * In other words, the user must set the appropriate parameters into it; these
	 * will be applied as soon as the capture starts.
	 * </p>
	 * <p>
	 * Warning:<br>
	 * Sampling parameters cannot be changed when a capture is active. These
	 * parameters must be applied before starting the capture. If they are applied
	 * when the capture is in progress, the new settings are ignored. Sampling
	 * works only when capturing data on Win32 or reading from a file. It has not
	 * been implemented on other platforms. Sampling works on remote machines
	 * provided that the probe (i.e. the capturing device) is a Win32 workstation.
	 * </p>
	 * 
	 * @return an object through which you can change the capture algorithm
	 */
	public native WinPcapSamp setSampling();

	/**
	 * This method extends the <code>Pcap.stats</code> method and allows more
	 * statistics to be returned. Note, the signature of this method deviates
	 * slightly from WinPcap implementation due to programming differences of
	 * java. There is no need to deallocate any structures.
	 * 
	 * @see Pcap#stats(org.jnetpcap.PcapStat) return stats structure which is
	 *      filled with statistics or null on error
	 */
	public native WinPcapStat statsEx();
}
