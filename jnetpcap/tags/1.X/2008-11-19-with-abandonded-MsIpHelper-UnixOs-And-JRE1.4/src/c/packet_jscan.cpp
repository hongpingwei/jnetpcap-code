/***************************************************************************
 * Copyright (C) 2007, Sly Technologies, Inc                               *
 * Distributed under the Lesser GNU Public License  (LGPL)                 *
 ***************************************************************************/

/*
 * Utility file that provides various conversion methods for chaging objects
 * back and forth between C and Java JNI.
 */

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

#include "nio_jmemory.h"
#include "packet_jscanner.h"
#include "jnetpcap_utils.h"
#include "org_jnetpcap_packet_JScan.h"
#include "export.h"


/****************************************************************
 * **************************************************************
 * 
 * Java declared native functions
 * 
 * **************************************************************
 ****************************************************************/

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_id
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JScan_scan_1id__
  (JNIEnv *env, jobject obj) {
	
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return -1;
	}

	return scan->id;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_next_id
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JScan_scan_1next_1id__
  (JNIEnv *env, jobject obj) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return -1;
	}
	
	return scan->next_id;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_length
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JScan_scan_1length__
  (JNIEnv *env, jobject obj) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return -1;
	}

	return scan->length;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_id
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_packet_JScan_scan_1id__I
  (JNIEnv *env, jobject obj, jint id) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return;
	}

	scan->id = (int) id;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_next_id
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_packet_JScan_scan_1next_1id__I
  (JNIEnv *env, jobject obj, jint next_id) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return;
	}

	scan->next_id = (int) next_id;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_length
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_packet_JScan_scan_1length__I
  (JNIEnv *env, jobject obj, jint length) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return;
	}

	scan->length = (int) length;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_buf
 * Signature: (Lorg/jnetpcap/nio/JBuffer;)V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_packet_JScan_scan_1buf
  (JNIEnv *env, jobject obj, jobject jbuf) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return;
	}

	char *buf = (char *)getJMemoryPhysical(env, jbuf);
	scan->buf = buf;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_buf_len
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_packet_JScan_scan_1buf_1len
  (JNIEnv *env, jobject obj, jint len) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return;
	}

	scan->buf_len = (int) len;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_offset
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_packet_JScan_scan_1offset__I
  (JNIEnv *env, jobject obj, jint offset) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return;
	}

	scan->offset = (int) offset;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_packet
 * Signature: ()Lorg/jnetpcap/packet/JPacket;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_packet_JScan_scan_1packet
  (JNIEnv *env, jobject obj) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return NULL;
	}

	return scan->jpacket;
}

/*
 * Class:     org_jnetpcap_packet_JScan
 * Method:    scan_offset
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JScan_scan_1offset__
  (JNIEnv *env, jobject obj) {
	scan_t *scan = (scan_t *)getJMemoryPhysical(env, obj);
	if (scan == NULL) {
		return -1;
	}

	return scan->offset;
}
