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
#include "org_jnetpcap_packet_JHeader_State.h"
#include "export.h"

/****************************************************************
 * **************************************************************
 * 
 * NON Java declared native functions. Private scan function
 * 
 * **************************************************************
 ****************************************************************/

/****************************************************************
 * **************************************************************
 * 
 * Java declared native functions
 * 
 * **************************************************************
 ****************************************************************/

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    getAnalysis
 * Signature: ()Lorg/jnetpcap/analysis/JAnalysis;
 */
JNIEXPORT jobject JNICALL Java_org_jnetpcap_packet_JHeader_00024State_getAnalysis
(JNIEnv *env, jobject obj) {

	header_t *header = (header_t *)jmem_data_ro_get(env, obj);
	if (header == NULL) {
		return NULL;
	}

	return header->hdr_analysis;
}

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    getId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JHeader_00024State_getId
(JNIEnv *env, jobject obj) {

	header_t *header = (header_t *)jmem_data_ro_get(env, obj);
	if (header == NULL) {
		return -1;
	}

	return (jint) header->hdr_id;
}

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    getOffset
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JHeader_00024State_getOffset
(JNIEnv *env, jobject obj) {

	header_t *header = (header_t *)jmem_data_ro_get(env, obj);
	if (header == NULL) {
		return -1;
	}

	return (jint) header->hdr_offset;
}

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    getLength
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JHeader_00024State_getLength
(JNIEnv *env, jobject obj) {

	header_t *header = (header_t *)jmem_data_ro_get(env, obj);
	if (header == NULL) {
		return -1;
	}

	return (jint) header->hdr_length;
}

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    setAnalysis
 * Signature: (Lorg/jnetpcap/packet/JPacket$State;Lorg/jnetpcap/analysis/JAnalysis;)V
 */
JNIEXPORT void JNICALL Java_org_jnetpcap_packet_JHeader_00024State_setAnalysis
(JNIEnv *env, jobject obj, jobject packet, jobject analysis) {

	/*
	 * We attach and create JREFs on the owner node that this node points to
	 * and not on this particular node itself.
	 */
	jmemory_t *node = jmem_get_owner(env, obj);
	if (node == NULL) {
		return;
	}

	header_t *header = (header_t *)jmem_data_ro(node);
	if (header == NULL) {
		jnp_exception(env);
		return;
	}

	if (header->hdr_analysis != NULL 
			&& jref_lc_free_obj(env, node, header->hdr_analysis)) {
		return;
	}

	if (analysis == NULL) {
		header->hdr_analysis = NULL;
		return;
	}
	
	if ((header->hdr_analysis = jref_lc_create(env, node, analysis)) != NULL) {
		return;
	}
}

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    getPrefix
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JHeader_00024State_getPrefix
(JNIEnv *env, jobject obj) {

	header_t *header = (header_t *)jmem_data_ro_get(env, obj);
	if (header == NULL) {
		return -1;
	}

	return (jint)header->hdr_prefix;
}

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    getGap
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JHeader_00024State_getGap
(JNIEnv *env, jobject obj) {

	header_t *header = (header_t *)jmem_data_ro_get(env, obj);
	if (header == NULL) {
		return -1;
	}

	return (jint)header->hdr_gap;
}

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    getPayload
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JHeader_00024State_getPayload
(JNIEnv *env, jobject obj) {

	header_t *header = (header_t *)jmem_data_ro_get(env, obj);
	if (header == NULL) {
		return -1;
	}

	return (jint)header->hdr_payload;
}

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    getPostfix
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JHeader_00024State_getPostfix
(JNIEnv *env, jobject obj) {

	header_t *header = (header_t *)jmem_data_ro_get(env, obj);
	if (header == NULL) {
		return -1;
	}

	return (jint)header->hdr_postfix;
}

/*
 * Class:     org_jnetpcap_packet_JHeader_State
 * Method:    getFlags
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jnetpcap_packet_JHeader_00024State_getFlags
(JNIEnv *env, jobject obj) {

	header_t *header = (header_t *)jmem_data_ro_get(env, obj);
	if (header == NULL) {
		return -1;
	}

	return (jint)header->hdr_flags;
}

