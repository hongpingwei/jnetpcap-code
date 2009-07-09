
// Include this file after jni.h is included. It undefines MS compiler, def for
// gcc specific one.
//
#ifndef Include_nap_h
#define Include_nap_h

#include <stdint.h>

#undef __declspec
#define __declspec(a) extern "C"

#define NAP_RECORD_BLOCK		0x1
#define NAP_RECORD_PAKCET		0x0
#define	NAP_RECORD_META			0x2
#define	NAP_RECORD_VENDOR		0x3
#define	NAP_RECORD_NOP			0xF

typedef struct nap_record_t {
	uint32_t	type:4;
	uint32_t	unused:24; // Record type specific
	uint32_t	length;
	
	uint32_t	unused1; // Record type specific
	uint32_t	unused2; // Record type specific
	uint32_t	unused3; // Record type specific
	uint32_t	unused4; // Record type specific
	
} nap_record_t;
#define	NAP_RECORD_SIZE				sizeof(nap_packet_t)

#define	NAP_BLOCK_FLAG_SCRAMBLE_HEADER	0x0001
#define	NAP_BLOCK_FLAG_SCRAMBLE_DATA	0x0002
#define	NAP_BLOCK_FLAG_ENCRYPT_HEADER	0x0004
#define	NAP_BLOCK_FLAG_ENCRYPT_DATA		0x0008

#define	NAP_BLOCK_MAGIC					0x12AA3BB4
#define NAP_BLOCK_MAJOR					1
#define	NAP_BLOCK_MINOR					0

#define	NAP_DEFAULT_BLOCK_SIZE			(512 * 1024)
#define NAP_DEFAULT_BLOCK_FLAGS			0

typedef struct nap_block_t {
	uint32_t	magic;			// NAP magic number 0x12AA3BB4
	uint32_t	length;			// Length of the block in bytes including hdr
	uint8_t		major;			// Major version number
	uint8_t		minor;			// Minor version number
	uint16_t	flags;			// Flags applicable for entire block
	
	uint32_t	block_id;		// Id of this block record
	uint32_t	next_block_id;	// Id of the block after this record
	uint32_t	count;			// Packet count

} nap_block_t;

#define	NAP_BLOCK_SIZE				NAP_RECORD_SIZE


#define	NAP_PACKET_FLAG_STRUCTURAL_ERROR	0x0001
#define	NAP_PACKET_FLAG_LOGICAL_ERROR		0x0002
#define	NAP_PACKET_FLAG_FRAME_PREFIX		0x0004
#define	NAP_PACKET_FLAG_FRAME_CRC			0x0008
#define	NAP_PACKET_FLAG_CRC_OFFLOAD			0x0010
#define	NAP_PACKET_FLAG_HIDDEN				0x0020
#define	NAP_PACKET_FLAG_MARKED				0x0040
#define	NAP_PACKET_FLAG_NODECODE			0x0080
#define	NAP_PACKET_FLAG_COMMENT				0x0100
#define	NAP_PACKET_FLAG_NANO_RESOLUTION		0x0200
#define	NAP_PACKET_FLAG_USER1				0x1000
#define	NAP_PACKET_FLAG_USER2				0x2000
#define	NAP_PACKET_FLAG_USER3				0x3000
#define	NAP_PACKET_FLAG_USER4				0x4000


typedef struct nap_packet_t {
	uint8_t		type:4;		// packet record type == 0x0
	uint16_t	capture:12;	// index into capture system table 
	uint16_t	flags;		// packet record flags
	uint32_t	length;		// record length in bytes including the header
	
	uint32_t	original;	// number of bytes in the original packet
	uint32_t	included;	// number of bytes copied into this record
	uint32_t	seconds;	// timestamp in UNIX seconds
	uint32_t	nanos;		// fraction nanoseconds valid range 0 - 999,999,999
} nap_packet_t, *pnap_packet_t;

typedef nap_record_t nap_nop_t, *pnap_nop_t;

typedef struct nap_vendor_t {
	uint32_t	type:4;		// vendor record type == 0x3
	uint32_t	flags:4;	// Vendor record specific flags
	uint32_t	vendor:24;	// Pre-assigned vendor ID
	uint32_t	length;		// Record length in bytes including header
	
	uint32_t	reserved1;  // Vendor specific
	uint32_t	reserved2;  // Vendor specific
	uint32_t	reserved3;  // Vendor specific
	uint32_t	reserved4;  // Vendor specific
	
} nap_vendor_t;

#define	NAP_PACKET_SIZE			NAP_RECORD_SIZE

#define	NAP_DLT_NONE			0
#define	NAP_DLT_ETHERNET		1
#define	NAP_DLT_ETHERNET_3		2
#define	NAP_DLT_AX25			3
#define	NAP_DLT_PRONET_TOKEN	4
#define	NAP_DLT_CHAOS			5
#define	NAP_DLT_IEEE_TOKEN		6
#define	NAP_DLT_SLIP			8
#define	NAP_DLT_PPP				9
#define	NAP_DLT_FDDI			10
#define NAP_DLT_ATM_LLC			11
#define	NAP_DLT_PPPOE			51
#define NAP_DLT_WIFI			105

#define	NAP_OK					0
#define	NAP_NOT_OK				1
#define	NAP_NOT_FOUND			2

#define	NAP_FNAME_SIZE			1024

#define	NAP_MSG_OUT_OF_MEMORY	"out of memory"

#define NAP_DEFAULT_OPEN_MODE	"r+"


/*
 * State structures
 */

struct nap_t;

typedef struct block_t {
	struct nap_t	*b_nap;			// parent
	
	uint64_t		b_start;		// start of the block within the file
	size_t			b_length;			// length of the block
	
	uint32_t		b_offset;		// offset into the current block
	uint32_t		b_read;			// How much data from this block has been read
	nap_block_t		*b_header;		// current block header
	
} block_t;

typedef struct nap_t {
	char		fname[NAP_FNAME_SIZE];
	FILE		*fhandle;
	uint64_t	flength;	// file length
	char		fmode[8];
} nap_t, *pcap_t;

#define	NAP_SIZE			sizeof(nap_t)

const nap_t *nap_open(const char *file, char *errbuf);
const nap_t *nap_open_mode(const char *file, char *mode, char *errbuf);
int nap_close(const nap_t *nap);
const uint32_t  generate_id();

#endif
