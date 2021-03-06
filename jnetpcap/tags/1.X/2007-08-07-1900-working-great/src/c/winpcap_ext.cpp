/***************************************************************************
 * Copyright (C) 2007, Sly Technologies, Inc                               *
 * Distributed under the Lesser GNU Public License  (LGPL)                 *
 ***************************************************************************/

/*
 * Main WinPcap extensions file for jNetPcap.
 */

#include "export.h"

#include <stdio.h>
#include <stdlib.h>
#include <pcap.h>
#include <jni.h>

#ifndef WIN32
#include <errno.h>
#include <string.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <unistd.h>
#else
#include <Win32-Extensions.h>
#endif /*WIN32*/

#include "winpcap_stat_ex.h"
#include "jnetpcap_bpf.h"
#include "winpcap_ext.h"
#include "jnetpcap_utils.h"


jclass winPcapClass = 0;

jmethodID winPcapConstructorMID = 0;

/*
 * Function: testExtensionSupport
 * Description: Tests if WinPcap extensions is available on this platform.
 * Return: JNI_TRUE if yes, otherwise JNI_FALSE
 */
jboolean testExtensionSupport() {
#ifdef WIN32
	return (jboolean)JNI_TRUE;
#else
	return (jboolean)JNI_FALSE;
#endif
}

/*
 * Function: testExtensionSupportAndThrow
 * Description: checks if winpcap ext is supported and throws exception if not.
 * Return: JNI_TRUE if yes, otherwise JNI_FALSE
 */
jboolean testExtensionSupportAndThrow(JNIEnv *env) {

	if (testExtensionSupport() == JNI_FALSE) {
		throwException(env, UNSUPPORTED_OPERATION_EXCEPTION, "");

		return JNI_FALSE;
	} else {
		return JNI_TRUE;
	}
}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    initIDs
 * Signature: ()V
 */
JNIEXPORT void Java_org_jnetpcap_winpcap_WinPcap_initIDs(JNIEnv *env, jclass jclazz) {
	winPcapClass = (jclass) env->NewGlobalRef(jclazz); // This one is easy

	/*
	 * Check if extensions are supported, if not, just quietly exit. Users
	 * must use WinPcap.isSupported() to check if extensions are availabe.
	 * Therefore we must let the WinPcap class finish loading normally, just
	 * left in uninitialized state. All static methods check and throw exception
	 * if not supported and called.
	 */
	if (testExtensionSupport() == JNI_FALSE) {
		return;
	}

	if ( (winPcapConstructorMID = env->GetMethodID(jclazz, "<init>", "()V"))
			== NULL) {
		throwException(env, NO_SUCH_METHOD_EXCEPTION,
				"Unable to initialize constructor WinPcap.WinPcap()");
		return;
	}

}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    isSupported
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_jnetpcap_winpcap_WinPcap_isSupported
(JNIEnv *env , jclass jclazz) {

	return testExtensionSupport();
}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    openDead
 * Signature: (II)Lorg/jnetpcap/winpcap/WinPcap;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_winpcap_WinPcap_openDead
(JNIEnv *env, jclass clazz, jint jlinktype, jint jsnaplen) {

	/*
	 * Make sure extensions are supported, these methods will compile on
	 * non WinPcap based systems, so we rely on exception handling to prevent
	 * people from using these methods.
	 */
	if (testExtensionSupportAndThrow(env) == JNI_FALSE) {
		return NULL; // Exception already thrown
	}

	pcap_t *p = pcap_open_dead(jlinktype, jsnaplen);
	if (p == NULL) {
		return NULL;
	}

	/*
	 * Use a no-arg constructor and initialize 'physical' field using
	 * special JNI priviledges.
	 */
	jobject obj = env->NewObject(clazz, winPcapConstructorMID);
	setPhysical(env, obj, toLong(p));

	return obj;
}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    openLive
 * Signature: (Ljava/lang/String;IIILjava/lang/StringBuilder;)Lorg/jnetpcap/winpcap/WinPcap;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_winpcap_WinPcap_openLive
(JNIEnv *env, jclass clazz, jstring jdevice, jint jsnaplen, jint jpromisc, jint jtimeout,
		jobject jerrbuf) {

	/*
	 * Make sure extensions are supported, these methods will compile on
	 * non WinPcap based systems, so we rely on exception handling to prevent
	 * people from using these methods.
	 */
	if (testExtensionSupportAndThrow(env) == JNI_FALSE) {
		return NULL; // Exception already thrown
	}

	if (jdevice == NULL || jerrbuf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return NULL;
	}

	char errbuf[PCAP_ERRBUF_SIZE];
	errbuf[0] = '\0'; // Reset the buffer;

	const char *device = env->GetStringUTFChars(jdevice, 0);

	//	printf("device=%s snaplen=%d, promisc=%d timeout=%d\n",
	//			device, jsnaplen, jpromisc, jtimeout);

	pcap_t *p = pcap_open_live(device, jsnaplen, jpromisc, jtimeout, errbuf);
	setString(env, jerrbuf, errbuf); // Even if no error, could have warning msg
	if (p == NULL) {
		return NULL;
	}

	/*
	 * Use a no-arg constructor and initialize 'physical' field using
	 * special JNI priviledges.
	 */
	jobject obj = env->NewObject(clazz, winPcapConstructorMID);
	setPhysical(env, obj, toLong(p));

	return obj;
}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    openOffline
 * Signature: (Ljava/lang/String;Ljava/lang/StringBuilder;)Lorg/jnetpcap/winpcap/WinPcap;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_winpcap_WinPcap_openOffline
(JNIEnv *env, jclass clazz, jstring jfname, jobject jerrbuf) {

	/*
	 * Make sure extensions are supported, these methods will compile on
	 * non WinPcap based systems, so we rely on exception handling to prevent
	 * people from using these methods.
	 */
	if (testExtensionSupportAndThrow(env) == JNI_FALSE) {
		return NULL; // Exception already thrown
	}

	if (jfname == NULL || jerrbuf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return NULL;
	}

	char errbuf[PCAP_ERRBUF_SIZE];
	errbuf[0] = '\0'; // Reset the buffer;
	const char *fname = env->GetStringUTFChars(jfname, 0);

	pcap_t *p = pcap_open_offline(fname, errbuf);
	if (p == NULL) {
		setString(env, jerrbuf, errbuf);
		return NULL;
	}

	/*
	 * Use a no-arg constructor and initialize 'physical' field using
	 * special JNI priviledges.
	 */
	jobject obj = env->NewObject(clazz, winPcapConstructorMID);
	setPhysical(env, obj, toLong(p));

	return obj;
}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    setBuff
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_winpcap_WinPcap_setBuff
(JNIEnv *env, jobject obj, jint value) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return pcap_setbuff(p, value);
}
/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    setMode
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_winpcap_WinPcap_setMode
(JNIEnv *env, jobject obj, jint value) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return pcap_setmode(p, value);
}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    setMinToCopy
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_winpcap_WinPcap_setMinToCopy
(JNIEnv *env, jclass obj, jint jminsize) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return pcap_setmintocopy(p, (int)jminsize);
}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    offlineFilter
 * Signature: (Lorg/jnetpcap/PcapBpfProgram;Lorg/jnetpcap/PcapPktHdr;Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_winpcap_WinPcap_offlineFilter
(JNIEnv *env, jclass clazz, jobject jbpf, jobject jhdr, jobject jbuf) {

	/*
	 * Make sure extensions are supported, these methods will compile on
	 * non WinPcap based systems, so we rely on exception handling to prevent
	 * people from using these methods.
	 */
	if (testExtensionSupportAndThrow(env) == JNI_FALSE) {
		return -1; // Exception already thrown
	}

	if (jbpf == NULL || jhdr == NULL || jbuf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	bpf_program *bpf = getBpfProgram(env, jbpf);
	if (bpf == NULL) {
		return -1; // Exception already thrown
	}

	pcap_pkthdr hdr;
	getPktHeader(env, jhdr, &hdr);

	u_char *b = (u_char *)env->GetDirectBufferAddress(jbuf);
	if (b == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_offline_filter (bpf, &hdr, b);
}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    liveDump
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_winpcap_WinPcap_liveDump
(JNIEnv *env, jobject obj, jstring jfname, jint jmaxsize, jint jmaxpackets) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	char *fname = (char *)env->GetStringUTFChars(jfname, 0);
	if (fname == NULL) {
		return -1; // Out of memory
	}

	return pcap_live_dump(p, fname, (int) jmaxsize, (int) jmaxpackets);

}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    liveDumpEnded
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_winpcap_WinPcap_liveDumpEnded
(JNIEnv *env, jobject obj, jint jsync) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return pcap_live_dump_ended(p, (int) jsync);
}

/*
 * Class:     org_jnetpcap_winpcap_WinPcap
 * Method:    statsEx
 * Signature: ()Lorg/jnetpcap/winpcap/PcapStatEx;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_winpcap_WinPcap_statsEx
  (JNIEnv *env, jobject obj) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return NULL; // Exception already thrown
	}

	struct pcap_stat *stats;
	int size = 0;
	stats = (struct pcap_stat *)pcap_stats_ex(p, &size); // Fills the stats structure
	if (stats == NULL) {
		return NULL; // error
	}
	
	jobject jstats = newWinPcapStat(env);
	if (jstats == NULL) {
		return NULL;
	}
	
	struct pcap_stat ps;
	ps.ps_netdrop = 0;

	setWinPcapStat(env, jstats, stats, size);
	
	free(stats); // release the memory

	return jstats;
}
