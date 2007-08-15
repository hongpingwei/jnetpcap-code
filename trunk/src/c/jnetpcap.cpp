/***************************************************************************
 * Copyright (C) 2007, Sly Technologies, Inc                               *
 * Distributed under the Lesser GNU Public License  (LGPL)                 *
 ***************************************************************************/

/*
 * Small parts of the file were based on C source file written
 * by Patrick Charles and Jonas Lehmann from http://sf.net/projects/jpcap 
 * project. 
 *
 * JNI wrapper around Libpcap and Winpcap. The included header signatures 
 * here were autogenerated using Java JDK supplied "javah" program. The file 
 * contains methods that do the minimum amount neccessary to adapt Libpcap
 * calls and translate them to Java. The few structures that are used by
 * C libpcap implementation have their own Java counter parts with JNI
 * implementations. The JNI implementation of those native methods simply 
 * read and write directly out of the C structures, adapting any objects
 * as neccessary for Java VM environment.
 *
 * This implementation does not utilize instance numbers to reffer to 
 * structures but maintains a structure pointer that is stored within the Java 
 * class as a long field. The implementation uses the stored long value 
 * as a structure pointer which is used to access the original strucutre.
 * As previously stated, any reads or writes using the Java API translate
 * direct reads and writes from the underlying C structure. Care must be
 * taken not to modify any structure, through Java interface, if that is
 * a private structure as returned from Libpcap itself. 
 *
 * The purpose of this type of implementation is to provide all the capabilities
 * of the underlying libpcap library, including any quirks that may 
 * entail.
 *
 * There are no exception thrown from any JNI method defined here. All 
 * error conditions are passed on into the Java environment as originally
 * discovered from libpcap. The only exception thrown is the 
 * IllegalStateException if a serious error has been discovered that prevents
 * the completion of a JNI call. Such as the "physical" address of a 
 * structure stored in Java object is null or invalid.
 * 
 * Other then that, most error codes are simply the
 * integer return value and the message has been written into the user
 * provided byte[] buffer. The strings contained in byte[] buffers need
 * be encoded and decoded using the trivial UTC8 encoding.
 * 
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
#endif /*WIN32*/

/*
 *  jNetPcap header files
 * 
 * The JNIEXPORT macro defines JNIEXPORT "C" for c++ or nothing for C
 */
#include "jnetpcap_utils.h"
#include "jnetpcap_bpf.h"
#include "jnetpcap_dumper.h"
#include "org_jnetpcap_Pcap.h"



/*
 * Class:     org_jnetpcap_Pcap
 * Method:    openLive
 * Signature: (Ljava/lang/String;IIILjava/lang/StringBuilder;)Lorg/jnetpcap/Pcap;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_Pcap_openLive(JNIEnv *env, jclass clazz,
		jstring jdevice, jint jsnaplen, jint jpromisc, jint jtimeout,
		jobject jerrbuf) {

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
	
	env->ReleaseStringUTFChars(jdevice, device);
	
	if (p == NULL) {
		return NULL;
	}

	/*
	 * Use a no-arg constructor and initialize 'physical' field using
	 * special JNI priviledges.
	 */
	jobject obj = env->NewObject(clazz, pcapConstructorMID);
	setPhysical(env, obj, toLong(p));

	return obj;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    openDead
 * Signature: (II)Lorg/jnetpcap/Pcap;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_Pcap_openDead
(JNIEnv *env, jclass clazz, jint jlinktype, jint jsnaplen) {

	pcap_t *p = pcap_open_dead(jlinktype, jsnaplen);
	if (p == NULL) {
		return NULL;
	}

	/*
	 * Use a no-arg constructor and initialize 'physical' field using
	 * special JNI priviledges.
	 */
	jobject obj = env->NewObject(clazz, pcapConstructorMID);
	setPhysical(env, obj, toLong(p));

	return obj;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    openOffline
 * Signature: (Ljava/lang/String;Ljava/lang/StringBuilder;)Lorg/jnetpcap/Pcap;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_Pcap_openOffline
(JNIEnv *env, jclass clazz, jstring jfname, jobject jerrbuf) {

	if (jfname == NULL || jerrbuf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return NULL;
	}

	char errbuf[PCAP_ERRBUF_SIZE];
	errbuf[0] = '\0'; // Reset the buffer;
	const char *fname = env->GetStringUTFChars(jfname, 0);

	pcap_t *p = pcap_open_offline(fname, errbuf);
	
	env->ReleaseStringUTFChars(jfname, fname);
	
	if (p == NULL) {
		setString(env, jerrbuf, errbuf);
		return NULL;
	}

	/*
	 * Use a no-arg constructor and initialize 'physical' field using
	 * special JNI priviledges.
	 */
	jobject obj = env->NewObject(clazz, pcapConstructorMID);
	setPhysical(env, obj, toLong(p));

	return obj;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_Pcap_close
(JNIEnv *env, jobject obj) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return; // Exception already thrown
	}

	pcap_close(p);
	setPhysical(env, obj, 0); // Clear in Java object

}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    dispatch
 * Signature: (ILorg/jnetpcap/PcapHandler;Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_dispatch
(JNIEnv *env, jobject obj, jint jcnt, jobject jhandler, jobject juser) {

	if (jhandler == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	/*
	 * Structure to encapsulate user data object, and store our JNI information
	 * so we can dispatch to Java land.
	 */
	pcap_user_data_t data;
	data.env = env;
	data.obj = jhandler;
	data.user = juser;
	data.clazz = env->GetObjectClass(jhandler);
	data.p = p;

	data.mid = env->GetMethodID(data.clazz, "nextPacket",
			"(Ljava/lang/Object;JIIILjava/nio/ByteBuffer;)V");

	return pcap_dispatch(p, jcnt, pcap_callback, (u_char *)&data);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    loop
 * Signature: (ILorg/jnetpcap/PcapHandler;Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_loop
(JNIEnv *env, jobject obj, jint jcnt, jobject jhandler, jobject juser) {

	if (jhandler == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	/*
	 * Structure to encapsulate user data object, and store our JNI information
	 * so we can dispatch to Java land.
	 */
	pcap_user_data_t data;
	data.env = env;
	data.obj = jhandler;
	data.user = juser;
	data.clazz = env->GetObjectClass(jhandler);
	data.p = p;

	data.mid = env->GetMethodID(data.clazz, "nextPacket",
			"(Ljava/lang/Object;JIIILjava/nio/ByteBuffer;)V");

	return pcap_loop(p, jcnt, pcap_callback, (u_char *)&data);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    next
 * Signature: (Lorg/jnetpcap/PcapPkthdr;)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_Pcap_next
(JNIEnv *env, jobject obj, jobject jpkt_header) {

	if (jpkt_header == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return NULL;
	}

	pcap_pkthdr pkt_header;

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return NULL; // Exception already thrown
	}

	const u_char *pkt_data = pcap_next(p, &pkt_header);
	if (pkt_data == NULL) {
		return NULL;
	}

	setPktHeader(env, jpkt_header, &pkt_header);

	jobject jbuffer = env->NewDirectByteBuffer((void *)pkt_data,
			pkt_header.caplen);

	return jbuffer;

}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    nextEx
 * Signature: (Lorg/jnetpcap/PcapPkthdr;Lorg/jnetpcap/PcapPktbuffer;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_nextEx
(JNIEnv *env, jobject obj, jobject jpkt_header, jobject jpkt_buffer) {

	if (jpkt_header == NULL || jpkt_buffer == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	pcap_pkthdr *pkt_header;
	const u_char *pkt_data; // PTR will be initialized by pcap_next_ex

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	int r = pcap_next_ex(p, &pkt_header, &pkt_data);
	if (r == 1) {

		setPktHeader(env, jpkt_header, pkt_header);

		jobject jbuffer = env->NewDirectByteBuffer((void *)pkt_data,
				pkt_header->caplen);

		setPktBuffer(env, jpkt_buffer, jbuffer);

	}
	return (jint) r;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    sendPacketPrivate
 * Signature: (Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_sendPacketPrivate
(JNIEnv *env, jobject obj, jobject jbytebuffer, jint jstart, jint jlength) {

	if (jbytebuffer == NULL) {
		throwException(env, NULL_PTR_EXCEPTION,
				"buffer argument is null");
		return -1;
	}

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	u_char *b = (u_char *)env->GetDirectBufferAddress(jbytebuffer);
	if (b == NULL) {
		throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
				"Unable to retrieve physical address from ByteBuffer");
	}

	int r = pcap_sendpacket(p, b + (int) jstart, (int) jlength);
	return r;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    breakloop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_Pcap_breakloop
(JNIEnv *env, jobject obj) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return; // Exception already thrown
	}

	pcap_breakloop(p);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    datalink
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_datalink
(JNIEnv *env, jobject obj) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return pcap_datalink(p);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    setNonBlock
 * Signature: (ILjava/lang/StringBuilder;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_setNonBlock
(JNIEnv *env, jobject obj, jint jnonblock, jobject jerrbuf) {

	if (jerrbuf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	char errbuf[PCAP_ERRBUF_SIZE];
	errbuf[0] = '\0'; // Reset the buffer;

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	int r = pcap_setnonblock(p, jnonblock, errbuf);
	if (r == -1) {
		setString(env, jerrbuf, errbuf);
	}

	return r;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    getNonBlock
 * Signature: (Ljava/lang/StringBuilder;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_getNonBlock
(JNIEnv *env, jobject obj, jobject jerrbuf) {

	if (jerrbuf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	char errbuf[PCAP_ERRBUF_SIZE];
	errbuf[0] = '\0'; // Reset the buffer;

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	int r = pcap_getnonblock(p, errbuf);
	if (r == -1) {
		setString(env, jerrbuf, errbuf);
	}

	return r;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    setDatalink
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_setDatalink
(JNIEnv *env, jobject obj, jint value) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return pcap_set_datalink(p, value);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    snapshot
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_snapshot
(JNIEnv *env, jobject obj) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return pcap_snapshot(p);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    datalinkNameToVal
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_datalinkNameToVal
(JNIEnv *env, jclass clazz, jstring jname) {

	if (jname == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	const char *name = env->GetStringUTFChars(jname, 0);

	int r = (jint) pcap_datalink_name_to_val(name);
	
	env->ReleaseStringUTFChars(jname, name);
	
	return r;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    datalinkValToName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_jnetpcap_Pcap_datalinkValToName
(JNIEnv *env, jclass clazz, jint jdlt) {

	const char *name = pcap_datalink_val_to_name((int)jdlt);

	jstring jname = env->NewStringUTF(name);

	return jname;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    datalinkValToDescription
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_jnetpcap_Pcap_datalinkValToDescription
(JNIEnv *env, jclass clazz, jint jdlt) {

	const char *name = pcap_datalink_val_to_description((int)jdlt);

	jstring jname = env->NewStringUTF(name);

	return jname;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    isSwapped
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_isSwapped
(JNIEnv *env, jobject obj) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_is_swapped(p);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    majorVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_majorVersion
(JNIEnv *env, jobject obj) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_major_version(p);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    minorVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_minorVersion
(JNIEnv *env, jobject obj) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_minor_version(p);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    getErr
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_jnetpcap_Pcap_getErr
(JNIEnv *env, jobject obj) {

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return NULL; // Exception already thrown
	}

	const char *str = pcap_geterr(p);

	jstring jstr = env->NewStringUTF(str);

	return jstr;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    libVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_jnetpcap_Pcap_libVersion
(JNIEnv *env , jclass clazz) {

	const char *str = pcap_lib_version();

	jstring jstr = env->NewStringUTF(str);

	return jstr;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    findAllDevs
 * Signature: (Ljava/util/Listf;Ljava/lang/StringBuilder;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_findAllDevs
(JNIEnv *env, jclass clazz, jobject jlist, jobject jerrbuf) {

	if (jlist == NULL || jerrbuf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	char errbuf[PCAP_ERRBUF_SIZE];
	errbuf[0] = '\0'; // Reset the buffer;

	pcap_if_t *alldevsp;

	int r = pcap_findalldevs(&alldevsp, errbuf);
	if (r != 0) {
		setString(env, jerrbuf, errbuf);
		return r;
	}

	if (alldevsp != NULL) {
		jmethodID MID_add = findMethod(env, jlist, "add",
				"(Ljava/lang/Object;)Z");

		jobject jpcapif = newPcapIf(env, jlist, MID_add, alldevsp);
		if (jpcapif == NULL) {
			return -1; // Out of memory
		}

		if (env->CallBooleanMethod(jlist, MID_add, jpcapif) == JNI_FALSE) {
			env->DeleteLocalRef(jpcapif);

			return -1; // Failed to add to the list
		}

		env->DeleteLocalRef(jpcapif);

		return r;
	}

	/*
	 * The device list is freed up, since we copied all the info into Java
	 * objects that are no longer dependent on native C classes.
	 */
	pcap_freealldevs(alldevsp);

	return r;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    compileNoPcap
 * Signature: (IILorg/jnetpcap/PcapBpfProgram;Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_compileNoPcap
(JNIEnv *env, jclass clazz, jint snaplen, jint dlt,
		jobject jbpf, jstring jstr, jint optimize, jint mask) {

	if (jbpf == NULL || jstr == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	bpf_program src;

	freeBpfProgramIfExists(env, jbpf);

	char *str = (char *)env->GetStringUTFChars(jstr, 0);

	int r = pcap_compile_nopcap(snaplen, dlt, &src, str, optimize, (bpf_u_int32) mask);
	
	env->ReleaseStringUTFChars(jstr, str);
	
	if (r != 0) {
		return r;
	}

	/*
	 * Now make a copy and store reference to it in jbpf object
	 */
	bpfProgramInitFrom(env, jbpf, &src);

	/*
	 * We're done with BPF copy of the code, we copied it and stored in PcapBpfProgram
	 * object.
	 */
	pcap_freecode(&src);

	return r;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    compile
 * Signature: (Lorg/jnetpcap/PcapBpfProgram;Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_compile
(JNIEnv *env, jobject obj, jobject jbpf, jstring jstr, jint optimize, jint mask) {

	if (jbpf == NULL || jstr == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	bpf_program src;

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	freeBpfProgramIfExists(env, jbpf);

	char *str = (char *)env->GetStringUTFChars(jstr, 0);

	int r = pcap_compile(p, &src, str, optimize, (bpf_u_int32) mask);
	
	env->ReleaseStringUTFChars(jstr, str);
	
	if (r != 0) {
		return r;
	}

	/*
	 * Now make a copy and store reference to it in jbpf object
	 */
	bpfProgramInitFrom(env, jbpf, &src);

	/*
	 * We're done with BPF copy of the code, we copied it and stored in PcapBpfProgram
	 * object.
	 */
	pcap_freecode(&src);

	return r;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    freecode
 * Signature: (Lorg/jnetpcap/PcapBpfProgram;)V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_Pcap_freecode
(JNIEnv *env, jclass clazz, jobject jbpf) {

	if (jbpf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return;
	}

	freeBpfProgramIfExists(env, jbpf);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    setFilter
 * Signature: (Lorg/jnetpcap/PcapBpfProgram;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_setFilter
(JNIEnv *env, jobject obj, jobject jbpf) {

	if (jbpf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return -1;
	}

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	bpf_program *b = getBpfProgram(env, jbpf);
	if (b == NULL) {
		return -1; // Exception already thrown
	}

	return pcap_setfilter(p, b);
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    dumpOpen
 * Signature: (Ljava/lang/String;)Lorg/jnetpcap/PcapDumper;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_Pcap_dumpOpen
(JNIEnv *env, jobject obj, jstring jfname) {

	if (jfname == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, "fname argument is null");
		return NULL;
	}

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return NULL; // Exception already thrown
	}

	char *str = (char *)env->GetStringUTFChars(jfname, 0);
	if (str[0] != '\0' && str[1] == '-' && str[2] == '\0') {
		throwException(env, ILLEGAL_ARGUMENT_EXCEPTION,
				"use of '-' for dumping to stdout is not supported.");
		
		env->ReleaseStringUTFChars(jfname, str);
		
		return NULL;
	}

	pcap_dumper_t *d = pcap_dump_open(p, str);
	
	env->ReleaseStringUTFChars(jfname, str);
	
	if (d == NULL) {
		return NULL; // Exception already thrown
	}

	jobject jdumper = newPcapDumper(env, d);

	return jdumper;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    stats
 * Signature: (Lorg/jnetpcap/PcapStat;)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_stats
(JNIEnv *env, jobject obj, jobject jstats) {

	if (jstats == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, "stats argument is null");
		return -1;
	}

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	pcap_stat stats;
	memset(&stats, 0, sizeof(stats));
	int r = pcap_stats(p, &stats); // Fills the stats structure
	if (r != 0) {
		return r; // error
	}

	setPcapStat(env, jstats, &stats);

	return r;
}

/*
 * Class:     org_jnetpcap_Pcap
 * Method:    checkIsActive
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_Pcap_checkIsActive
  (JNIEnv *env, jobject obj) {
	
	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return; // Exception already thrown
	}

	return; // No exception thrown, check OK
}
