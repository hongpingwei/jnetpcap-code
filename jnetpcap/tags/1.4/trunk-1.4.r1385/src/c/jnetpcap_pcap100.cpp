/***************************************************************************
 * Copyright (C) 2010, Sly Technologies, Inc                               *
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

#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <pcap.h>
#include <jni.h>
#include <string.h>

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
#include "jnetpcap_ids.h"
#include "nio_jmemory.h"
#include "org_jnetpcap_Pcap.h"
#include "export.h"


/*
 * Class:     org_jnetpcap
 * Method:    create
 * Signature: (Ljava/lang/String;Ljava/lang/StringBuilder;)Lorg/jnetpcap/Pcap;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_Pcap_create
  (JNIEnv *env, jclass clazz, jstring jdevice, jobject jerrbuf) {

#if (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	throwException(env, UNSUPPORTED_OPERATION_EXCEPTION, "");
	return NULL;
#else
	
	if (jdevice == NULL || jerrbuf == NULL) {
		throwException(env, NULL_PTR_EXCEPTION, NULL);
		return NULL;
	}

	char errbuf[PCAP_ERRBUF_SIZE];
	errbuf[0] = '\0'; // Reset the buffer;

	const char *device = env->GetStringUTFChars(jdevice, 0);

	//	printf("device=%s snaplen=%d, promisc=%d timeout=%d\n",
	//			device, jsnaplen, jpromisc, jtimeout);

	pcap_t *p = pcap_create(device, errbuf);
	setString(env, jerrbuf, errbuf); // Even if no error, could have warning msg

	env->ReleaseStringUTFChars(jdevice, device);

	if (p == NULL) {
		return NULL;
	}

//	jmethodID GetMethodID(JNIEnv *env, jclass clazz,
//	const char *name, const char *sig);
	
	jmethodID constructor_MID = env->GetMethodID(clazz, "<init>", "()V");
	if (constructor_MID == NULL) {
		pcap_close(p); // We already have the handle open, don't forget to close
		return NULL;
	}
	
	/*
	 * Use a no-arg constructor and initialize 'physical' field using
	 * special JNI priviledges.
	 */
	jobject obj = env->NewObject(clazz, constructor_MID);
	setPhysical(env, obj, toLong(p));

	return obj;
#endif
}

/*
 * Class:     org_jnetpcap
 * Method:    isImplemented
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_jnetpcap_Pcap_isImplemented
  (JNIEnv *env, jclass clazz) {
	
#if (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	return JNI_FALSE;
#else
	return JNI_TRUE;
#endif
}

/*
 * Class:     org_jnetpcap
 * Method:    registerNatives
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_Pcap_registerNatives
  (JNIEnv *env, jclass clazz) {

}

/*
 * Class:     org_jnetpcap
 * Method:    activate
 * Signature: ()I
 */                  /*Java_org_jnetpcap_Pcap_activate*/
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_activate
  (JNIEnv *env, jobject obj) {
	
#if (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	throwException(env, UNSUPPORTED_OPERATION_EXCEPTION, "");
	return -1;
#else
	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_activate(p);
#endif
}


/*
 * Class:     org_jnetpcap
 * Method:    canSetRfmon
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_canSetRfmon
  (JNIEnv *env, jobject obj) {
#if defined(WIN32) || defined(WIN64) || (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	return (jint) 0;
#else

	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_can_set_rfmon(p);
#endif
}

/*
 * Class:     org_jnetpcap
 * Method:    setBufferSize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_setBufferSize
  (JNIEnv *env, jobject obj, jlong jsize) {
#if (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	throwException(env, UNSUPPORTED_OPERATION_EXCEPTION, "");
	return -1;
#else
	
	
	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_set_buffer_size(p, (int) jsize);
#endif
}

/*
 * Class:     org_jnetpcap
 * Method:    setDirection
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_setDirection
  (JNIEnv *env, jobject obj, jint jdir) {
	
#if (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	throwException(env, UNSUPPORTED_OPERATION_EXCEPTION, "");
	return -1;
#else
	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_setdirection(p, (pcap_direction_t) jdir);
#endif
}

/*
 * Class:     org_jnetpcap
 * Method:    setPromisc
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_setPromisc
  (JNIEnv *env, jobject obj, jint jpromisc) {
#if (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	throwException(env, UNSUPPORTED_OPERATION_EXCEPTION, "");
	return -1;
#else
	
	
	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_set_promisc(p, (int) jpromisc);
#endif
}

/*
 * Class:     org_jnetpcap
 * Method:    setRfmon
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_setRfmon
  (JNIEnv *env, jobject obj, jint jrfmon) {
	
#if defined(WIN32) || defined(WIN64) || (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	return (jint) -1;
#else
	
	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_set_rfmon(p, (int) jrfmon);
#endif
}

/*
 * Class:     org_jnetpcap
 * Method:    setSnaplen
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_setSnaplen
  (JNIEnv *env, jobject obj, jint jsnaplen) {
	
#if (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	throwException(env, UNSUPPORTED_OPERATION_EXCEPTION, "");
	return -1;
#else
	
	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_set_snaplen(p, (int) jsnaplen);
#endif
}

/*
 * Class:     org_jnetpcap
 * Method:    setTimeout
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_Pcap_setTimeout
  (JNIEnv *env, jobject obj, jint jtimeout) {
	
#if (LIBPCAP_VERSION < LIBPCAP_PCAP_CREATE)
	throwException(env, UNSUPPORTED_OPERATION_EXCEPTION, "");
	return -1;
#else
	
	pcap_t *p = getPcap(env, obj);
	if (p == NULL) {
		return -1; // Exception already thrown
	}

	return (jint) pcap_set_timeout(p, (int) jtimeout);
#endif
}






