/**
 * Copyright (C) 2006 Sly Technologies, Inc.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.slytechs.capture.file;

import java.nio.ByteBuffer;

import com.slytechs.capture.file.capabilities.CaptureTimestamp;
import com.slytechs.utils.io.BitBuffer;

/**
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public interface PacketRecord extends Record, CaptureTimestamp {

	public int getProtocolNumber();
	public String getProtocolName();
	
	public byte[] toByteArray();
	
	public ByteBuffer toByteBuffer();
	
	public BitBuffer toBitBuffer();
	
	public void setPacketData(byte[] data);
	public void setPacketData(ByteBuffer buffer);
	public void setPacketData(BitBuffer buffer);
	
	public long getPacketDataPosition();
	
	public int getPacketDataLength();
}